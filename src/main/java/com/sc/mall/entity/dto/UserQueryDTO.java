package com.sc.mall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户列表查询参数")
public class UserQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "用户名(模糊查询)")
    private String username;

    @Schema(description = "角色筛选: 1-超管, 2-信管, 3-用户")
    private Integer role;
}