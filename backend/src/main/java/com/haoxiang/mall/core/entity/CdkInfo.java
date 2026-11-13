package com.haoxiang.mall.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("cdk_info")
public class CdkInfo {
    @TableId(type = IdType.AUTO)
    private Integer cdkId;
    private String cdkNo;

    private Date createTime;
    private String createUser;
}
