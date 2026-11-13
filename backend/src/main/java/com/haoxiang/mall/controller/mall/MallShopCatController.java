package com.haoxiang.mall.controller.mall;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.ShopCat;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.entity.vo.MyCouponVO;
import com.haoxiang.mall.core.entity.vo.ShopCatVO;
import com.haoxiang.mall.core.service.CouponService;
import com.haoxiang.mall.core.service.ShopCatService;
import com.haoxiang.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台商城-购物车
 */
@RestController
public class MallShopCatController extends BaseController {

    @Autowired
    private ShopCatService shopCatService;

    @Autowired
    private CouponService couponService;

    @ResponseBody
    @RequestMapping(value = "shopCart", method = {RequestMethod.POST, RequestMethod.PUT})
    public R save(@RequestBody ShopCat shopCat, HttpSession session) {
        shopCat.setUserId(((MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY)).getUserId());
        shopCatService.saveShopCat(shopCat);
        return R.success();
    }

    @ResponseBody
    @GetMapping("shopCart/getUserShopCartCount")
    public R getUserShopCartCount(HttpSession session) {
        Object attribute = session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        long count = 0;
        if (attribute == null) {
            return R.success().add("count", count);
        }
        MallUserVO mallUserVO = (MallUserVO) attribute;
        count = (shopCatService.count(new QueryWrapper<ShopCat>()
                .eq("user_id", mallUserVO.getUserId())));
        return R.success().add("count", count);
    }

    @ResponseBody
    @DeleteMapping("shopCart/{id}")
    public R delete(@PathVariable("id") Long shopCatId) {
        shopCatService.removeById(shopCatId);
        return R.success();
    }


    @GetMapping("shopCart")
    public R shopCart(HttpServletRequest request, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        int itemsTotal = 0;
        int priceTotal = 0;
        List<ShopCatVO> collect = shopCatService.getShopCatVOList(mallUserVO.getUserId());
        if (CollectionUtils.isNotEmpty(collect)) {
            itemsTotal = collect.size();
            for (ShopCatVO shopCatVO : collect) {
                priceTotal += shopCatVO.getGoodsCount() * shopCatVO.getSellingPrice();
            }
        }
        request.setAttribute("itemsTotal", itemsTotal);
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", collect);
        return pageData(request);
    }

    @GetMapping("shopCart/settle")
    public R settle(HttpServletRequest request, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        List<ShopCat> cats = shopCatService.list(new QueryWrapper<ShopCat>().eq("user_id", mallUserVO.getUserId()));
        if (CollectionUtils.isEmpty(cats)) {
            return R.error("购物车中无数据");
        }
        int priceTotal = 0;
        List<ShopCatVO> collect = shopCatService.getShopCatVOList(mallUserVO.getUserId());
        if (CollectionUtils.isNotEmpty(collect)) {
            for (ShopCatVO shopCatVO : collect) {
                priceTotal += shopCatVO.getGoodsCount() * shopCatVO.getSellingPrice();
            }
        }
        List<MyCouponVO> myCouponVOS = couponService.selectMyCoupons(collect, priceTotal, mallUserVO.getUserId());
        request.setAttribute("coupons", myCouponVOS);
        request.setAttribute("priceTotal", priceTotal);
        request.setAttribute("myShoppingCartItems", collect);
        return pageData(request);
    }

}
