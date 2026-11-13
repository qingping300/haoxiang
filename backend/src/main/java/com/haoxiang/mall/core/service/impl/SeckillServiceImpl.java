package com.haoxiang.mall.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.google.common.util.concurrent.RateLimiter;
import lombok.AllArgsConstructor;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.core.mapper.SeckillMapper;
import com.haoxiang.mall.core.entity.Seckill;
import com.haoxiang.mall.core.entity.SeckillSuccess;
import com.haoxiang.mall.core.entity.vo.ExposerVO;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.entity.vo.SeckillSuccessVO;
import com.haoxiang.mall.core.entity.vo.SeckillVO;
import com.haoxiang.mall.core.service.SeckillService;
import com.haoxiang.mall.core.service.SeckillSuccessService;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.redis.RedisCache;
import com.haoxiang.mall.util.security.Md5Utils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class SeckillServiceImpl extends ServiceImpl<SeckillMapper, Seckill> implements SeckillService {

    // 使用令牌桶RateLimiter 限流
    private static final RateLimiter RATE_LIMITER = RateLimiter.create(20);

    private SeckillMapper seckillMapper;
    private SeckillSuccessService seckillSuccessService;
    private RedisCache redisCache;

    @Override
    public IPage<Seckill> selectPage(Page<Seckill> page, SeckillVO seckillVO) {
        return seckillMapper.selectListPage(page, seckillVO);
    }

    // 秒杀商品URL暴露
    @Override
    public ExposerVO exposerUrl(Long seckillId) {
        // 从缓存中获取秒杀商品
        Seckill seckill = redisCache.getCacheObject(Constants.SECKILL_KEY + seckillId);
        if (seckill == null) {
            // 如果缓存中不存在，则从数据库中获取
            seckill = getById(seckillId);
            redisCache.setCacheObject(Constants.SECKILL_KEY + seckillId, seckill, 24, TimeUnit.HOURS);
        }
        Date startTime = seckill.getSeckillBegin();
        Date endTime = seckill.getSeckillEnd();
        // 系统当前时间
        Date nowTime = new Date();
        //判断当前时间是否在秒杀窗口内
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new ExposerVO(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        // 加密
        String md5 = Md5Utils.hash(seckillId);
        return new ExposerVO(true, md5, seckillId);
    }

    // 增加库存
    @Override
    public boolean addStock(Long seckillId) {
        return seckillMapper.addStock(seckillId);
    }

    /**
     * 秒杀执行方案
     * 1、使用令牌桶算法过滤用户请求<br>
     * 2、使用redis-set数据结构判断用户是否买过秒杀商品<br>
     * 3、返回用户秒杀成功VO
     *
     * @param seckillId 秒杀商品ID
     * @param userVO    秒杀用户VO
     * @return SeckillSuccessVO
     */
    public SeckillSuccessVO executeSeckillFinal(Long seckillId, MallUserVO userVO) {
        // 判断能否在500毫秒内得到令牌，如果不能则立即返回false，不会阻塞程序
        if (!RATE_LIMITER.tryAcquire(500, TimeUnit.MILLISECONDS)) {
            // System.out.println("短期无法获取令牌，真不幸，排队也瞎排");
            throw new BusinessException("秒杀失败");
        }
        // 判断用户是否购买过秒杀商品
        if (redisCache.containsCacheSet(Constants.SECKILL_SUCCESS_USER_ID + seckillId, userVO.getUserId())) {
            throw new BusinessException("您已经购买过秒杀商品，请勿重复购买");
        }
        Seckill seckill = redisCache.getCacheObject(Constants.SECKILL_KEY + seckillId);
        if (seckill == null) {
            seckill = getById(seckillId);
            redisCache.setCacheObject(Constants.SECKILL_KEY + seckillId, seckill, 24, TimeUnit.HOURS);
        }
        // 判断秒杀商品是否再有效期内
        long beginTime = seckill.getSeckillBegin().getTime();
        long endTime = seckill.getSeckillEnd().getTime();
        Date now = new Date();
        long nowTime = now.getTime();
        if (nowTime < beginTime) {
            throw new BusinessException("秒杀未开启");
        } else if (nowTime > endTime) {
            throw new BusinessException("秒杀已结束");
        }

        SeckillSuccess seckillSuccess = new SeckillSuccess();
        seckillSuccess.setSeckillId(seckillId);
        seckillSuccess.setUserId(userVO.getUserId());
        if (!seckillSuccessService.save(seckillSuccess)) {
            throw new BusinessException("保存用户秒杀商品失败");
        }
        SeckillSuccessVO seckillSuccessVO = new SeckillSuccessVO();
        Long seckillSuccessId = seckillSuccess.getSecId();
        seckillSuccessVO.setSeckillSuccessId(seckillSuccessId);
        seckillSuccessVO.setMd5(Md5Utils.hash(seckillSuccessId + Constants.SECKILL_EXECUTE_SALT));
        return seckillSuccessVO;
    }

    @Override
    public boolean reduceStock(Long seckillId, Date now) {
        return seckillMapper.reduceStock(seckillId, now);
    }
}
