package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haoxiang.mall.core.entity.OrderItem;
import com.haoxiang.mall.core.entity.vo.OrderItemVO;

import java.util.List;


public interface OrderItemMapper extends BaseMapper<OrderItem> {
    List<OrderItemVO> selectByOrderId(Long orderId);

    List<OrderItemVO> selectByOrderIds(List<Long> orderIds);
}
