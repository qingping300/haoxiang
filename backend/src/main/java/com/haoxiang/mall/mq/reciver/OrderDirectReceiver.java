package com.haoxiang.mall.mq.reciver;


import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import com.haoxiang.mall.core.entity.Seckill;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.event.SeckillOrderEvent;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.redis.RedisCache;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.haoxiang.mall.mq.RabbitMQConstant.QUEUE_NAME;

@Slf4j
public class OrderDirectReceiver {


    public static final String springReturnedMessageCorrelation = "spring_returned_message_correlation";

    private Integer num;

    @Autowired
    private RedisCache redisCache;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public OrderDirectReceiver(Integer num) {
        this.num = num;
    }

    @RabbitListener(queues = QUEUE_NAME)
    public void process(Channel channel, Message message) throws IOException {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            JSONObject msgObject = JSONObject.parseObject(body);
            String orderNo = msgObject.getString("orderNo");
            String msgId = message.getMessageProperties().getMessageId();
            if (msgId == null || msgId.isBlank()) {
                msgId = message.getMessageProperties().getHeader(springReturnedMessageCorrelation);
            }
            if (msgId == null || msgId.isBlank()) {
                msgId = orderNo;
            }
            if (msgId == null || msgId.isBlank() || orderNo == null || orderNo.isBlank()) {
                throw new BusinessException("订单消息标识不存在");
            }
            log.info("{}-{} 消费者收到消息: msgId={}, orderNo={}", QUEUE_NAME, this.num, msgId, orderNo);
            // 消费消息时幂等性处理
            Object value = redisCache.getCacheObject("order_" + msgId);
            // redis中包含该 key，说明该消息已经被消费过
            if (value != null) {
                log.info("msgId:{}, orderNo:{}, 消息已经处理", msgId, orderNo);
                channel.basicAck(deliveryTag, false);
                return;
            }
            // 发生异常，消费5次后依然失败，则不在消费此消息
            int retryCount = 5;
            if (redisCache.incrByCacheMapValue("order_consumer_map", msgId, 1) > retryCount) {
                log.error("msgId: {}，已经消费{}次，超过最大消费次数！", msgId, retryCount);
                // 确认消息已消费
                channel.basicAck(deliveryTag, false);
                return;
            }
            Seckill seckill = msgObject.getObject("seckill", Seckill.class);
            MallUserVO userVO = msgObject.getObject("userVO", MallUserVO.class);
            Long nowTime = msgObject.getLong("nowTime");
            applicationEventPublisher.publishEvent(new SeckillOrderEvent(orderNo, seckill, userVO, nowTime));
            // multiple参数：确认收到消息，false只确认当前consumer一个消息收到，true确认所有consumer获得的消息
            redisCache.setCacheObject("order_" + msgId, "1", 30, TimeUnit.SECONDS);
            redisCache.delCacheMapValue("order_consumer_map", msgId);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 发生异常，将消息重新入队
            channel.basicNack(deliveryTag, false, true);
            log.error(e.getMessage(), e);
        }
    }
}
