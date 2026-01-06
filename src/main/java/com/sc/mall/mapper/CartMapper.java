package com.sc.mall.mapper;

import com.sc.mall.entity.Cart;
import com.sc.mall.entity.vo.CartVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * <p>
 * 购物车表 Mapper 接口
 * </p>
 *
 * @author 斯聪
 * @since 2025-12-18
 */
public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 自定义查询：获取用户购物车列表（关联商品表信息）
     * @param userId 当前登录用户ID
     * @return 包含商品详情的购物车列表
     */
    List<CartVO> selectCartList(@Param("userId") Long userId);
}