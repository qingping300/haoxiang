package com.haoxiang.mall.controller.mall;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.haoxiang.mall.annotation.Limit;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.Goods;
import com.haoxiang.mall.core.entity.Seckill;
import com.haoxiang.mall.core.entity.SeckillSuccess;
import com.haoxiang.mall.core.entity.vo.ExposerVO;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.entity.vo.SeckillSuccessVO;
import com.haoxiang.mall.core.entity.vo.ShopCatVO;
import com.haoxiang.mall.core.service.GoodsService;
import com.haoxiang.mall.core.service.SeckillService;
import com.haoxiang.mall.core.service.SeckillSuccessService;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.redis.RedisCache;
import com.haoxiang.mall.util.R;
import com.haoxiang.mall.util.security.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 前台商城-秒杀功能相关
 */
@RestController
@RequestMapping("seckill")
public class MallSeckillController extends BaseController {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private SeckillSuccessService seckillSuccessService;

    @Autowired
    private RedisCache redisCache;

    @ResponseBody
    @GetMapping("time/now")
    public R getTimeNow() {
        return R.success().add("now", System.currentTimeMillis());
    }

    @ResponseBody
    @PostMapping("{seckillId}/exposer")
    public R exposerUrl(@PathVariable Long seckillId) {
        ExposerVO exposerVO = seckillService.exposerUrl(seckillId);
        return R.success().add("exposer", exposerVO);
    }

    @ResponseBody
    // 接口限流注解
    @Limit(key = "seckill", period = 1, count = 20, name = "执行秒杀限制", prefix = Constants.CACHE_PREFIX)
    @PostMapping(value = "/{seckillId}/{md5}/executionFour")
    public R execute(@PathVariable Long seckillId,
                     @PathVariable String md5, HttpSession session) {
        if (md5 == null || !md5.equals(Md5Utils.hash(seckillId))) {
            throw new BusinessException("秒杀商品不存在");
        }
        MallUserVO userVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        SeckillSuccessVO seckillSuccessVO = null;
        try {
            seckillSuccessVO = seckillService.executeSeckillFinal(seckillId, userVO);
        } catch (BusinessException e) {
            return R.error(e.getMessage());
        }
        return R.success().add("seckillSuccess", seckillSuccessVO);
    }

    @GetMapping("/{seckillSuccessId}/{md5}/settle")
    public R settle(@PathVariable Long seckillSuccessId,
                         @PathVariable String md5,
                         HttpServletRequest request,
                         HttpSession session) {
        if (md5 == null || !md5.equals(Md5Utils.hash(seckillSuccessId + Constants.SECKILL_EXECUTE_SALT))) {
            throw new BusinessException("秒杀商品结算不合法");
        }
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        SeckillSuccess seckillSuccess = seckillSuccessService.getById(seckillSuccessId);
        if (!seckillSuccess.getUserId().equals(mallUserVO.getUserId())) {
            throw new BusinessException("当前登陆用户与抢购秒杀商品的用户不匹配");
        }
        Long seckillId = seckillSuccess.getSeckillId();
        Seckill seckill = seckillService.getById(seckillId);
        ShopCatVO shopCatVO = new ShopCatVO();
        Long goodsId = seckill.getGoodsId();
        Goods goods = goodsService.getById(goodsId);
        shopCatVO.setGoodsId(goodsId);
        shopCatVO.setGoodsName(goods.getGoodsName());
        shopCatVO.setGoodsCoverImg(goods.getGoodsCoverImg());
        shopCatVO.setGoodsCount(1);
        shopCatVO.setSellingPrice(seckill.getSeckillPrice());
        request.setAttribute("isSeckillSettle", true);
        request.setAttribute("seckillSuccessId", seckillSuccessId);
        request.setAttribute("seckillSecretKey", Md5Utils.hash(seckillSuccessId + Constants.SECKILL_ORDER_SALT));
        request.setAttribute("priceTotal", seckill.getSeckillPrice());
        request.setAttribute("myShoppingCartItems", Collections.singletonList(shopCatVO));
        return R.success().add("isSeckillSettle", true)
                .add("seckillSuccessId", seckillSuccessId)
                .add("seckillSecretKey", Md5Utils.hash(seckillSuccessId + Constants.SECKILL_ORDER_SALT))
                .add("priceTotal", seckill.getSeckillPrice())
                .add("myShoppingCartItems", Collections.singletonList(shopCatVO));
    }

