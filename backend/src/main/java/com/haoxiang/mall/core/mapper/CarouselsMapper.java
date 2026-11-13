package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haoxiang.mall.core.entity.Carousels;

public interface CarouselsMapper extends BaseMapper<Carousels> {

    IPage<Carousels> selectListPage(Page<Carousels> page, Carousels carousels);
}
