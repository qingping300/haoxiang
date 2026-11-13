package com.haoxiang.mall.core.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import com.haoxiang.mall.core.mapper.CouponMapper;
import com.haoxiang.mall.core.mapper.CouponUserMapper;
import com.haoxiang.mall.core.entity.Coupon;
import com.haoxiang.mall.core.entity.CouponUser;
import com.haoxiang.mall.core.service.CouponService;
import com.haoxiang.mall.core.service.CouponUserService;
import com.haoxiang.mall.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
@AllArgsConstructor
public class CouponUserServiceImpl extends ServiceImpl<CouponUserMapper, CouponUser> implements CouponUserService {

    private CouponService couponService;

    private CouponMapper couponMapper;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveCouponUser(Long couponId, Long userId) {
        Coupon coupon = couponService.getById(couponId);
        if (coupon.getCouponLimit() != 0) {
            long count = count(new QueryWrapper<CouponUser>()
                    .eq("user_id", userId)
                    .eq("coupon_id", coupon.getCouponId()));
            if (count != 0) {
                throw new BusinessException("优惠卷已经领过了,无法再次领取！");
            }
        }
        if (coupon.getCouponTotal() != 0) {
            long count = count(new QueryWrapper<CouponUser>()
                    .eq("coupon_id", coupon.getCouponId()));
            if (count >= coupon.getCouponTotal()) {
                throw new BusinessException("优惠卷已经领完了！");
            }
            if (couponMapper.reduceCouponTotal(couponId) <= 0) {
                throw new BusinessException("优惠卷领取失败！");
            }
        }
        CouponUser couponUser = new CouponUser();
        couponUser.setUserId(userId);
        couponUser.setCouponId(couponId);
        Date endDate = calculateEndDate(coupon.getDays());
        couponUser.setStartTime(new Date());
        couponUser.setEndTime(endDate);
        couponUser.setCreateTime(new Date());
        return save(couponUser);
    }

    @Override
    public Coupon getCoupon(Long orderId) {
        CouponUser couponUser = getOne(new QueryWrapper<CouponUser>().eq("order_id", orderId));
        if (couponUser == null) {
            return null;
        }
        return couponService.getById(couponUser.getCouponId());
    }

    @Override
    public Date calculateEndDate(Short days) {
        LocalDate startLocalDate = LocalDate.now();
        LocalDate endLocalDate = startLocalDate.plusDays(days);
        ZoneId zone = ZoneId.systemDefault();
        return Date.from(endLocalDate.atStartOfDay().atZone(zone).toInstant());
    }

    @Override
    public Coupon getCouponByCouponUserId(Long couponUserId) {
        return couponMapper.getCouponByCouponUserId(couponUserId);
    }
}
