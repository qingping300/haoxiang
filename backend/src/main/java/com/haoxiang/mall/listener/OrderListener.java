package com.haoxiang.mall.listener;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.core.entity.Coupon;
import com.haoxiang.mall.core.entity.CouponUser;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.OrderItem;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.entity.vo.ShopCatVO;
import com.haoxiang.mall.core.service.*;
import com.haoxiang.mall.event.OrderEvent;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.redis.RedisCache;
import com.haoxiang.mall.task.OrderUnPaidTask;
import com.haoxiang.mall.task.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 普通订单监听器
 */
@Slf4j
@Component
public class OrderListener implements ApplicationListener<OrderEvent> {

    @Resource
    private ShopCatService shopCatService;
    @Resource
    private GoodsService goodsService;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private CouponUserService couponUserService;
    @Resource
    private CouponService couponService;
    @Resource
    private TaskService taskService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private PlatformTransactionManager platformTransactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

    @Override
    public void onApplicationEvent(OrderEvent event) {
        log.info("OrderListener orderNo:{}", event.getOrderNo());
        String orderNo = event.getOrderNo();
        if (StringUtils.isBlank(orderNo)) {
            throw new BusinessException("订单编号不存在");
        }
        Long orderId;
        // 开启事务
        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
        try {
            Long couponUserId = event.getCouponUserId();
            MallUserVO mallUserVO = event.getMallUserVO();
            List<ShopCatVO> shopcatVOList = event.getShopcatVOList();
            List<Long> cartItemIdList = shopcatVOList.stream().map(ShopCatVO::getCartItemId).collect(Collectors.toList());

            // 删除购物项
            if (!shopCatService.removeByIds(cartItemIdList)) {
                throw new BusinessException("删除购物车异常");
            }
            // 更新商品库存
            shopcatVOList.forEach(shopCatVO -> {
                if (!goodsService.reduceStock(shopCatVO.getGoodsId(), shopCatVO.getGoodsCount())) {
                    throw new BusinessException("扣减商品库存失败");
                }
            });
            Integer discountPrice = 0;
            // 查询优惠卷
            if (couponUserId != null) {
                Coupon coupon = couponUserService.getCouponByCouponUserId(couponUserId);
                if (coupon != null) {
                    discountPrice = coupon.getDiscount();
                }
            }

            // 计算总价
            int totalPrice = 0;
            for (ShopCatVO shopCatVO : shopcatVOList) {
                totalPrice += shopCatVO.getGoodsCount() * shopCatVO.getSellingPrice();
            }
            // 保存订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setTotalPrice(totalPrice - discountPrice);
            order.setUserId(mallUserVO.getUserId());
            order.setUserAddress(mallUserVO.getAddress());
            // 订单附加信息暂设为空字符串
            String extraInfo = "";
            order.setExtraInfo(extraInfo);
            // 生成订单项并保存订单项纪录
            if (!orderService.save(order)) {
                throw new BusinessException("保存订单异常");
            }
            orderId = order.getOrderId();
            // 生成所有的订单项快照，并保存至数据库
            List<OrderItem> orderItems = shopcatVOList.stream().map(shopCatVO -> {
                OrderItem orderItem = new OrderItem();
                BeanUtils.copyProperties(shopCatVO, orderItem);
                orderItem.setOrderId(orderId);
                return orderItem;
            }).collect(Collectors.toList());

            if (!orderItemService.saveBatch(orderItems)) {
                throw new BusinessException("保存订单内部异常");
            }
            // 如果使用了优惠卷，则更新优惠卷状态
            if (couponUserId != null) {
                CouponUser couponUser = couponUserService.getById(couponUserId);
                if (couponUser == null || !mallUserVO.getUserId().equals(couponUser.getUserId()) || couponUser.getStatus() != 0) {
                    throw new BusinessException("优惠券不可使用");
                }
                couponUser.setStatus((byte) 1);
                couponUser.setUsedTime(new Date());
                couponUser.setOrderId(order.getOrderId());
                if (!couponUserService.updateById(couponUser)) throw new BusinessException("优惠券状态已发生变化");
            }
            platformTransactionManager.commit(transaction);
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            if (!transaction.isCompleted()) {
                platformTransactionManager.rollback(transaction);
            }
            throw e;
        }
        try {
            // 订单支付超期任务
            taskService.addTask(new OrderUnPaidTask(orderId));
            redisCache.setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + orderNo, Constants.SUCCESS, 10, TimeUnit.MINUTES);
        } catch (RuntimeException e) {
            // 订单已经提交，结果查询以数据库为准。
            log.error("订单已创建，提交后状态同步失败，orderNo:{}", orderNo, e);
        }

    }
}
