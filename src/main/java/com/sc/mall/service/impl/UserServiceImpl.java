package com.sc.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sc.mall.common.utils.CaptchaUtil;
import com.sc.mall.common.utils.JwtUtil;
import com.sc.mall.common.utils.Md5Util;
import com.sc.mall.entity.User;
import com.sc.mall.entity.dto.EmailRegisterDTO;
import com.sc.mall.entity.dto.EmailSendCodeDTO;
import com.sc.mall.entity.dto.UserLoginDTO;
import com.sc.mall.entity.dto.UserRegisterDTO;
import com.sc.mall.entity.vo.CaptchaVO;
import com.sc.mall.entity.vo.UserLoginVO;
import com.sc.mall.mapper.UserMapper;
import com.sc.mall.service.IUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private EmailSenderService emailSenderService;

    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(2);
    private static final Duration EMAIL_CODE_TTL = Duration.ofMinutes(5);
    private static final Duration SEND_LIMIT_TTL = Duration.ofSeconds(60);

    private static final String KEY_CAPTCHA = "captcha:";
    private static final String KEY_EMAIL_CODE = "email_code:";
    private static final String KEY_EMAIL_LIMIT = "email_limit:";
    private static final String KEY_IP_LIMIT = "ip_limit:";

    private static final SecureRandom SR = new SecureRandom();

    // =========================
    // 0) 图形验证码
    // =========================
    @Override
    public CaptchaVO createCaptcha() {
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        String code = CaptchaUtil.randomCode(4);

        try {
            String img = CaptchaUtil.renderBase64Svg(code, 140, 44);

            // 统一用常量前缀
            stringRedisTemplate.opsForValue()
                    .set(KEY_CAPTCHA + captchaKey, code.toLowerCase(), CAPTCHA_TTL);

            CaptchaVO vo = new CaptchaVO();
            vo.setCaptchaKey(captchaKey);
            vo.setCaptchaImg(img);
            return vo;

        } catch (Exception e) {
            log.error("createCaptcha failed, captchaKey={}, code={}", captchaKey, code, e);
            throw new RuntimeException("生成图形验证码失败：" + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    // =========================
    // 1) 发送邮箱验证码（先校验图形验证码）
    // =========================
    @Override
    public void sendEmailCode(EmailSendCodeDTO dto, String clientIp) {
        String email = normalizeEmail(dto.getEmail());
        String maskedEmail = maskEmail(email);

        log.info("[EMAIL_CODE] 请求发送验证码: email={}, ip={}, captchaKey={}", maskedEmail, safe(clientIp), dto.getCaptchaKey());

        // 1) 校验图形验证码
        String captchaKey = KEY_CAPTCHA + dto.getCaptchaKey();
        String redisCaptcha = stringRedisTemplate.opsForValue().get(captchaKey);

        if (!StringUtils.hasText(redisCaptcha)) {
            log.warn("[EMAIL_CODE] 图形验证码不存在/过期: email={}, captchaKey={}", maskedEmail, dto.getCaptchaKey());
            throw new RuntimeException("图形验证码已过期，请刷新");
        }

        String inputCaptcha = dto.getCaptchaCode() == null ? "" : dto.getCaptchaCode().trim().toLowerCase();
        if (!redisCaptcha.equals(inputCaptcha)) {
            log.warn("[EMAIL_CODE] 图形验证码错误: email={}, input={}, expected={}", maskedEmail, inputCaptcha, redisCaptcha);
            throw new RuntimeException("图形验证码错误");
        }

        // 用完即删（防重放）
        stringRedisTemplate.delete(captchaKey);

        // 2) 限流：同邮箱60秒1次（注意：后面如果发信失败会回滚这个key）
        String emailLimitKey = KEY_EMAIL_LIMIT + email;
        Boolean emailLimitOk = stringRedisTemplate.opsForValue().setIfAbsent(emailLimitKey, "1", SEND_LIMIT_TTL);
        if (Boolean.FALSE.equals(emailLimitOk)) {
            log.warn("[EMAIL_CODE] 邮箱限流命中: email={}", maskedEmail);
            throw new RuntimeException("发送太频繁，请稍后再试");
        }

        // 3) 限流：同IP 60秒最多5次（简单计数）
        String ipKey = KEY_IP_LIMIT + (clientIp == null ? "unknown" : clientIp);
        Long ipCount = stringRedisTemplate.opsForValue().increment(ipKey);
        if (ipCount != null && ipCount == 1) {
            stringRedisTemplate.expire(ipKey, 60, TimeUnit.SECONDS);
        }
        if (ipCount != null && ipCount > 5) {
            log.warn("[EMAIL_CODE] IP限流命中: ip={}, count={}", safe(clientIp), ipCount);
            throw new RuntimeException("请求过于频繁，请稍后再试");
        }

        // 4) 生成验证码（6位数字）
        String code = random6Digits();
        log.info("[EMAIL_CODE] 生成验证码: email={}, code={}, ttlSec={}", maskedEmail, code, EMAIL_CODE_TTL.toSeconds());
        // 上线前把上面 code 打印降为 debug 或删除即可

        // 5) 先写Redis（原子性更好），后发邮件；发失败则回滚
        String emailCodeKey = KEY_EMAIL_CODE + email;
        stringRedisTemplate.opsForValue().set(emailCodeKey, code, EMAIL_CODE_TTL);

        try {
            emailSenderService.sendVerifyCode(email, code);
            log.info("[EMAIL_CODE] 验证码流程完成: email={}", maskedEmail);
        } catch (RuntimeException ex) {
            // 回滚：让用户能立刻重试
            stringRedisTemplate.delete(emailCodeKey);
            stringRedisTemplate.delete(emailLimitKey);
            log.error("[EMAIL_CODE] 邮件发送失败已回滚Redis: email={}", maskedEmail, ex);
            throw ex;
        }
    }

    // =========================
    // 2) 邮箱注册（邮箱作为登录账号）
    // =========================
    @Override
    public User registerByEmail(EmailRegisterDTO dto) {
        String email = normalizeEmail(dto.getEmail());

        // 1) 校验邮箱验证码
        String redisCode = stringRedisTemplate.opsForValue().get(KEY_EMAIL_CODE + email);
        if (!StringUtils.hasText(redisCode)) {
            throw new RuntimeException("邮箱验证码已过期，请重新获取");
        }
        if (!redisCode.equals(dto.getEmailCode().trim())) {
            throw new RuntimeException("邮箱验证码错误");
        }

        // 2) 校验邮箱是否已注册（email UNIQUE）
        LambdaQueryWrapper<User> wEmail = new LambdaQueryWrapper<>();
        wEmail.eq(User::getEmail, email);
        if (this.count(wEmail) > 0) {
            throw new RuntimeException("该邮箱已注册");
        }

        // 3) 生成 username（注意：你库里 username 是 UNIQUE + NOT NULL）
        String preferred = dto.getUsername();
        String username = generateUniqueUsername(preferred, email);

        // 4) 创建用户（role=3 前端用户）
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);

        String salt = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        user.setSalt(salt);
        user.setPassword(Md5Util.encrypt(dto.getPassword(), salt));

        user.setRole((byte) 3);
        user.setStatus((byte) 1);
        user.setPoints(0);

        this.save(user);

        // 5) 删除验证码（用一次即失效）
        stringRedisTemplate.delete(KEY_EMAIL_CODE + email);

        // 6) 脱敏返回（同时建议实体层@JsonIgnore兜底，见Step 3）
        user.setPassword(null);
        user.setSalt(null);
        return user;
    }

    // =========================
    // 3) 旧 register/login：保留 + 增强（支持邮箱登录）
    // =========================
    @Override
    public User register(UserRegisterDTO registerDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("用户名已存在，请换一个试试");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());

        String salt = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        user.setSalt(salt);
        user.setPassword(Md5Util.encrypt(registerDTO.getPassword(), salt));

        user.setStatus((byte) 1);
        user.setRole((byte) (registerDTO.getRole() == null ? 3 : registerDTO.getRole()));
        user.setPoints(0);

        this.save(user);

        user.setPassword(null);
        user.setSalt(null);
        return user;
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        String account = loginDTO.getUsername().trim();

        User user;
        if (account.contains("@")) {
            LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
            w.eq(User::getEmail, account.toLowerCase());
            user = this.getOne(w);
        } else {
            LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
            w.eq(User::getUsername, account);
            user = this.getOne(w);
        }

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        String salt = user.getSalt();
        String encrypted = Md5Util.encrypt(loginDTO.getPassword(), salt);
        boolean ok = encrypted != null && encrypted.equals(user.getPassword());

        if (!ok) {
            String oldEncrypted = Md5Util.encrypt(loginDTO.getPassword(), null);
            if (oldEncrypted != null && oldEncrypted.equals(user.getPassword())) {
                String newSalt = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                user.setSalt(newSalt);
                user.setPassword(Md5Util.encrypt(loginDTO.getPassword(), newSalt));
                this.updateById(user);
                ok = true;
            }
        }

        if (!ok) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.createToken(user.getId(), user.getUsername(), (int) user.getRole());

        UserLoginVO vo = new UserLoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole((int) user.getRole());
        vo.setToken(token);
        return vo;
    }

    // =========================
    // helpers
    // =========================
    private static String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase();
    }

    private static String random6Digits() {
        int n = SR.nextInt(900000) + 100000;
        return String.valueOf(n);
    }

    private String generateUniqueUsername(String preferred, String email) {
        String base;
        if (StringUtils.hasText(preferred)) {
            base = preferred.trim();
        } else {
            int at = email.indexOf('@');
            base = (at > 0 ? email.substring(0, at) : email);
        }

        // 去空白，防止“看似不同实际同名”
        base = base.replaceAll("\\s+", "");
        if (!StringUtils.hasText(base)) {
            base = "user";
        }

        // username 列是 varchar(64)，给后缀留位置
        if (base.length() > 50) base = base.substring(0, 50);

        if (!existsUsername(base)) return base;

        for (int i = 0; i < 10; i++) {
            String candidate = base + "_" + (SR.nextInt(9000) + 1000); // 4位
            if (candidate.length() > 64) candidate = candidate.substring(0, 64);
            if (!existsUsername(candidate)) return candidate;
        }

        // 兜底：UUID短串
        String candidate = base + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        if (candidate.length() > 64) candidate = candidate.substring(0, 64);
        return candidate;
    }

    private boolean existsUsername(String username) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        w.eq(User::getUsername, username);
        return this.count(w) > 0;
    }

    private static String maskEmail(String email) {
        if (email == null) return "null";
        int at = email.indexOf('@');
        if (at <= 1) return "***" + (at >= 0 ? email.substring(at) : "");
        String name = email.substring(0, at);
        String domain = email.substring(at);
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + domain;
    }

    private static String safe(String s) {
        return s == null ? "null" : s;
    }
}
