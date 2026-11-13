package com.haoxiang.mall.listener;

import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.entity.vo.ShopCatVO;
import com.haoxiang.mall.core.service.*;
import com.haoxiang.mall.event.OrderEvent;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.redis.RedisCache;
import com.haoxiang.mall.task.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderListenerFailureTest {
    private final OrderListener listener = new OrderListener();
    private final ShopCatService carts = mock(ShopCatService.class);
    private final GoodsService goods = mock(GoodsService.class);
    private final OrderService orders = mock(OrderService.class);
    private final OrderItemService items = mock(OrderItemService.class);
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final TransactionStatus transaction = mock(TransactionStatus.class);
    private final RedisCache redis = mock(RedisCache.class);
    private final TaskService tasks = mock(TaskService.class);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listener, "shopCatService", carts);
        ReflectionTestUtils.setField(listener, "goodsService", goods);
        ReflectionTestUtils.setField(listener, "orderService", orders);
        ReflectionTestUtils.setField(listener, "orderItemService", items);
        ReflectionTestUtils.setField(listener, "platformTransactionManager", transactions);
        ReflectionTestUtils.setField(listener, "transactionDefinition", new DefaultTransactionDefinition());
        ReflectionTestUtils.setField(listener, "redisCache", redis);
        ReflectionTestUtils.setField(listener, "taskService", tasks);
        when(transactions.getTransaction(any())).thenReturn(transaction);
        when(carts.removeByIds(anyCollection())).thenReturn(true);
        when(goods.reduceStock(1L, 1)).thenReturn(true);
        when(orders.save(any(Order.class))).thenAnswer(call -> {
            call.<Order>getArgument(0).setOrderId(42L);
            return true;
        });
        when(items.saveBatch(anyCollection())).thenReturn(true);
    }

    @Test
    void databaseFailureRollsBackAndReachesCaller() {
        when(items.saveBatch(anyCollection())).thenReturn(false);
        assertThrows(BusinessException.class, () -> listener.onApplicationEvent(event()));
        verify(transactions).rollback(transaction);
        verify(transactions, never()).commit(any());
        verifyNoInteractions(redis, tasks);
    }

    @Test
    void committedOrderRemainsSuccessfulWhenResultCacheFails() {
        doThrow(new IllegalStateException("redis unavailable")).when(redis)
                .setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + "HX-1", Constants.SUCCESS, 10, TimeUnit.MINUTES);
        assertDoesNotThrow(() -> listener.onApplicationEvent(event()));
        verify(transactions, never()).rollback(any());
        var ordered = inOrder(transactions, tasks, redis);
        ordered.verify(transactions).commit(transaction);
        ordered.verify(tasks).addTask(any());
        ordered.verify(redis).setCacheObject(Constants.SAVE_ORDER_RESULT_KEY + "HX-1", Constants.SUCCESS, 10, TimeUnit.MINUTES);
    }

    private OrderEvent event() {
        MallUserVO user = new MallUserVO();
        user.setUserId(7L);
        ShopCatVO item = new ShopCatVO();
        item.setCartItemId(1L);
        item.setGoodsId(1L);
        item.setGoodsCount(1);
        item.setSellingPrice(100);
        return new OrderEvent("HX-1", user, null, List.of(item));
    }
}
