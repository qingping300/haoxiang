package com.haoxiang.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.Goods;
import com.haoxiang.mall.core.entity.Seckill;
import com.haoxiang.mall.core.entity.vo.SeckillVO;
import com.haoxiang.mall.core.service.GoodsService;
import com.haoxiang.mall.core.service.SeckillService;
import com.haoxiang.mall.redis.RedisCache;
import com.haoxiang.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 后台管理-秒杀活动管理
 */
@RestController
@RequestMapping("admin/seckill")
public class SeckillManagerController extends BaseController {

    @Autowired
    private SeckillService seckillService;
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private RedisCache redisCache;

    @GetMapping
    public R index(HttpServletRequest request) {
        request.setAttribute("path", "seckill");
        return pageData(request);
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    @ResponseBody
    public IPage<Seckill> list(SeckillVO seckillVO, HttpServletRequest request) {
        Page<Seckill> page = getPage(request);
        return seckillService.selectPage(page, seckillVO);
    }

    @ResponseBody
    @PostMapping("/save")
    public R save(@RequestBody Seckill seckill) {
        Long goodsId = seckill.getGoodsId();
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            return R.error("秒杀商品不存在！");
        }
        if (goods.getGoodsSellStatus() != 0) {
            return R.error("秒杀商品未上架！");
        }

        boolean save = seckillService.save(seckill);
        if (save) {
            // 库存预热
            redisCache.setCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + seckill.getSeckillId(), seckill.getSeckillNum());
            redisCache.deleteObject(Constants.SECKILL_GOODS_LIST_DATA);
        }
        return R.result(save);
    }

    @ResponseBody
    @PostMapping("/update")
    public R update(@RequestBody Seckill seckill) {
        seckill.setUpdateTime(new Date());
        boolean update = seckillService.updateById(seckill);
        if (update) {
            // 库存预热
            Long seckillId = seckill.getSeckillId();
            redisCache.setCacheObject(Constants.SECKILL_GOODS_STOCK_KEY + seckillId, seckill.getSeckillNum());
            redisCache.deleteObject(Constants.SECKILL_GOODS_LIST_DATA);
            redisCache.deleteObject(Constants.SECKILL_GOODS_DETAIL_DATA + seckillId);
        }
        return R.result(update);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public R Info(@PathVariable("id") Long id) {
        return R.success().add("data", seckillService.getById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public R delete(@PathVariable("id") Long id) {
        return R.result(seckillService.removeById(id));
    }
}
