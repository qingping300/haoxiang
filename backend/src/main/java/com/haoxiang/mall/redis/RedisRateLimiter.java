package com.haoxiang.mall.redis;
import com.google.common.util.concurrent.RateLimiter;
import com.haoxiang.mall.config.StabilityProperties;
import com.haoxiang.mall.exception.FlowControlException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import java.util.*;
/** 实例粗筛、用户滑动窗口、共享活动令牌桶各自承担一个流量维度。 */
@Component
public class RedisRateLimiter {
    @Resource private StabilityProperties config;
    @Resource(name = "stringRedisTemplate") private StringRedisTemplate redis;
    private RateLimiter local;
    private static final DefaultRedisScript<Long> WINDOW=LuaScripts.load("sliding_window");
    private static final DefaultRedisScript<Long> BUCKET=LuaScripts.load("token_bucket");
    @PostConstruct public void init() { local=RateLimiter.create(config.getLocalPermitsPerSecond()); }
    public void local() {
        if (!local.tryAcquire()) throw new FlowControlException(429,1,"请求较多，请稍后重试");
    }
    public void submission(Long userId, Long activityId) {
        if (userId == null || activityId == null || userId <= 0 || activityId <= 0)
            throw new IllegalArgumentException("用户和活动不能为空");
        local();
        try {
            Long wait=redis.execute(WINDOW,List.of(config.getRateLimitPrefix()+":window:{user:"+userId+"}:seckill:"+activityId),
                    String.valueOf(config.getWindowSeconds()*1000),String.valueOf(config.getWindowMaxRequests()),UUID.randomUUID().toString());
            reject(wait);
            // 总入口与单活动额度分开，多个活动也受共享数据库容量约束。
            reject(redis.execute(BUCKET,List.of(config.getRateLimitPrefix()+":bucket:{seckill:all}"),String.valueOf(config.getBucketRefillPerSecond()),String.valueOf(config.getBucketCapacity())));
            reject(redis.execute(BUCKET,List.of(config.getRateLimitPrefix()+":bucket:{seckill:"+activityId+"}"),String.valueOf(config.getBucketRefillPerSecond()),String.valueOf(config.getBucketCapacity())));
        } catch (FlowControlException e) { throw e; }
        catch (RuntimeException e) { throw new FlowControlException(503,2,"秒杀服务暂不可用，请稍后重试"); }
    }
    private void reject(Long wait) {
        if(wait==null) throw new FlowControlException(503,2,"限流服务暂不可用");
        if(wait>0) throw new FlowControlException(429,(int)Math.max(1,(wait+999)/1000),"操作频繁，请稍后重试");
    }
}
