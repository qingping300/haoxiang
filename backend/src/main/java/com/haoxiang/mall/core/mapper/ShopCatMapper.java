package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haoxiang.mall.core.entity.ShopCat;

public interface ShopCatMapper extends BaseMapper<ShopCat> {

    IPage<ShopCat> selectListPage(Page<ShopCat> page, ShopCat shopCat);

    ShopCat selectByUserIdAndGoodsId(Long userId, Long goodsId);
}
