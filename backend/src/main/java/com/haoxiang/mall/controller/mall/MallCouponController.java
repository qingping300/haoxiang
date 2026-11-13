package com.haoxiang.mall.controller.mall;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.vo.CouponVO;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.service.CouponService;
import com.haoxiang.mall.core.service.CouponUserService;
import com.haoxiang.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 前台商城-优惠券
 */
@RestController
public class MallCouponController extends BaseController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponUserService couponUserService;

    @GetMapping("coupon")
    public R index(HttpServletRequest request, HttpSession session) {
        Long userId = null;
        if (session.getAttribute(Constants.MALL_USER_SESSION_KEY) != null) {
            userId = ((MallUserVO) request.getSession().getAttribute(Constants.MALL_USER_SESSION_KEY)).getUserId();
        }
        List<CouponVO> coupons = couponService.selectAvailableCoupon(userId);
        request.setAttribute("coupons", coupons);
        return pageData(request);
    }

    @ResponseBody
    @PostMapping("coupon/{couponId}")
    public R save(@PathVariable Long couponId, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        couponUserService.saveCouponUser(couponId, mallUserVO.getUserId());
        return R.success();
    }
}
