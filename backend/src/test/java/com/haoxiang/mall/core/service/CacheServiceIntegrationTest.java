package com.haoxiang.mall.core.service;

import com.haoxiang.mall.config.StabilityProperties;
import com.haoxiang.mall.core.service.impl.CacheServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

/** 使用单独随机键验证 Redis 缓存；未设置密码时不运行集成测试。 */
class CacheServiceIntegrationTest {
    private LettuceConnectionFactory factory;
    private StringRedisTemplate redis;
    private CacheServiceImpl service;
    private String key;
    @BeforeEach void setup() {
        String password=System.getenv("HAOXIANG_TEST_REDIS_PASSWORD");
        Assumptions.assumeTrue(password!=null && !password.isBlank());
        var config=new RedisStandaloneConfiguration("127.0.0.1",16379);config.setPassword(password);
        factory=new LettuceConnectionFactory(config);factory.afterPropertiesSet();factory.start();
        redis=new StringRedisTemplate(factory);redis.afterPropertiesSet();
        service=new CacheServiceImpl();var props=new StabilityProperties();props.validate();
        ReflectionTestUtils.setField(service,"redis",redis);ReflectionTestUtils.setField(service,"config",props);service.init();
        key="integration:"+UUID.randomUUID();
    }
    @AfterEach void cleanup() {
        if(redis!=null) redis.delete(List.of(prefix()+"data",prefix()+"lock",prefix()+"generation"));
        if(factory!=null) factory.destroy();
    }
    private String prefix(){return "haoxiang:cache:{"+key+"}:";}
    @Test void cachesConfirmedMissingAndUsesShortRandomTtl() {
        AtomicInteger count=new AtomicInteger();
        assertNull(service.get(key,String.class,()->{count.incrementAndGet();return null;}));
        assertNull(service.get(key,String.class,()->{count.incrementAndGet();return "incorrect";}));
        assertEquals(1,count.get());long ttl=redis.getExpire(prefix()+"data",TimeUnit.SECONDS);assertTrue(ttl>=19 && ttl<=40,"ttl="+ttl);
    }
    @Test void cachesValuesWithJitterAndInvalidates() {
        assertEquals("old",service.get(key,String.class,()->"old"));
        long ttl=redis.getExpire(prefix()+"data",TimeUnit.SECONDS);assertTrue(ttl>=239 && ttl<=360,"ttl="+ttl);
        service.invalidate(key);assertEquals("new",service.get(key,String.class,()->"new"));
    }
    @Test void loaderFailureDoesNotBecomeAnEmptyResult() {
        assertThrows(IllegalStateException.class,()->service.get(key,String.class,()->{throw new IllegalStateException("database failed");}));
        assertNull(redis.opsForValue().get(prefix()+"data"));
        assertEquals("ok",service.get(key,String.class,()->"ok"));
    }
    @Test void simultaneousMissesUseOneRebuild() throws Exception {
        var count=new AtomicInteger();var ready=new CountDownLatch(16);var start=new CountDownLatch(1);
        ExecutorService pool=Executors.newFixedThreadPool(16);try {
            List<Future<String>> futures=new ArrayList<>();
            for(int i=0;i<16;i++) futures.add(pool.submit(()->{ready.countDown();start.await();return service.get(key,String.class,()->{count.incrementAndGet();try{Thread.sleep(50);}catch(InterruptedException e){Thread.currentThread().interrupt();}return "value";});}));
            assertTrue(ready.await(3,TimeUnit.SECONDS));start.countDown();for(Future<String> f:futures)assertEquals("value",f.get(5,TimeUnit.SECONDS));assertEquals(1,count.get());
        } finally {pool.shutdownNow();}
    }
    @Test void slowOldQueryCannotRepopulateAfterInvalidation() throws Exception {
        var entered=new CountDownLatch(1);var proceed=new CountDownLatch(1);ExecutorService pool=Executors.newSingleThreadExecutor();
        try {
            Future<String> old=pool.submit(()->service.get(key,String.class,()->{entered.countDown();try{proceed.await();}catch(InterruptedException e){Thread.currentThread().interrupt();}return "old";}));
            assertTrue(entered.await(3,TimeUnit.SECONDS));service.invalidate(key);proceed.countDown();assertEquals("old",old.get(3,TimeUnit.SECONDS));
            assertNull(redis.opsForValue().get(prefix()+"data"));assertEquals("new",service.get(key,String.class,()->"new"));
        } finally {proceed.countDown();pool.shutdownNow();}
    }
}
