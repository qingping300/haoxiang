package com.haoxiang.mall.listener;
/**
 * 秒杀订单监听器
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.core.entity.Goods;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.OrderItem;
import com.haoxiang.mall.core.entity.Seckill;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.service.GoodsService;
import com.haoxiang.mall.core.service.OrderItemService;
import com.haoxiang.mall.core.service.OrderService;
import com.haoxiang.mall.core.service.SeckillService;
import com.haoxiang.mall.event.SeckillOrderEvent;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.redis.RedisCache;
import com.haoxiang.mall.task.OrderUnPaidTask;
import com.haoxiang.mall.task.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SeckillOrderListener implements ApplicationListener<SeckillOrderEvent> {

    @Resource
    private GoodsService goodsService;
    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private SeckillService seckillService;
    @Resource
    private TaskService taskService;
    @Resource
    private RedisCache redisCache;
    @Resource
    private PlatformTransactionManager platformTransactionManager;
    @Resource
    private TransactionDefinition transactionDefinition;

    @Override
    public void onApplicationEvent(SeckillOrderEvent event) {
        log.info("SeckillOrderListener orderNo:{}", event.getOrderNo());
        String orderNo = event.getOrderNo();
        if (StringUtils.isBlank(orderNo)) {
            throw new BusinessException("秒杀订单编号不存在");
        }
        // 检查订单是否存在
        Order orderExists = orderService.getOne(new QueryWrapper<Order>().eq("order_no", orderNo));
        // 判断订单userId
        if (orderExists != null) {
            log.info("秒杀订单已存在");
            syncOrderState(event, orderExists);
            return;
        }
        Order order = new Order();
        TransactionStatus transaction = platformTransactionManager.getTransaction(transactionDefinition);
        try {
            Seckill seckill = event.getSeckill();
            MallUserVO userVO = event.getUserVO();
            Long nowTime = event.getNowTime();
            Long seckillId = seckill.getSeckillId();
            // 减库存
            if (!seckillService.reduceStock(seckillId, new Date(nowTime))) {
                throw new BusinessException("秒杀商品减库存失败");
            }
            Long goodsId = seckill.getGoodsId();
            Goods goods = goodsService.getById(goodsId);
            // 保存订单
            order.setOrderNo(orderNo);
            order.setCreateTime(new Date());
            order.setTotalPrice(seckill.getSeckillPrice());
            order.setUserId(userVO.getUserId());
            order.setUserAddress(userVO.getAddress());
            String extraInfo = "";
            order.setExtraInfo(extraInfo);
            if (!orderService.save(order)) {
                throw new BusinessException("生成订单内部异常");
            }
            // 保存订单商品项
            OrderItem orderItem = new OrderItem();
            Long orderId = order.getOrderId();
            orderItem.setOrderId(orderId);
            orderItem.setSeckillId(seckillId);
            orderItem.setGoodsId(goods.getGoodsId());
            orderItem.setGoodsCoverImg(goods.getGoodsCoverImg());
            orderItem.setGoodsName(goods.getGoodsName());
            orderItem.setGoodsCount(1);
            orderItem.setSellingPrice(seckill.getSeckillPrice());
            if (!orderItemService.save(orderItem)) {
                throw new BusinessException("生成订单内部异常");
            }
            platformTransactionManager.commit(transaction);
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            if (!transaction.isCompleted()) {
                platformTransactionManager.rollback(transaction);
            }
            throw e;
        }
        syncOrderState(event, order);
    }

    private void syncOrderState(SeckillOrderEvent event, Order order) {
        MallUserVO userVO = event.getUserVO();
        if (userVO == null || !order.getUserId().equals(userVO.getUserId())) {
            throw new BusinessException("当前订单用户异常");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() >= 0) {
            Seckill seckill = event.getSeckill();
            long remainingMillis = seckill.getSeckillEnd().getTime() - System.currentTimeMillis();
            if (remainingMillis > 0) {
                // 记录购买过的用户
                redisCache.setCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckill.getSeckillId(), userVO.getUserId());
                redisCache.expire(Constants.SECKILL_SUCCESS_USER_ID + seckill.getSeckillId(), remainingMillis, TimeUnit.MILLISECONDS);
            }
            if (order.getOrderStatus() == null || order.getOrderStatus() == 0) {
                // 秒杀订单30秒未支付超期任务
                long createdAt = order.getCreateTime() == null ? event.getNowTime() : order.getCreateTime().getTime();
                long delay = Math.max(0, createdAt + 30_000L - System.currentTimeMillis());
                taskService.addTask(new OrderUnPaidTask(order.getOrderId(), delay));
            }
        }
        redisCache.setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + order.getOrderNo(), Constants.SUCCESS, 10, TimeUnit.MINUTES);
    }
}
