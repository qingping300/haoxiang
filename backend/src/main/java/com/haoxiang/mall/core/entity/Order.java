package com.haoxiang.mall.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("haoxiang_mall_order")
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 9199020613703879810L;
    @TableId(type = IdType.AUTO)
    private Long orderId;

    private String orderNo;

    private Long userId;

    private Integer totalPrice;

    private Byte payStatus;

    private Byte payType;

    private Date payTime;

    private Byte orderStatus;

    private String extraInfo;

    private String userName;
    private String userPhone;
    private String userAddress;

    private Byte isDeleted;

    private Date createTime;

    private Date updateTime;

    /** 更新时检查并递增的版本号。 */
    @com.baomidou.mybatisplus.annotation.Version
    private Long version;
}
