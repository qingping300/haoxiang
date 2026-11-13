package com.haoxiang.mall.controller.mall;

import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.service.OrderService;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.redis.RedisCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderResultAuthorizationTest {
    private final OrderService orders = mock(OrderService.class);
    private final RedisCache redis = mock(RedisCache.class);
    private final MallOrderController controller = new MallOrderController();
    private final MockHttpSession session = new MockHttpSession();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "orderService", orders);
        ReflectionTestUtils.setField(controller, "redisCache", redis);
        MallUserVO user = new MallUserVO();
        user.setUserId(7L);
        session.setAttribute(Constants.MALL_USER_SESSION_KEY, user);
    }

    @Test
    void rejectsUnauthenticatedQueryBeforeReadingData() {
        assertThrows(BusinessException.class, () -> controller.saveOrderResult("HX-1", new MockHttpSession()));
        verifyNoInteractions(orders, redis);
    }

    @Test
    void persistedOrderSucceedsWithoutResultCache() {
        when(orders.getOrderByOrderNo("HX-1")).thenReturn(order(7L));
        assertEquals(200, controller.saveOrderResult("HX-1", session).getCode());
        verifyNoInteractions(redis);
    }

    @Test
    void rejectsOtherUsersPersistedOrder() {
        when(orders.getOrderByOrderNo("HX-1")).thenReturn(order(8L));
        assertThrows(BusinessException.class, () -> controller.saveOrderResult("HX-1", session));
        verifyNoInteractions(redis);
    }

    @Test
    void onlyOwnerCanQueryPendingRequest() {
        when(redis.getCacheObject(Constants.SAVE_ORDER_USER_ID_KEY + "HX-1")).thenReturn("7");
        assertEquals("PROCESSING", controller.saveOrderResult("HX-1", session).getMap().get("status"));
    }

    @Test
    void rejectsOtherUsersPendingRequestWithoutReadingResult() {
        when(redis.getCacheObject(Constants.SAVE_ORDER_USER_ID_KEY + "HX-1")).thenReturn("8");
        assertThrows(BusinessException.class, () -> controller.saveOrderResult("HX-1", session));
        verify(redis, never()).getCacheObject(Constants.SAVE_ORDER_RESULT_KEY + "HX-1");
    }

    @Test
    void rejectsUnknownRequest() {
        assertThrows(BusinessException.class, () -> controller.saveOrderResult("HX-1", session));
    }

    @Test
    void cachedSuccessAloneCannotProveOrderWasCommitted() {
        when(redis.getCacheObject(Constants.SAVE_ORDER_USER_ID_KEY + "HX-1")).thenReturn("7");
        when(redis.getCacheObject(Constants.SAVE_ORDER_RESULT_KEY + "HX-1")).thenReturn(Constants.SUCCESS);
        assertEquals("PROCESSING", controller.saveOrderResult("HX-1", session).getMap().get("status"));
    }

    private Order order(Long userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo("HX-1");
        return order;
    }
}
