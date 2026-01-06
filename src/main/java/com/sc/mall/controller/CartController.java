package com.sc.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sc.mall.common.Result;
import com.sc.mall.common.utils.SecurityUtils;
import com.sc.mall.entity.Cart;
import com.sc.mall.entity.dto.CartDTO;
import com.sc.mall.entity.vo.CartVO;
import com.sc.mall.mapper.CartMapper;
import com.sc.mall.service.ICartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "5. 购物车模块", description = "前端用户专用")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Resource
    private ICartService cartService;

    // 注入 Mapper 以使用刚才写的 selectCartList 方法
    @Resource
    private CartMapper cartMapper;

    @Operation(summary = "加入购物车")
    @PostMapping("/add")
    public Result<String> add(@RequestBody @Valid CartDTO cartDTO) {
        // 1. 校验参数
        if (cartDTO.getProductId() == null) {
            return Result.error("商品ID不能为空");
        }

        Long userId = SecurityUtils.getUserId();

        // 2. 查库：看该用户是否已经加购过该商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.eq(Cart::getProductId, cartDTO.getProductId());

        Cart existCart = cartService.getOne(wrapper);

        if (existCart != null) {
            // 3. 存在：累加数量
            existCart.setQuantity(existCart.getQuantity() + cartDTO.getQuantity());
            cartService.updateById(existCart);
        } else {
            // 4. 不存在：插入新记录
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setProductId(cartDTO.getProductId());
            newCart.setQuantity(cartDTO.getQuantity());
            newCart.setCreateTime(LocalDateTime.now());
            cartService.save(newCart);
        }

        return Result.success("已加入购物车");
    }

    @Operation(summary = "查看我的购物车")
    @GetMapping("/list")
    public Result<List<CartVO>> list() {
        Long userId = SecurityUtils.getUserId();

        // 使用 XML 中写的联表查询，一步到位获取前端所需的所有字段
        List<CartVO> list = cartMapper.selectCartList(userId);

        return Result.success(list);
    }

    @Operation(summary = "修改购物车数量")
    @PutMapping("/update")
    public Result<String> updateQuantity(@RequestBody @Valid CartDTO cartDTO) {
        // 这里的 ID 指的是购物车记录的 ID (tb_cart.id)
        if (cartDTO.getId() == null) {
            return Result.error("购物车ID不能为空");
        }

        Cart cart = cartService.getById(cartDTO.getId());

        // 安全检查：防止越权修改别人的购物车
        if (cart == null || !cart.getUserId().equals(SecurityUtils.getUserId())) {
            return Result.error("记录不存在或无权修改");
        }

        // 更新数量
        cart.setQuantity(cartDTO.getQuantity());
        cartService.updateById(cart);

        return Result.success("数量已更新");
    }

    @Operation(summary = "移除购物车商品")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        // 先查询，确保删除的是自己的记录
        Cart cart = cartService.getById(id);
        if (cart != null && cart.getUserId().equals(SecurityUtils.getUserId())) {
            cartService.removeById(id);
        }
        return Result.success("移除成功");
    }
}