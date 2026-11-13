package com.haoxiang.mall.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import com.haoxiang.mall.core.mapper.GoodsCategoryMapper;
import com.haoxiang.mall.core.entity.GoodsCategory;
import com.haoxiang.mall.core.entity.vo.GoodsCategoryVO;
import com.haoxiang.mall.core.service.GoodsCategoryService;
import com.haoxiang.mall.util.MyBeanUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements GoodsCategoryService {

    private GoodsCategoryMapper goodsCategoryMapper;

    @Override
    public IPage<GoodsCategory> selectPage(Page<GoodsCategory> page, GoodsCategory goodsCategory) {
        return goodsCategoryMapper.selectListPage(page, goodsCategory);
    }

    @Override
    public List<GoodsCategoryVO> treeList() {
        // 查询所有分类
        List<GoodsCategory> list = list(new QueryWrapper<GoodsCategory>().eq("is_deleted", 0));
        List<GoodsCategoryVO> voList = MyBeanUtil.copyList(list, GoodsCategoryVO.class);
        List<GoodsCategoryVO> root = new ArrayList<>();
        for (GoodsCategoryVO goodsCategoryVO : voList) {
            // 添加一级分类
            if (goodsCategoryVO.getParentId() == 0) {
                root.add(goodsCategoryVO);
            }
            Long id = goodsCategoryVO.getCategoryId();
            List<GoodsCategoryVO> subList = new ArrayList<>();
            for (GoodsCategoryVO categoryVO : voList) {
                // 添加二级或者三级分类
                if (categoryVO.getParentId().equals(id)) {
                    subList.add(categoryVO);
                    goodsCategoryVO.setSubCategoryVOS(subList);
                }
            }
        }
        return root.subList(0, 10);
    }
}
