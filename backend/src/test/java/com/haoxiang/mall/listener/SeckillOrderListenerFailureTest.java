package com.haoxiang.mall.listener;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.core.entity.Goods;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.OrderItem;
import com.haoxiang.mall.core.entity.Seckill;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.service.*;
import com.haoxiang.mall.event.SeckillOrderEvent;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.redis.RedisCache;
import com.haoxiang.mall.task.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SeckillOrderListenerFailureTest {
    private final SeckillOrderListener listener = new SeckillOrderListener();
    private final GoodsService goods = mock(GoodsService.class);
    private final OrderService orders = mock(OrderService.class);
    private final OrderItemService items = mock(OrderItemService.class);
    private final SeckillService seckill = mock(SeckillService.class);
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final TransactionStatus transaction = mock(TransactionStatus.class);
    private final RedisCache redis = mock(RedisCache.class);
    private final TaskService tasks = mock(TaskService.class);
    private final AtomicReference<Order> savedOrder = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listener, "goodsService", goods);
        ReflectionTestUtils.setField(listener, "orderService", orders);
        ReflectionTestUtils.setField(listener, "orderItemService", items);
        ReflectionTestUtils.setField(listener, "seckillService", seckill);
        ReflectionTestUtils.setField(listener, "platformTransactionManager", transactions);
        ReflectionTestUtils.setField(listener, "transactionDefinition", new DefaultTransactionDefinition());
        ReflectionTestUtils.setField(listener, "redisCache", redis);
        ReflectionTestUtils.setField(listener, "taskService", tasks);
        when(transactions.getTransaction(any())).thenReturn(transaction);
        when(seckill.reduceStock(eq(6L), any(Date.class))).thenReturn(true);
        Goods product = new Goods();
        product.setGoodsId(1L);
        product.setGoodsName("stock test");
        when(goods.getById(1L)).thenReturn(product);
        when(orders.save(any(Order.class))).thenAnswer(call -> {
            Order order = call.getArgument(0);
            order.setOrderId(42L);
            savedOrder.set(order);
            return true;
        });
        when(items.save(any(OrderItem.class))).thenReturn(true);
    }

    @Test
    void databaseFailureDoesNotLeaveSuccessMarkerOrTimeoutTask() {
        when(items.save(any(OrderItem.class))).thenReturn(false);
        assertThrows(BusinessException.class, () -> listener.onApplicationEvent(event()));
        verify(transactions).rollback(transaction);
        verify(transactions, never()).commit(any());
        verifyNoInteractions(redis, tasks);
    }

    @Test
    void redisAndTimeoutTaskAreUpdatedAfterDatabaseCommit() {
        listener.onApplicationEvent(event());
        var ordered = inOrder(transactions, redis, tasks);
        ordered.verify(transactions).commit(transaction);
        ordered.verify(redis).setCacheSet(Constants.SECKILL_SUCCESS_USER_ID + 6L, 7L);
        ordered.verify(tasks).addTask(any());
        ordered.verify(redis).setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + "HX-1", Constants.SUCCESS, 10, TimeUnit.MINUTES);
    }

    @Test
    void redeliveryRepairsPostCommitFailureWithoutDeductingAgain() {
        doThrow(new IllegalStateException("redis unavailable")).doNothing()
                .when(redis).setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + "HX-1", Constants.SUCCESS, 10, TimeUnit.MINUTES);
        assertThrows(IllegalStateException.class, () -> listener.onApplicationEvent(event()));
        verify(transactions, never()).rollback(any());
        when(orders.getOne(any(Wrapper.class))).thenReturn(savedOrder.get());
        assertDoesNotThrow(() -> listener.onApplicationEvent(event()));
        verify(seckill, times(1)).reduceStock(eq(6L), any(Date.class));
        verify(orders, times(1)).save(any(Order.class));
        verify(transactions, times(1)).commit(transaction);
        verify(redis, times(2)).setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + "HX-1", Constants.SUCCESS, 10, TimeUnit.MINUTES);
    }

    @Test
    void redeliveryOfClosedOrderDoesNotRestoreUserPurchaseMarker() {
        Order closed = new Order();
        closed.setOrderId(42L);
        closed.setOrderNo("HX-1");
        closed.setUserId(7L);
        closed.setOrderStatus((byte) -2);
        when(orders.getOne(any(Wrapper.class))).thenReturn(closed);
        listener.onApplicationEvent(event());
        verifyNoInteractions(transactions, seckill, tasks);
        verify(redis, never()).setCacheSet(anyString(), any());
        verify(redis).setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + "HX-1", Constants.SUCCESS, 10, TimeUnit.MINUTES);
    }

    private SeckillOrderEvent event() {
        Seckill activity = new Seckill();
        activity.setSeckillId(6L);
        activity.setGoodsId(1L);
        activity.setSeckillPrice(100);
        activity.setSeckillEnd(new Date(System.currentTimeMillis() + 300_000));
        MallUserVO user = new MallUserVO();
        user.setUserId(7L);
        return new SeckillOrderEvent("HX-1", activity, user, System.currentTimeMillis());
    }
}
