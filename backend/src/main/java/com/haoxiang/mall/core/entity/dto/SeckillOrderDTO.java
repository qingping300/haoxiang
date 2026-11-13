package com.haoxiang.mall.core.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 传递秒杀订单创建所需的订单号、活动和用户标识以及请求时间。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderDTO {

    private String orderNo;
    private Long seckillId;
    private Long userId;
    private Long nowTime;
}
