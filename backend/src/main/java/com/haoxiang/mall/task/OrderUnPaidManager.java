package com.haoxiang.mall.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.core.mapper.GoodsMapper;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.OrderItem;
import com.haoxiang.mall.core.service.CouponService;
import com.haoxiang.mall.core.service.CacheService;
import com.haoxiang.mall.core.service.OrderItemService;
import com.haoxiang.mall.core.service.OrderService;
import com.haoxiang.mall.core.service.SeckillService;
import com.haoxiang.mall.enums.OrderStatusEnum;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.redis.RedisCache;
import com.haoxiang.mall.util.spring.SpringContextUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class OrderUnPaidManager {

    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private CouponService couponService;
    @Resource
    private CacheService cacheService;
    @Resource
    private PlatformTransactionManager platformTransactionManager;

    public void doUnPaidTask(Long orderId) {
        // 启用编程式事务
        // 1. 在开启事务钱查询订单是否存在
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new BusinessException(String.format("订单不存在，orderId:%s", orderId));
        }
        if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()) {
            throw new BusinessException(String.format("订单状态错误，order:%s", order));
        }
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setTimeout(30);
        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
        try {
            // 2. 设置订单为已取消状态
            order.setOrderStatus((byte) OrderStatusEnum.ORDER_CLOSED_BY_EXPIRED.getOrderStatus());
            order.setUpdateTime(new Date());
            if (!orderService.updateById(order)) {
                throw new BusinessException("更新数据已失效");
            }
            // 3. 商品货品数量增加
            LambdaQueryWrapper<OrderItem> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> orderItems = orderItemService.list(queryWrapper);
            for (OrderItem orderItem : orderItems) {
                if (orderItem.getSeckillId() != null) { // 秒杀单商品项处理
                    Long seckillId = orderItem.getSeckillId();
                    SeckillService seckillService = SpringContextUtil.getBean(SeckillService.class);
                    RedisCache redisCache = SpringContextUtil.getBean(RedisCache.class);
                    if (!seckillService.addStock(seckillId)) {
                        throw new BusinessException("秒杀商品货品库存增加失败");
                    }
                    redisCache.increment(Constants.SECKILL_GOODS_STOCK_KEY + seckillId);
                    redisCache.deleteCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, order.getUserId());
                } else { // 普通单商品项处理
                    Long goodsId = orderItem.getGoodsId();
                    Integer goodsCount = orderItem.getGoodsCount();
                    if (!goodsMapper.addStock(goodsId, goodsCount)) {
                        throw new BusinessException("秒杀商品货品库存增加失败");
                    }
                }
            }

            // 4. 返还优惠券
            couponService.releaseCoupon(orderId);
            // 5. 所有更新操作完成后，提交事务
            platformTransactionManager.commit(transaction);
            for (OrderItem item : orderItems) {
                if (item.getSeckillId() == null) {
                    try { cacheService.invalidate("goods:" + item.getGoodsId()); }
                    catch (RuntimeException e) { log.error("商品缓存失效失败，goodsId={}", item.getGoodsId(), e); }
                }
            }
            log.info("---------------订单orderId:{},未支付超时取消成功", orderId);
        } catch (Exception e) {
            log.info("---------------订单orderId:{},未支付超时取消失败", orderId, e);
            // 6. 发生异常，回滚事务
            platformTransactionManager.rollback(transaction);
        }
    }

}
