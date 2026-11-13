package com.haoxiang.mall.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.IService;
import com.haoxiang.mall.core.entity.GoodsCategory;
import com.haoxiang.mall.core.entity.vo.GoodsCategoryVO;

import java.util.List;

public interface GoodsCategoryService extends IService<GoodsCategory> {

    /**
     * 分页查询
     * @param page 分页对象
     * @param goodsCategory 商品分类对象
     * @return 分页数据
     */
    IPage<GoodsCategory> selectPage(Page<GoodsCategory> page, GoodsCategory goodsCategory);

    /**
     * 查询商品三级分类树集合
     * @return
     */
    List<GoodsCategoryVO> treeList();

}
