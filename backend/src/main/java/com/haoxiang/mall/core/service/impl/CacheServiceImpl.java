package com.haoxiang.mall.core.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.haoxiang.mall.config.StabilityProperties;
import com.haoxiang.mall.core.service.CacheService;
import com.haoxiang.mall.exception.FlowControlException;
import com.haoxiang.mall.redis.LuaScripts;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** 公共展示缓存：空值、随机 TTL、互斥重建、代次校验及有限回源。 */
@Service
public class CacheServiceImpl implements CacheService {
    @Resource private StringRedisTemplate redis;
    @Resource private StabilityProperties config;
    private Semaphore permits;
    private static final DefaultRedisScript<Long> FILL=LuaScripts.load("cache_fill");
    private static final DefaultRedisScript<Long> UNLOCK=LuaScripts.load("cache_unlock");
    private static final DefaultRedisScript<Long> INVALIDATE=LuaScripts.load("cache_invalidate");
    @PostConstruct public void init() { permits=new Semaphore(config.getRebuildMaxConcurrent()); }
    @Override public <T> T get(String key, Class<T> type, Supplier<T> loader) {
        String prefix="haoxiang:cache:{"+key+"}:";
        long deadline=System.nanoTime()+TimeUnit.MILLISECONDS.toNanos(config.getRebuildWaitMillis());
        for (;;) {
            String raw;
            try { raw=redis.opsForValue().get(prefix+"data"); }
            catch(RuntimeException unavailable) { return load(loader); }
            if(raw!=null) {
                JSONObject json;
                try { json=JSON.parseObject(raw); }
                catch(RuntimeException corrupt) { invalidate(key); throw new FlowControlException(503,1,"缓存数据正在刷新"); }
                if("NOT_FOUND".equals(json.getString("state"))) return null;
                return json.getObject("data",type);
            }
            String token=UUID.randomUUID().toString();
            boolean locked;
            try { locked=Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(prefix+"lock",token,Duration.ofMillis(config.getRebuildLockMillis()))); }
            catch(RuntimeException unavailable) { return load(loader); }
            if(locked) {
                try {
                    raw=redis.opsForValue().get(prefix+"data");
                    if(raw!=null) { JSONObject json=JSON.parseObject(raw);return "NOT_FOUND".equals(json.getString("state"))?null:json.getObject("data",type); }
                    String generation=redis.opsForValue().get(prefix+"generation");
                    T value=load(loader);
                    JSONObject envelope=new JSONObject();envelope.put("state",value==null?"NOT_FOUND":"VALUE");envelope.put("data",value);
                    int ttl=value==null?config.getNullTtlSeconds():config.getDetailTtlSeconds();
                    int jitter=value==null?config.getNullJitterSeconds():config.getDetailJitterSeconds();
                    ttl+=ThreadLocalRandom.current().nextInt(-jitter,jitter+1);
                    redis.execute(FILL,List.of(prefix+"lock",prefix+"generation",prefix+"data"),token,generation==null?"0":generation,envelope.toJSONString(),String.valueOf(ttl*1000L));
                    return value;
                } finally { try { redis.execute(UNLOCK,List.of(prefix+"lock"),token); } catch(RuntimeException ignored) { /* 租期到期释放，不覆盖原查询结果。 */ } }
            }
            if(System.nanoTime()>=deadline) throw new FlowControlException(503,1,"商品信息正在刷新，请稍后重试");
            try { Thread.sleep(ThreadLocalRandom.current().nextInt(20,51)); }
            catch(InterruptedException e) { Thread.currentThread().interrupt();throw new FlowControlException(503,1,"请求已中断"); }
        }
    }
    private <T> T load(Supplier<T> loader) {
        if(!permits.tryAcquire()) throw new FlowControlException(503,1,"查询繁忙，请稍后重试");
        try { return loader.get(); } finally { permits.release(); }
    }
    @Override public void invalidate(String key) {
        String prefix="haoxiang:cache:{"+key+"}:";
        redis.execute(INVALIDATE,List.of(prefix+"generation",prefix+"data"));
    }
}