    @GetMapping("list")
    @ResponseBody
    public R list() {
        // 判断缓存中是否有当前秒杀商品列表数据
        List<Map<String, Object>> list = redisCache.getCacheObject(Constants.SECKILL_GOODS_LIST_DATA);
        if (list == null) {
            List<Seckill> seckillList = seckillService.list(new QueryWrapper<Seckill>().eq("status", 1).orderByDesc("seckill_rank"));
            list = seckillList.stream().map(seckill -> {
                Map<String, Object> map = new HashMap<>();
                map.put("seckillId", seckill.getSeckillId());
                map.put("goodsId", seckill.getGoodsId());
                map.put("seckillPrice", seckill.getSeckillPrice());
                Goods goods = goodsService.getById(seckill.getGoodsId());
                map.put("goodsName", goods.getGoodsName());
                map.put("originalPrice", goods.getOriginalPrice());
                map.put("goodsCoverImg", goods.getGoodsCoverImg());
                map.put("goodsIntro", goods.getGoodsIntro());
                return map;
            }).collect(Collectors.toList());
            // 缓存秒杀商品列表数据
            redisCache.setCacheObject(Constants.SECKILL_GOODS_LIST_DATA, list, 100, TimeUnit.HOURS);
        }
        return R.success().add("seckillList", list);
    }


    @GetMapping("detail/{seckillId}")
    @ResponseBody
    public R detail(@PathVariable("seckillId") Long seckillId) {
        // 判断缓存中是否有当前秒杀商品详情数据
        Map<String, Object> data = redisCache.getCacheObject(Constants.SECKILL_GOODS_DETAIL_DATA + seckillId);
        if (data != null) {
            R result = R.success();
            data.forEach(result::add);
            return result;
        }
        Seckill seckill = seckillService.getById(seckillId);
        Long goodsId = seckill.getGoodsId();
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goodsId);
        map.put("seckillPrice", seckill.getSeckillPrice());
        map.put("startDate", seckill.getSeckillBegin().getTime());
        map.put("endDate", seckill.getSeckillEnd().getTime());
        Goods goods = goodsService.getById(seckill.getGoodsId());
        map.put("goodsName", goods.getGoodsName());
        map.put("originalPrice", goods.getOriginalPrice());
        map.put("goodsCoverImg", goods.getGoodsCoverImg());
        map.put("goodsIntro", goods.getGoodsIntro());
        map.put("goodsDetailContent", goods.getGoodsDetailContent());
        long now = System.currentTimeMillis();
        long startAt = seckill.getSeckillBegin().getTime();
        long endAt = seckill.getSeckillEnd().getTime();
        int miaoshaStatus;
        int remainSeconds;
        if (now < startAt) {
            // 秒杀还没开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int) ((startAt - now) / 1000);
        } else if (now > endAt) {
            // 秒杀已经结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        } else {
            // 秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        data = new HashMap<>();
        data.put("goodsDetail", map);
        data.put("seckillId", seckill.getSeckillId());
        data.put("seckillStatus", miaoshaStatus);
        data.put("remainSeconds", remainSeconds);
        // 缓存秒杀商品详情数据
        redisCache.setCacheObject(Constants.SECKILL_GOODS_DETAIL_DATA + seckillId, data, 30, TimeUnit.MINUTES);
        R result = R.success();
        data.forEach(result::add);
        return result;
    }

}
