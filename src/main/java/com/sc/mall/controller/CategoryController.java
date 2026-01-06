package com.sc.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sc.mall.common.Result;
import com.sc.mall.entity.Category;
import com.sc.mall.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "6. 分类模块", description = "前台分类展示")
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private ICategoryService categoryService;

    @Operation(summary = "获取所有启用的一级分类 (公开)")
    @GetMapping("/public/list")
    public Result<List<Category>> listPublicCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 1)
                .eq(Category::getParentId, 0L)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId);
        return Result.success(categoryService.list(wrapper));
    }
}
