package com.haoxiang.mall.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import com.haoxiang.mall.core.mapper.CarouselsMapper;
import com.haoxiang.mall.core.entity.Carousels;
import com.haoxiang.mall.core.service.CarouselsService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CarouselsServiceImpl extends ServiceImpl<CarouselsMapper, Carousels> implements CarouselsService {

    private CarouselsMapper carouselsMapper;

    @Override
    public IPage<Carousels> selectPage(Page<Carousels> page, Carousels carousels) {
        return carouselsMapper.selectListPage(page, carousels);
    }
}
