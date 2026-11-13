package com.haoxiang.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.Coupon;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.OrderItem;
import com.haoxiang.mall.core.entity.vo.OrderItemVO;
import com.haoxiang.mall.core.entity.vo.OrderVO;
import com.haoxiang.mall.core.service.CouponUserService;
import com.haoxiang.mall.core.service.OrderItemService;
import com.haoxiang.mall.core.service.OrderService;
import com.haoxiang.mall.enums.OrderStatusEnum;
import com.haoxiang.mall.enums.PayStatusEnum;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.util.MyBeanUtil;
import com.haoxiang.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台管理-订单管理
 */
@RestController
@RequestMapping("admin/orders")
public class MallOrderManagerController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CouponUserService couponUserService;

    @GetMapping
    public R index(HttpServletRequest request) {
        request.setAttribute("path", "orders");
        return pageData(request);
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    @ResponseBody
    public IPage<Order> list(OrderVO orderVO, HttpServletRequest request) {
        Page<Order> page = getPage(request);
        return orderService.selectPage(page, orderVO);
    }

    @ResponseBody
    @GetMapping("order-items/{orderId}")
    public R getOrderItems(@PathVariable("orderId") Long orderId) {
        R success = R.success();
        List<OrderItem> orderItems = orderItemService.list(new QueryWrapper<OrderItem>().eq("order_id", orderId));
        List<OrderItemVO> orderItemVOS = MyBeanUtil.copyList(orderItems, OrderItemVO.class);
        Coupon coupon = couponUserService.getCoupon(orderId);
        if (coupon != null) {
            success.add("discount", coupon.getDiscount());
        }
        return success.add("data", orderItemVOS);
    }

    @ResponseBody
    @PostMapping("update")
    public R update(@RequestBody Order order) {
        Order order1 = orderService.getById(order.getOrderId());
        if (order1 == null) {
            throw new BusinessException("未查询到该订单");
        }
        if (order1.getOrderStatus() > OrderStatusEnum.OREDER_PACKAGED.getOrderStatus()
                || order1.getOrderStatus() < OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            throw new BusinessException("当前订单无法更改");

        }
        order.setVersion(order1.getVersion());
        return R.result(orderService.updateById(order), "订单已发生变化，请刷新后重试");
    }


    /**
     * 配货
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @PostMapping("checkDone")
    @Transactional(rollbackFor = Exception.class)
    public R checkDone(@RequestBody List<Long> ids) {
        List<Long> updateOrderIds = new ArrayList<>();
        List<Order> orders = orderService.listByIds(ids);
        for (Order order : orders) {
            if (order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()
                    || order.getOrderStatus() != OrderStatusEnum.OREDER_PAID.getOrderStatus()) {
                throw new BusinessException("编号：" + order.getOrderNo() + " 订单未支付，不可配货");
            }
            updateOrderIds.add(order.getOrderId());
        }
        if (updateOrderIds.size() != ids.size()) throw new BusinessException("订单不存在或重复");
        for (Order order : orders) {
            order.setOrderStatus((byte) OrderStatusEnum.OREDER_PACKAGED.getOrderStatus());
            if (!orderService.updateById(order)) throw new BusinessException("订单状态已发生变化，请刷新后重试");
        }
        return R.success();
    }

    /**
     * 出库
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @PostMapping("checkOut")
    @Transactional(rollbackFor = Exception.class)
    public R checkOut(@RequestBody List<Long> ids) {
        List<Long> updateOrderIds = new ArrayList<>();
        List<Order> orders = orderService.listByIds(ids);
        for (Order order : orders) {
            if (order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()
                    || order.getOrderStatus() != OrderStatusEnum.OREDER_PACKAGED.getOrderStatus()) {
                throw new BusinessException("编号：" + order.getOrderNo() + " 订单未配货，不可出库");
            }
            updateOrderIds.add(order.getOrderId());
        }
        if (updateOrderIds.size() != ids.size()) throw new BusinessException("订单不存在或重复");
        for (Order order : orders) {
            order.setOrderStatus((byte) OrderStatusEnum.OREDER_EXPRESS.getOrderStatus());
            if (!orderService.updateById(order)) throw new BusinessException("订单状态已发生变化，请刷新后重试");
        }
        return R.success();
    }

    /**
     * 关闭订单
     *
     * @param ids
     * @return
     */
    @ResponseBody
    @PostMapping("close")
    @Transactional(rollbackFor = Exception.class)
    public R close(@RequestBody List<Long> ids) {
        List<Long> updateOrderIds = new ArrayList<>();
        List<Order> orders = orderService.listByIds(ids);
        for (Order order : orders) {
            if (order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()
                    || order.getOrderStatus() == OrderStatusEnum.ORDER_SUCCESS.getOrderStatus()) {
                throw new BusinessException("编号：" + order.getOrderNo() + " 订单已关闭");
            }
            if (order.getOrderStatus() != OrderStatusEnum.OREDER_EXPRESS.getOrderStatus()) {
                throw new BusinessException("编号：" + order.getOrderNo() + " 订单未出库，不可关闭");
            }
            updateOrderIds.add(order.getOrderId());
        }
        if (updateOrderIds.size() != ids.size()) throw new BusinessException("订单不存在或重复");
        for (Order order : orders) {
            order.setOrderStatus((byte) OrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
            if (!orderService.updateById(order)) throw new BusinessException("订单状态已发生变化，请刷新后重试");
        }
        return R.success();
    }

}
