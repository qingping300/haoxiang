package com.haoxiang.mall.core.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.haoxiang.mall.core.entity.ShopCat;
import com.haoxiang.mall.core.entity.vo.ShopCatVO;

import java.util.List;

public interface ShopCatService extends IService<ShopCat> {

    /**
     * 添加商品至购物车
     * @param shopCat 购物车对象
     */
    void saveShopCat(ShopCat shopCat);

    /**
     * 获取用户购物车商品详情
     * @param userId 用户ID
     * @return 商品集合
     */
    List<ShopCatVO> getShopCatVOList(Long userId);

    ShopCat selectByUserIdAndGoodsId(Long userId, Long goodsId);
}
