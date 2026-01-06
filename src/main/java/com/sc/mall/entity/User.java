package com.sc.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("tb_user")
@Schema(name = "User", description = "用户表 (三合一)")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID (主键)")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "邮箱（登录账号）")
    @TableField("email")
    private String email;

    @Schema(description = "用户名/昵称（库内 UNIQUE，登录时也可使用）")
    @TableField("username")
    private String username;

    @JsonIgnore
    @Schema(description = "密码 (MD5加密)", accessMode = Schema.AccessMode.WRITE_ONLY)
    @TableField("password")
    private String password;

    @JsonIgnore
    @Schema(description = "密码盐值", accessMode = Schema.AccessMode.WRITE_ONLY)
    @TableField("salt")
    private String salt;

    @Schema(description = "用户角色: 1-超级管理员, 2-信息管理员, 3-前端用户")
    @TableField("role")
    private Byte role;

    @Schema(description = "账户状态: 0-禁用, 1-启用")
    @TableField("status")
    private Byte status;

    @Schema(description = "积分 (前端用户) - [加分项]")
    @TableField("points")
    private Integer points;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;
}
