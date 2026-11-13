package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.vo.DayTransactionAmountVO;
import com.haoxiang.mall.core.entity.vo.OrderListVO;
import com.haoxiang.mall.core.entity.vo.OrderVO;

import java.util.List;

public interface OrderMapper extends BaseMapper<Order> {

    IPage<OrderListVO> selectListVOPage(Page<OrderListVO> page, Order order);

    IPage<Order> selectListPage(Page<Order> page, OrderVO order);

    List<DayTransactionAmountVO> countMallTransactionAmount(Integer dayNum);
    List<Order> selectOrderIds();
}
