package com.haoxiang.mall.mq;

import com.haoxiang.mall.event.SeckillOrderEvent;
import com.haoxiang.mall.mq.reciver.OrderDirectReceiver;
import com.haoxiang.mall.redis.RedisCache;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderDirectReceiverTest {
    private final RedisCache redis = mock(RedisCache.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final Channel channel = mock(Channel.class);
    private final OrderDirectReceiver receiver = new OrderDirectReceiver(1);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(receiver, "redisCache", redis);
        ReflectionTestUtils.setField(receiver, "applicationEventPublisher", events);
    }

    @Test
    void duplicateIsAcknowledgedWithoutCreatingOrder() throws Exception {
        when(redis.getCacheObject("order_event-1")).thenReturn("1");
        receiver.process(channel, message("event-1"));
        verify(channel).basicAck(42L, false);
        verifyNoInteractions(events);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void businessExceptionIsNotAcknowledgedAsSuccess() throws Exception {
        doThrow(new IllegalStateException("database unavailable")).when(events).publishEvent(any(SeckillOrderEvent.class));
        receiver.process(channel, message("event-1"));
        verify(channel).basicNack(42L, false, true);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(redis, never()).setCacheObject(anyString(), any(), anyInt(), any(TimeUnit.class));
    }

    @Test
    void acknowledgesOnlyAfterBusinessHandlerReturns() throws Exception {
        receiver.process(channel, message("event-1"));
        var ordered = inOrder(events, redis, channel);
        ordered.verify(events).publishEvent(any(SeckillOrderEvent.class));
        ordered.verify(redis).setCacheObject("order_event-1", "1", 30, TimeUnit.SECONDS);
        ordered.verify(channel).basicAck(42L, false);
    }

    @Test
    void legacyMessageFallsBackToOrderNumberInsteadOfSharedNullKey() throws Exception {
        receiver.process(channel, message(null));
        verify(redis).getCacheObject("order_HX-1");
        verify(redis, never()).getCacheObject("order_null");
        verify(channel).basicAck(42L, false);
    }

    private Message message(String id) {
        MessageProperties props = new MessageProperties();
        props.setMessageId(id);
        props.setDeliveryTag(42L);
        return new Message("{\"orderNo\":\"HX-1\",\"nowTime\":1,\"seckill\":{},\"userVO\":{}}".getBytes(StandardCharsets.UTF_8), props);
    }
}
