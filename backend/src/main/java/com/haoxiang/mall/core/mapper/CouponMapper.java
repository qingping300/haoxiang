package com.haoxiang.mall.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.haoxiang.mall.core.entity.Coupon;
import com.haoxiang.mall.core.entity.vo.CouponVO;

import java.util.List;

public interface CouponMapper extends BaseMapper<Coupon> {

    IPage<Coupon> selectListPage(Page<Coupon> page, CouponVO coupon);

    List<Coupon> selectAvailableCoupon();

    Integer reduceCouponTotal(Long couponId);

    Coupon getCouponByCouponUserId(Long couponUserId);
}
