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
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "4. 商品管理模块", description = "包含商家管理和用户搜索")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Resource
    private IProductService productService;

    @Resource
    private IShopService shopService;

    // ================== 商家功能 ==================

    @Operation(summary = "发布商品 (商家)")
    @PostMapping("/add")
    public Result<Product> addProduct(@RequestBody @Valid ProductDTO productDTO) {
        // 1. 校验商店归属权
        Shop shop = shopService.getById(productDTO.getShopId());
        if (shop == null) {
            return Result.error("商店不存在");
        }
        Long currentUserId = SecurityUtils.getUserId();
        if (!shop.getAdminId().equals(currentUserId)) {
            return Result.error(403, "你无权向别人的商店发布商品");
        }

        // 2. 保存商品
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        // 默认为 1-在售
        if (product.getStatus() == null) {
            product.setStatus((byte) 1);
        }
        productService.save(product);
        return Result.success(product);
    }

    @Operation(summary = "修改商品信息 (商家)")
    @PutMapping("/update")
    public Result<String> updateProduct(@RequestBody @Valid ProductDTO productDTO) {
        if (productDTO.getId() == null) {
            return Result.error("商品ID不能为空");
        }

        // 1. 校验商品是否存在
        Product dbProduct = productService.getById(productDTO.getId());
        if (dbProduct == null) {
            return Result.error("商品不存在");
        }

        // 2. 校验权限 (通过商品 -> 查商店 -> 查店主)
        Shop shop = shopService.getById(dbProduct.getShopId());
        Long currentUserId = SecurityUtils.getUserId();
        if (shop != null && !shop.getAdminId().equals(currentUserId)) {
            return Result.error(403, "你无权修改别人的商品");
        }

        // 3. 更新
        Product updateProduct = new Product();
        BeanUtils.copyProperties(productDTO, updateProduct);
        // 禁止修改归属商店 (防止把商品挪到别的店)
        updateProduct.setShopId(null);

        productService.updateById(updateProduct);
        return Result.success("修改成功");
    }

    @Operation(summary = "查询我的商品列表 (商家)")
    @GetMapping("/my-list")
    public Result<List<Product>> getMyProductList() {
        Long currentUserId = SecurityUtils.getUserId();

        // 1. 先查出我名下的所有商店ID
        LambdaQueryWrapper<Shop> shopWrapper = new LambdaQueryWrapper<>();
        shopWrapper.eq(Shop::getAdminId, currentUserId);
        List<Shop> myShops = shopService.list(shopWrapper);

        if (myShops.isEmpty()) {
            return Result.success(List.of());
        }
        List<Long> shopIds = myShops.stream().map(Shop::getId).collect(Collectors.toList());

        // 2. 再查这些商店下的所有商品
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(Product::getShopId, shopIds);
        productWrapper.orderByDesc(Product::getCreateTime);

        return Result.success(productService.list(productWrapper));
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<String> deleteProduct(@PathVariable Long id) {
        Product dbProduct = productService.getById(id);
        if (dbProduct == null) {
            return Result.success("删除成功");
        }

        // 权限校验
        Shop shop = shopService.getById(dbProduct.getShopId());
        Long currentUserId = SecurityUtils.getUserId();
        if (shop != null && !shop.getAdminId().equals(currentUserId)) {
            return Result.error(403, "你无权删除此商品");
        }

        productService.removeById(id);
        return Result.success("删除成功");
    }

    // ================== 公共功能 (前端用户/搜索) ==================

    @Operation(summary = "搜索商品 (公开接口，支持分页和关键字)")
    @GetMapping("/public/search")
    public Result<Page<Product>> search(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 1. 只查上架的商品 (status=1)
        wrapper.eq(Product::getStatus, 1);

        // 2. 关键字搜索 (商品名 or 描述)
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or()
                    .like(Product::getDescription, keyword));
        }

        wrapper.orderByDesc(Product::getCreateTime);

        return Result.success(productService.page(page, wrapper));
    }

    @Operation(summary = "查看商品详情")
    @GetMapping("/public/{id}")
    public Result<Product> getDetail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null || product.getStatus() == 0) {
            // 如果商品下架了，也不让看，或者前端处理
            return Result.error("商品不存在或已下架");
        }
        return Result.success(product);
    }
}