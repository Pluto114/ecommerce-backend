package com.sc.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sc.mall.common.Result;
import com.sc.mall.common.utils.SecurityUtils;
import com.sc.mall.entity.Shop;
import com.sc.mall.entity.dto.ShopDTO;
import com.sc.mall.service.IShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "3. 商店管理模块", description = "信息管理员专用：管理自己的商店")
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    private IShopService shopService;

    @Operation(summary = "查询我的商店列表")
    @GetMapping("/my-list")
    public Result<List<Shop>> getMyShopList() {
        Long currentUserId = SecurityUtils.getUserId();

        // 核心逻辑：只查 admin_id = 当前用户 的数据
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Shop::getAdminId, currentUserId);
        wrapper.orderByDesc(Shop::getCreateTime);

        List<Shop> list = shopService.list(wrapper);
        return Result.success(list);
    }

    @Operation(summary = "创建商店")
    @PostMapping("/add")
    public Result<Shop> addShop(@RequestBody @Valid ShopDTO shopDTO) {
        Long currentUserId = SecurityUtils.getUserId();

        Shop shop = new Shop();
        BeanUtils.copyProperties(shopDTO, shop);

        // 关键：绑定归属人
        shop.setAdminId(currentUserId);
        // 默认状态启用
        shop.setStatus((byte) 1);

        shopService.save(shop);
        return Result.success(shop);
    }

    @Operation(summary = "修改商店信息")
    @PutMapping("/update")
    public Result<String> updateShop(@RequestBody @Valid ShopDTO shopDTO) {
        if (shopDTO.getId() == null) {
            return Result.error("商店ID不能为空");
        }

        // 安全检查：确保要修改的商店属于当前登录用户
        Shop dbShop = shopService.getById(shopDTO.getId());
        if (dbShop == null) {
            return Result.error("商店不存在");
        }
        Long currentUserId = SecurityUtils.getUserId();
        if (!dbShop.getAdminId().equals(currentUserId)) {
            return Result.error(403, "你无权修改别人的商店！");
        }

        // 执行修改
        Shop updateShop = new Shop();
        BeanUtils.copyProperties(shopDTO, updateShop);
        // 防止被篡改关键字段
        updateShop.setAdminId(null);

        shopService.updateById(updateShop);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除商店")
    @DeleteMapping("/{id}")
    public Result<String> deleteShop(@PathVariable Long id) {
        Shop dbShop = shopService.getById(id);
        if (dbShop == null) {
            return Result.success("删除成功"); // 幂等性
        }

        // 安全检查
        Long currentUserId = SecurityUtils.getUserId();
        if (!dbShop.getAdminId().equals(currentUserId)) {
            return Result.error(403, "你无权删除别人的商店！");
        }

        shopService.removeById(id);
        return Result.success("删除成功");
    }
}