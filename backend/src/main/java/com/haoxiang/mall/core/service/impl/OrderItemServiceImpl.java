package com.haoxiang.mall.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.haoxiang.mall.core.mapper.OrderItemMapper;
import com.haoxiang.mall.core.entity.OrderItem;
import com.haoxiang.mall.core.service.OrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
    @Override
    public List<OrderItem> selectItemsByOrderId(Long orderId) {
        LambdaQueryWrapper<OrderItem> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(OrderItem::getOrderId, orderId);
        return this.list(queryWrapper);
    }
}
