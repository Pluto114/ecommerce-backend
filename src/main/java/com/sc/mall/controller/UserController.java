package com.sc.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.mall.common.Result;
import com.sc.mall.common.utils.Md5Util;
import com.sc.mall.entity.User;
import com.sc.mall.entity.dto.UserQueryDTO;
import com.sc.mall.entity.dto.UserRegisterDTO;
import com.sc.mall.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户管理控制器 (超级管理员权限)
 * </p>
 *
 * @author 斯聪
 * @since 2025-05-29
 */
@Tag(name = "2. 用户管理模块", description = "超级管理员专用：增删改查用户")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Operation(summary = "分页查询用户列表")
    @PostMapping("/list") // 使用POST方便传复杂JSON对象
    public Result<Page<User>> list(@RequestBody UserQueryDTO queryDTO) {
        // 1. 构建分页对象
        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 如果传了用户名，就模糊查询
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), User::getUsername, queryDTO.getUsername());
        // 如果传了角色，就精确筛选
        wrapper.eq(queryDTO.getRole() != null, User::getRole, queryDTO.getRole());
        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);

        // 3. 执行查询
        Page<User> result = userService.page(page, wrapper);

        // 4. 数据脱敏 (把密码设为null，防止传给前端)
        result.getRecords().forEach(u -> {
            u.setPassword(null);
            u.setSalt(null);
        });

        return Result.success(result);
    }

    @Operation(summary = "新增信息管理员")
    @PostMapping("/add")
    public Result<User> addInfoAdmin(@RequestBody UserRegisterDTO dto) {
        // 复用 Service 里的注册逻辑，但强制设置角色为 2 (信息管理员)
        dto.setRole(2);
        User user = userService.register(dto);
        return Result.success(user);
    }

    @Operation(summary = "修改用户状态 (启用/禁用)")
    @PutMapping("/status/{id}/{status}")
    public Result<String> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status.byteValue());

        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("状态更新成功");
        }
        return Result.error("更新失败，用户可能不存在");
    }

    @Operation(summary = "重置密码 (默认重置为 123456)")
    @PutMapping("/reset-password/{id}")
    public Result<String> resetPassword(@PathVariable Long id) {
        User user = new User();
        user.setId(id);
        // 重置为默认密码 123456
        user.setPassword(Md5Util.encrypt("123456", null));

        boolean success = userService.updateById(user);
        if (success) {
            return Result.success("密码已重置为: 123456");
        }
        return Result.error("操作失败");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }
}