package com.haoxiang.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.Coupon;
import com.haoxiang.mall.core.entity.vo.CouponVO;
import com.haoxiang.mall.core.service.CouponService;
import com.haoxiang.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 后台管理-优惠券管理
 */
@RestController
@RequestMapping("admin/coupon")
public class CouponManagerController extends BaseController {

    @Autowired
    private CouponService couponService;

    @GetMapping
    public R index(HttpServletRequest request) {
        request.setAttribute("configType", 3);
        request.setAttribute("path", "coupon");
        return pageData(request);
    }

    @ResponseBody
    @GetMapping("/list")
    public IPage<Coupon> list(CouponVO couponVO, HttpServletRequest request) {
        Page<Coupon> page = getPage(request);
        return couponService.selectPage(page, couponVO);
    }

    /**
     * 保存
     *
     * @param coupon
     * @return
     */
    @ResponseBody
    @PostMapping("/save")
    public R save(@RequestBody Coupon coupon) {
        coupon.setCreateTime(new Date());
        return R.result(couponService.save(coupon));
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    @ResponseBody
    public R update(@RequestBody Coupon coupon) {
        return R.result(couponService.updateCoupon(coupon));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    @ResponseBody
    public R Info(@PathVariable("id") Long id) {
        return R.success().add("data", couponService.getById(id));
    }

    /**
     * 详情
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public R delete(@PathVariable("id") Long id) {
        return R.result(couponService.removeById(id));
    }
}
