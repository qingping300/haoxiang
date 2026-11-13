package com.haoxiang.mall.controller.mall;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.Coupon;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.OrderItem;
import com.haoxiang.mall.core.entity.vo.*;
import com.haoxiang.mall.core.service.CouponUserService;
import com.haoxiang.mall.core.service.OrderItemService;
import com.haoxiang.mall.core.service.OrderService;
import com.haoxiang.mall.core.service.ShopCatService;
import com.haoxiang.mall.enums.OrderStatusEnum;
import com.haoxiang.mall.enums.PayStatusEnum;
import com.haoxiang.mall.enums.PayTypeEnum;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.redis.RedisCache;
import com.haoxiang.mall.util.MyBeanUtil;
import com.haoxiang.mall.util.R;
import com.haoxiang.mall.util.security.Md5Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 前台商城-订单
 */
@Slf4j
@RestController
public class MallOrderController extends BaseController {

    @Autowired
    private ShopCatService shopCatService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CouponUserService couponUserService;

    @Autowired
    private RedisCache redisCache;


    @ResponseBody
    @GetMapping("saveOrder")
    public R saveOrder(Long couponUserId, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<ShopCatVO> shopcatVOList = shopCatService.getShopCatVOList(mallUserVO.getUserId());
        // 购物车中无数据则跳转至错误页
        if (CollectionUtils.isEmpty(shopcatVOList)) {
            throw new BusinessException("购物车中无数据");
        }
        String orderNo = orderService.saveOrder(mallUserVO, couponUserId, shopcatVOList);
        return R.success().add("orderNo", orderNo);
    }

    @ResponseBody
    @GetMapping("seckillSaveOrder/{seckillSuccessId}/{seckillSecretKey}")
    public R seckillSaveOrder(@PathVariable Long seckillSuccessId,
                              @PathVariable String seckillSecretKey,
                              HttpSession session) {
        if (seckillSecretKey == null || !seckillSecretKey.equals(Md5Utils.hash(seckillSuccessId + Constants.SECKILL_ORDER_SALT))) {
            throw new BusinessException("秒杀商品下单不合法");
        }
        MallUserVO userVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        String orderNo = orderService.seckillSaveOrder(seckillSuccessId, userVO);
        return R.success().add("orderNo", orderNo);
    }

    @ResponseBody
    @GetMapping("/saveOrder/result/{orderNo}")
    public R saveOrderResult(@PathVariable("orderNo") String orderNo, HttpSession session) {
        MallUserVO userVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        if (userVO == null || userVO.getUserId() == null) {
            throw new BusinessException("请先登录");
        }
        Order order = orderService.getOrderByOrderNo(orderNo);
        if (order != null) {
            if (!userVO.getUserId().equals(order.getUserId())) {
                throw new BusinessException("当前订单用户异常");
            }
            return R.success().add("status", "SUCCESS");
        }
        String ownerId = redisCache.getCacheObject(Constants.SAVE_ORDER_USER_ID_KEY + orderNo);
        if (!userVO.getUserId().toString().equals(ownerId)) {
            throw new BusinessException("当前订单用户异常");
        }
        String result = redisCache.getCacheObject(Constants.SAVE_ORDER_RESULT_KEY + orderNo);
        if (result != null && !Constants.SUCCESS.equals(result)) {
            return R.error("下单失败，请重试").add("status", "FAILED");
        }
        return R.error("正在处理").add("status", "PROCESSING");
    }

