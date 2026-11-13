package com.haoxiang.mall.core.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.haoxiang.mall.core.entity.OrderItem;

import java.util.List;

public interface OrderItemService extends IService<OrderItem> {
    List<OrderItem> selectItemsByOrderId(Long orderId);
}
