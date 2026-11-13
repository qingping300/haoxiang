package com.haoxiang.mall.event;

import lombok.Getter;
import lombok.Setter;
import com.haoxiang.mall.core.entity.Seckill;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 *普通下单事件
 */
@Getter
@Setter
public class SeckillOrderEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = -5946887164851822782L;

    private String orderNo;

    private Seckill seckill;

    private MallUserVO userVO;

    private Long nowTime;


    public SeckillOrderEvent(String orderNo, Seckill seckill, MallUserVO userVO, Long nowTime) {
        super(orderNo);
        this.orderNo = orderNo;
        this.seckill = seckill;
        this.userVO = userVO;
        this.nowTime = nowTime;
    }
}
