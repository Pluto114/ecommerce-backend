package com.sc.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sc.mall.common.Result;
import com.sc.mall.common.utils.SecurityUtils;
import com.sc.mall.entity.Banner;
import com.sc.mall.entity.dto.BannerDTO;
import com.sc.mall.entity.vo.BannerVO;
import com.sc.mall.service.IBannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "6. 首页模块", description = "轮播图等首页资源")
@RestController
@RequestMapping("/home")
public class HomeController {

    @Resource
    private IBannerService bannerService;

    // ================== 公开接口 ==================

    @Operation(summary = "获取首页轮播图 (公开)")
    @GetMapping("/public/banners")
    public Result<List<BannerVO>> banners() {
        LambdaQueryWrapper<Banner> w = new LambdaQueryWrapper<>();
        w.eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSort)
                .orderByDesc(Banner::getCreateTime);

        List<BannerVO> list = bannerService.list(w).stream().map(b -> {
            BannerVO vo = new BannerVO();
            vo.setId(b.getId());
            vo.setImgUrl(b.getImgUrl());
            vo.setLinkUrl(""); // 暂不做链接
            return vo;
        }).toList();

        return Result.success(list);
    }

    // ================== 超管接口 ==================

    private void onlyAdmin() {
        // 最简单的兜底：你们目前 SecurityUtils 没有 getRole，这里建议用“路由权限控制”解决（见下文 SecurityConfig）
        // 如果你已经在 SecurityConfig 里加了 /home/banner/** hasRole('ADMIN')，这里可以不写。
        // 若想保守一点，可临时在这里拒绝非超管（需要你实现 getRole 或从 authorities 判断）。
    }

    @Operation(summary = "发布轮播图 (超管)")
    @PostMapping("/banner/add")
    public Result<Banner> add(@RequestBody @Valid BannerDTO dto) {
        Banner b = new Banner();
        BeanUtils.copyProperties(dto, b);

        if (b.getStatus() == null) b.setStatus((byte) 1);
        if (b.getSort() == null) b.setSort(0);

        b.setCreateTime(LocalDateTime.now());
        b.setUpdateTime(LocalDateTime.now());

        bannerService.save(b);
        return Result.success(b);
    }

    @Operation(summary = "轮播图列表 (超管)")
    @GetMapping("/banner/list")
    public Result<List<Banner>> list() {
        LambdaQueryWrapper<Banner> w = new LambdaQueryWrapper<>();
        w.orderByAsc(Banner::getSort).orderByDesc(Banner::getCreateTime);
        return Result.success(bannerService.list(w));
    }

    @Operation(summary = "修改轮播图 (超管)")
    @PutMapping("/banner/update")
    public Result<String> update(@RequestBody @Valid BannerDTO dto) {
        if (dto.getId() == null) return Result.error("id不能为空");

        Banner b = new Banner();
        BeanUtils.copyProperties(dto, b);
        b.setUpdateTime(LocalDateTime.now());

        bannerService.updateById(b);
        return Result.success("修改成功");
    }

    @Operation(summary = "删除轮播图 (超管)")
    @DeleteMapping("/banner/{id}")
    public Result<String> delete(@PathVariable Long id) {
        bannerService.removeById(id);
        return Result.success("删除成功");
    }
}
