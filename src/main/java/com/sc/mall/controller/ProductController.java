package com.sc.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sc.mall.common.Result;
import com.sc.mall.common.utils.SecurityUtils;
import com.sc.mall.entity.Product;
import com.sc.mall.entity.Shop;
import com.sc.mall.entity.dto.ProductDTO;
import com.sc.mall.service.IProductService;
import com.sc.mall.service.IShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.sc.mall.entity.Category;
import com.sc.mall.entity.dto.group.AddGroup;
import com.sc.mall.entity.dto.group.UpdateGroup;
import com.sc.mall.service.ICategoryService;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Tag(name = "4. 商品管理模块", description = "包含商家管理和用户搜索")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Resource
    private ICategoryService categoryService;

    @Resource
    private IProductService productService;

    @Resource
    private IShopService shopService;

    // ================== 商家功能 ==================

    /**
     * ✅ 多店铺改造原则：
     * 商家端所有商品操作必须绑定“当前选择店铺”，店铺ID从请求头 X-Shop-Id 获取
     * 前端不允许再通过 body 传 shopId 来指定归属（避免越权/错店操作）
     */

    @Operation(summary = "发布商品 (商家，按当前店铺)")
    @PostMapping("/add")
    public Result<Product> addProduct(
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            @RequestBody @Validated(AddGroup.class) ProductDTO productDTO) {

        // 0. 必须选择店铺
        if (shopId == null) {
            return Result.error(400, "缺少X-Shop-Id，请先选择店铺");
        }

        // 1. 校验商店归属权（当前登录用户必须是该店铺 owner）
        Shop shop = shopService.getById(shopId);
        if (shop == null) return Result.error("商店不存在");

        Long currentUserId = SecurityUtils.getUserId();
        if (!Objects.equals(shop.getAdminId(), currentUserId)) {
            return Result.error(403, "你无权向别人的商店发布商品");
        }

        // 2. 校验分类是否存在且启用
        Category category = categoryService.getById(productDTO.getCategoryId());
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            return Result.error("分类不存在或未启用");
        }

        // 3. 保存商品（强制绑定到当前 shopId）
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        // ✅ 关键：归属店铺只能来自 X-Shop-Id
        product.setShopId(shopId);

        if (product.getStatus() == null) product.setStatus((byte) 1);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());

        productService.save(product);
        return Result.success(product);
    }

    @Operation(summary = "修改商品信息 (商家，按当前店铺)")
    @PutMapping("/update")
    public Result<String> updateProduct(
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            @RequestBody @Validated(UpdateGroup.class) ProductDTO productDTO) {

        if (shopId == null) {
            return Result.error(400, "缺少X-Shop-Id，请先选择店铺");
        }

        // 1. 校验商品是否存在
        Product dbProduct = productService.getById(productDTO.getId());
        if (dbProduct == null) return Result.error("商品不存在");

        // 2. ✅ 店铺维度权限：只能改当前店铺下的商品
        if (!Objects.equals(dbProduct.getShopId(), shopId)) {
            return Result.error(403, "你无权修改非当前店铺的商品");
        }

        // 2.1 进一步校验：该 shopId 属于当前登录用户（防伪造 header）
        Shop shop = shopService.getById(shopId);
        if (shop == null) return Result.error("商店不存在");
        Long currentUserId = SecurityUtils.getUserId();
        if (!Objects.equals(shop.getAdminId(), currentUserId)) {
            return Result.error(403, "你无权修改别人的商店商品");
        }

        // 3. 如果要改分类，校验分类存在且启用
        if (productDTO.getCategoryId() != null) {
            Category category = categoryService.getById(productDTO.getCategoryId());
            if (category == null || category.getStatus() == null || category.getStatus() != 1) {
                return Result.error("分类不存在或未启用");
            }
        }

        // 4. 更新（禁止修改归属商店）
        Product updateProduct = new Product();
        BeanUtils.copyProperties(productDTO, updateProduct);

        // ✅ 禁止改店铺归属
        updateProduct.setShopId(null);
        updateProduct.setUpdateTime(LocalDateTime.now());

        productService.updateById(updateProduct);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询我的商品列表 (商家，按当前店铺) - 支持返回分类名")
    @GetMapping("/my-list")
    public Result<List<Product>> getMyProductList(
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            @RequestParam(required = false) String name) {

        if (shopId == null) {
            return Result.error(400, "缺少X-Shop-Id，请先选择店铺");
        }

        // ✅ 校验当前店铺归属
        Shop shop = shopService.getById(shopId);
        if (shop == null) return Result.error("商店不存在");
        Long currentUserId = SecurityUtils.getUserId();
        if (!Objects.equals(shop.getAdminId(), currentUserId)) {
            return Result.error(403, "你无权查看别人的商店商品");
        }

        // 1. 查当前店铺下商品（可选：name 模糊）
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getShopId, shopId);

        if (StringUtils.hasText(name)) {
            productWrapper.like(Product::getName, name.trim());
        }
        productWrapper.orderByDesc(Product::getCreateTime);

        List<Product> products = productService.list(productWrapper);
        if (products.isEmpty()) return Result.success(products);

        // 2. 补齐 categoryName（批量查分类，避免 N+1）
        List<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());

        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryService.listByIds(categoryIds);
            java.util.Map<Long, String> idNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

            products.forEach(p -> {
                if (p.getCategoryId() == null) {
                    p.setCategoryName("未分类");
                } else {
                    p.setCategoryName(idNameMap.getOrDefault(p.getCategoryId(), "未分类"));
                }
            });
        } else {
            products.forEach(p -> p.setCategoryName("未分类"));
        }

        return Result.success(products);
    }

    @Operation(summary = "删除商品 (商家，按当前店铺)")
    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(
            @RequestHeader(value = "X-Shop-Id", required = false) Long shopId,
            @PathVariable Long id) {

        if (shopId == null) {
            return Result.error(400, "缺少X-Shop-Id，请先选择店铺");
        }

        Product dbProduct = productService.getById(id);
        if (dbProduct == null) {
            return Result.success("删除成功");
        }

        // ✅ 店铺维度权限：只能删当前店铺下商品
        if (!Objects.equals(dbProduct.getShopId(), shopId)) {
            return Result.error(403, "你无权删除非当前店铺的商品");
        }

        // ✅ 校验 shop 归属（防伪造 header）
        Shop shop = shopService.getById(shopId);
        if (shop == null) return Result.error("商店不存在");
        Long currentUserId = SecurityUtils.getUserId();
        if (!Objects.equals(shop.getAdminId(), currentUserId)) {
            return Result.error(403, "你无权删除别人的商店商品");
        }

        productService.removeById(id);
        return Result.success("删除成功");
    }

    // ================== 公共功能 (前端用户/搜索) ==================

    @Operation(summary = "搜索商品 (公开接口) - 支持 keyword + categoryId")
    @GetMapping("/public/search")
    public Result<Page<Product>> search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId) {

        Page<Product> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 1) 只查上架商品
        wrapper.eq(Product::getStatus, 1);

        // 2) keyword：商品名模糊查询
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword.trim());
        }

        // 3) categoryId：分类筛选
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        return Result.success(productService.page(mpPage, wrapper));
    }

    // ================== 公共/详情接口 ==================

    @Operation(summary = "查询商品详情")
    @GetMapping("/detail/{id}")
    public Result<Product> getDetail(@PathVariable Long id) {
        Product product = productService.getById(id);

        if (product == null) {
            return Result.error("商品不存在");
        }

        return Result.success(product);
    }
}
