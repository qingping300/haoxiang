package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haoxiang.mall.core.entity.GoodsCategory;

public interface GoodsCategoryMapper extends BaseMapper<GoodsCategory> {

    IPage<GoodsCategory> selectListPage(Page<GoodsCategory> page, GoodsCategory goodsCategory);
}
