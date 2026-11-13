package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.haoxiang.mall.core.entity.CouponUser;

import java.util.List;

public interface CouponUserMapper extends BaseMapper<CouponUser> {
    List<CouponUser> selectAvailableList(Long userId);
}
