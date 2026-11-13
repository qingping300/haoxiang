package com.haoxiang.mall.controller.mall;

import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.core.entity.MallUser;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.service.MallUserService;
import com.haoxiang.mall.core.service.OrderService;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.util.R;
import com.haoxiang.mall.util.security.Md5Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MallPayControllerTest {

    private final OrderService orderService = mock(OrderService.class);
    private final MallUserService mallUserService = mock(MallUserService.class);
    private final MallPayController controller = new MallPayController();
    private final MockHttpSession session = new MockHttpSession();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "orderService", orderService);
        ReflectionTestUtils.setField(controller, "mallUserService", mallUserService);
        MallUserVO userVO = new MallUserVO();
        userVO.setUserId(7L);
        session.setAttribute(Constants.MALL_USER_SESSION_KEY, userVO);
    }

    @Test
    void rejectsWrongPasswordWithoutUpdatingOrder() {
        Order order = pendingOrder();
        when(orderService.judgeOrderUserId("HX-1", 7L)).thenReturn(order);
        MallUser user = new MallUser();
        user.setPasswordMd5(Md5Utils.hash("correct"));
        when(mallUserService.getById(7L)).thenReturn(user);

        assertThrows(BusinessException.class, () -> controller.payOrder("HX-1", "wrong", 1, session));
        verify(orderService, never()).update();
    }

    @Test
    @SuppressWarnings("unchecked")
    void verifiesPasswordAndUpdatesOnlyPendingOrder() {
        Order order = pendingOrder();
        when(orderService.judgeOrderUserId("HX-1", 7L)).thenReturn(order);
        MallUser user = new MallUser();
        user.setPasswordMd5(Md5Utils.hash("correct"));
        when(mallUserService.getById(7L)).thenReturn(user);
        UpdateChainWrapper<Order> update = mock(UpdateChainWrapper.class, RETURNS_SELF);
        when(orderService.update()).thenReturn(update);
        doReturn(update).when(update).set(anyString(), any());
        doReturn(update).when(update).eq(anyString(), any());
        when(update.update()).thenReturn(true);

        R result = controller.payOrder("HX-1", "correct", 1, session);

        assertEquals(200, result.getCode());
        assertEquals("HX-1", result.getMap().get("orderNo"));
        verify(update).set("pay_type", 1);
        verify(update).set("pay_status", 1);
        verify(update).eq("order_id", 42L);
        verify(update).eq("order_status", 0);
        verify(update).eq("pay_status", 0);
        verify(update).update();
    }

    private Order pendingOrder() {
        Order order = new Order();
        order.setOrderId(42L);
        order.setOrderNo("HX-1");
        order.setUserId(7L);
        order.setOrderStatus((byte) 0);
        order.setPayStatus((byte) 0);
        order.setTotalPrice(100);
        return order;
    }
}