    @GetMapping("/orders/{orderNo}")
    public R orderDetailPage(HttpServletRequest request, @PathVariable("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        LambdaQueryWrapper<Order> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Order::getOrderNo, orderNo);
        Order order = orderService.getOne(queryWrapper);
        if (order == null || !order.getUserId().equals(mallUserVO.getUserId())) {
            throw new BusinessException("订单项异常");
        }
        return renderOrderDetail(request, order);
    }


    @GetMapping("/orders")
    public R orderListPage(HttpServletRequest request, HttpSession session) {
        Page<OrderListVO> page = getPage(request, Constants.ORDER_SEARCH_PAGE_LIMIT);
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = new Order();
        order.setUserId(mallUserVO.getUserId());
        IPage<OrderListVO> iPage = orderService.selectMyOrderPage(page, order);
        List<OrderListVO> orderListVOS = iPage.getRecords();
        for (OrderListVO orderListVO : orderListVOS) {
            orderListVO.setOrderStatusString(OrderStatusEnum.getOrderStatusEnumByStatus(orderListVO.getOrderStatus()).getName());
        }
        List<Long> longs = orderListVOS.stream().map(OrderListVO::getOrderId).collect(Collectors.toList());
        List<OrderItem> orderItems = orderItemService.list(new QueryWrapper<OrderItem>().in(CollectionUtils.isNotEmpty(longs), "order_id", longs));
        Map<Long, List<OrderItem>> longListMap = orderItems.stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        for (OrderListVO orderListVO : orderListVOS) {
            if (longListMap.containsKey(orderListVO.getOrderId())) {
                List<OrderItem> orderItemList = longListMap.get(orderListVO.getOrderId());
                List<OrderItemVO> itemVOList = MyBeanUtil.copyList(orderItemList, OrderItemVO.class);
                orderListVO.setHaoxiangMallOrderItemVOS(itemVOList);
            }
        }
        request.setAttribute("orderPageResult", iPage);
        request.setAttribute("path", "orders");
        return pageData(request);
    }


    @PutMapping("/orders/{orderNo}/cancel")
    @ResponseBody
    public R cancelOrder(@PathVariable("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.judgeOrderUserId(orderNo, mallUserVO.getUserId());
        // 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.OREDER_PAID.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()) {
            throw new BusinessException("订单无法关闭");
        }
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_CLOSED_BY_MALLUSER.getOrderStatus());
        return R.result(orderService.updateById(order), "订单状态修改异常");
    }

    @PutMapping("/orders/{orderNo}/finish")
    @ResponseBody
    public R finishOrder(@PathVariable("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.judgeOrderUserId(orderNo, mallUserVO.getUserId());
        // 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.OREDER_EXPRESS.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_SUCCESS.getPayStatus()) {
            throw new BusinessException("订单结算异常");
        }
        order.setOrderStatus((byte) OrderStatusEnum.ORDER_SUCCESS.getOrderStatus());
        return R.result(orderService.updateById(order), "订单状态更新异常");
    }

    /**
     * 返回订单详情数据
     *
     * @param request 请求对象
     * @param order   订单详情
     * @return 订单详情数据
     */
    private R renderOrderDetail(HttpServletRequest request, Order order) {
        List<OrderItem> orderItems = orderItemService.list(new QueryWrapper<OrderItem>().eq("order_id", order.getOrderId()));
        if (CollectionUtils.isEmpty(orderItems)) {
            throw new BusinessException("订单项异常");
        }
        List<OrderItemVO> itemVOList = MyBeanUtil.copyList(orderItems, OrderItemVO.class);
        OrderDetailVO orderDetailVO = new OrderDetailVO();
        BeanUtils.copyProperties(order, orderDetailVO);
        orderDetailVO.setOrderStatusString(OrderStatusEnum.getOrderStatusEnumByStatus(orderDetailVO.getOrderStatus()).getName());
        orderDetailVO.setPayTypeString(PayTypeEnum.getPayTypeEnumByType(orderDetailVO.getPayType()).getName());
        orderDetailVO.setHaoxiangMallOrderItemVOS(itemVOList);
        request.setAttribute("orderDetailVO", orderDetailVO);
        Coupon coupon = couponUserService.getCoupon(order.getOrderId());
        if (coupon != null) {
            request.setAttribute("discount", coupon.getDiscount());
        }
        return pageData(request);
    }

}
