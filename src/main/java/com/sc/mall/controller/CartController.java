package com.sc.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sc.mall.common.Result;
import com.sc.mall.common.utils.SecurityUtils;
import com.sc.mall.entity.Cart;
import com.sc.mall.entity.Product;
import com.sc.mall.entity.dto.CartDTO;
import com.sc.mall.entity.vo.CartVO;
import com.sc.mall.service.ICartService;
import com.sc.mall.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "5. 购物车模块", description = "前端用户专用")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Resource
    private ICartService cartService;

    @Resource
    private IProductService productService;

    @Operation(summary = "加入购物车")
    @PostMapping("/add")
    public Result<String> add(@RequestBody @Valid CartDTO cartDTO) {
        if (cartDTO.getProductId() == null) {
            return Result.error("商品ID不能为空");
        }
        Long userId = SecurityUtils.getUserId();

        // 1. 检查该用户是否已经加购过该商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getProductId, cartDTO.getProductId());

        Cart existCart = cartService.getOne(wrapper);

        if (existCart != null) {
            // 2. 如果存在，则数量累加
            existCart.setQuantity(existCart.getQuantity() + cartDTO.getQuantity());
            cartService.updateById(existCart);
        } else {
            // 3. 如果不存在，则新增
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setProductId(cartDTO.getProductId());
            newCart.setQuantity(cartDTO.getQuantity());
            cartService.save(newCart);
        }

        return Result.success("加购成功");
    }

    @Operation(summary = "查看我的购物车")
    @GetMapping("/list")
    public Result<List<CartVO>> list() {
        Long userId = SecurityUtils.getUserId();

        // 1. 查出购物车所有记录
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.orderByDesc(Cart::getCreateTime);
        List<Cart> cartList = cartService.list(wrapper);

        if (cartList.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 2. 批量查询商品信息 (为了性能，避免在循环里查库)
        // 提取所有 productId
        List<Long> productIds = cartList.stream().map(Cart::getProductId).collect(Collectors.toList());
        // 查出所有商品
        List<Product> products = productService.listByIds(productIds);
        // 转成 Map<ProductId, Product> 方便查找
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 3. 组装 VO
        List<CartVO> voList = cartList.stream().map(cart -> {
            CartVO vo = new CartVO();
            vo.setId(cart.getId());
            vo.setProductId(cart.getProductId());
            vo.setQuantity(cart.getQuantity());

            Product product = productMap.get(cart.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductImage(product.getMainImageUrl()); // 假设你在ProductDTO里加了这个，或者是 image_url
                vo.setPrice(product.getPrice());
                // 判断有效性：商品存在且状态为1(上架)
                vo.setValid(product.getStatus() == 1);
            } else {
                vo.setProductName("商品已失效");
                vo.setValid(false);
            }
            return vo;
        }).collect(Collectors.toList());

        return Result.success(voList);
    }

    @Operation(summary = "修改购物车数量")
    @PutMapping("/update")
    public Result<String> updateQuantity(@RequestBody @Valid CartDTO cartDTO) {
        if (cartDTO.getId() == null) {
            return Result.error("购物车ID不能为空");
        }
        // 简单的权限校验，防止改别人的
        Cart cart = cartService.getById(cartDTO.getId());
        if (cart == null || !cart.getUserId().equals(SecurityUtils.getUserId())) {
            return Result.error("记录不存在");
        }

        cart.setQuantity(cartDTO.getQuantity());
        cartService.updateById(cart);
        return Result.success("更新成功");
    }

    @Operation(summary = "移除购物车商品")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Cart cart = cartService.getById(id);
        if (cart != null && cart.getUserId().equals(SecurityUtils.getUserId())) {
            cartService.removeById(id);
        }
        return Result.success("移除成功");
    }
}