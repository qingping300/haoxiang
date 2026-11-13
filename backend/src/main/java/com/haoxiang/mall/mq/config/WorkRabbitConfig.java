package com.haoxiang.mall.mq.config;

import com.haoxiang.mall.mq.reciver.OrderDirectReceiver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工作模式rabbitmq配置
 */
@Configuration
public class WorkRabbitConfig {

    /*******************************************订单消费者配置*****************************************/
    @Bean
    public OrderDirectReceiver orderWorkReceiver1() {
        return new OrderDirectReceiver(1);
    }
}
