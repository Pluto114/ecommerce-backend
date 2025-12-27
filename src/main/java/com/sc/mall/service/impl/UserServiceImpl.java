package com.sc.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sc.mall.common.exception.GlobalExceptionHandler;
import com.sc.mall.common.utils.JwtUtil;
import com.sc.mall.common.utils.Md5Util;
import com.sc.mall.entity.User;
import com.sc.mall.entity.dto.UserLoginDTO;
import com.sc.mall.entity.dto.UserRegisterDTO;
import com.sc.mall.entity.vo.UserLoginVO;
import com.sc.mall.mapper.UserMapper;
import com.sc.mall.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 (三合一) 服务实现类
 * </p>
 *
 * @author 斯聪
 * @since 2025-05-29
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public User register(UserRegisterDTO registerDTO) {
        // 1. 校验用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        if (this.count(wrapper) > 0) {
            // 这里为了简单，直接抛个 RuntimeException，会被全局异常捕获变成 500
            // 更好的做法是自定义 BusinessException 并返回 400
            throw new RuntimeException("用户名已存在，请换一个试试");
        }

        // 2. 创建用户对象
        User user = new User();
        // 将 DTO 属性拷贝到 Entity
        BeanUtils.copyProperties(registerDTO, user);

        // 3. 密码加密 (MD5)
        // 实际开发中可以使用 user.setSalt() 设置随机盐，这里暂传 null
        String encryptedPwd = Md5Util.encrypt(registerDTO.getPassword(), null);
        user.setPassword(encryptedPwd);

        // 4. 设置默认状态
        user.setStatus((byte) 1); // 1-启用
        // 如果前端没传角色，默认设为 3-前端用户
        if (user.getRole() == null) {
            user.setRole((byte) 3);
        }

        // 5. 保存到数据库
        this.save(user);

        // 清空密码再返回，防止泄露
        user.setPassword(null);
        return user;
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 校验密码
        // 将用户输入的密码加密后，与数据库存的密文对比
        String inputPwdEncrypted = Md5Util.encrypt(loginDTO.getPassword(), user.getSalt()); // 注意：如果上面注册用了盐，这里也要取
        // 因为我们注册时 salt 传了 null，所以这里实际上是 encrypt(pwd, null) vs db_pwd
        // 但为了严谨，我们直接用注册时的逻辑
        String inputPwd = Md5Util.encrypt(loginDTO.getPassword(), null);

        if (!user.getPassword().equals(inputPwd)) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 校验状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 4. 生成 Token
        // role是byte，转int
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), (int) user.getRole());

        // 5. 封装返回结果 VO
        UserLoginVO vo = new UserLoginVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole((int) user.getRole());
        vo.setToken(token);

        return vo;
    }
}