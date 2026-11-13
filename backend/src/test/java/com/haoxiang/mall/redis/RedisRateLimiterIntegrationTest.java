package com.haoxiang.mall.redis;

import com.haoxiang.mall.config.StabilityProperties;
import com.haoxiang.mall.exception.FlowControlException;
import org.junit.jupiter.api.*;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RedisRateLimiterIntegrationTest {
    private LettuceConnectionFactory factory;
    private StringRedisTemplate redis;
    private StabilityProperties config;
    private String prefix;
    private List<String> keys;
    @BeforeEach void setup(){
        String password=System.getenv("HAOXIANG_TEST_REDIS_PASSWORD");Assumptions.assumeTrue(password!=null&&!password.isBlank());
        var settings=new RedisStandaloneConfiguration("127.0.0.1",16379);settings.setPassword(password);
        factory=new LettuceConnectionFactory(settings);factory.afterPropertiesSet();factory.start();redis=new StringRedisTemplate(factory);redis.afterPropertiesSet();
        prefix="haoxiang:test:"+UUID.randomUUID().toString().replace("-","");keys=new ArrayList<>();
        config=new StabilityProperties();config.setRateLimitPrefix(prefix);config.setLocalPermitsPerSecond(10000);config.setWindowMaxRequests(5);config.setBucketCapacity(100);config.setBucketRefillPerSecond(100);config.validate();
    }
    @AfterEach void cleanup(){if(redis!=null&&!keys.isEmpty())redis.delete(keys);if(factory!=null)factory.destroy();}
    private RedisRateLimiter instance(){RedisRateLimiter v=new RedisRateLimiter();ReflectionTestUtils.setField(v,"config",config);ReflectionTestUtils.setField(v,"redis",redis);v.init();return v;}
    private void record(Long user,Long activity){keys.add(prefix+":window:{user:"+user+"}:seckill:"+activity);keys.add(prefix+":bucket:{seckill:"+activity+"}");keys.add(prefix+":bucket:{seckill:all}");}
    @Test void slidingWindowLimitsSameUserAcrossInstances(){
        var a=instance();var b=instance();record(9L,18L);
        for(int i=0;i<5;i++)(i%2==0?a:b).submission(9L,18L);
        FlowControlException error=assertThrows(FlowControlException.class,()->a.submission(9L,18L));
        assertEquals(429,error.getStatus());assertTrue(error.getRetryAfter()>0);
    }
    @Test void redisBucketIsSharedByIndependentInstances(){
        config.setBucketCapacity(3);config.setBucketRefillPerSecond(1);var a=instance();var b=instance();
        for(long i=1;i<=3;i++){record(i,23L);(i%2==0?a:b).submission(i,23L);}
        record(4L,23L);assertEquals(429,assertThrows(FlowControlException.class,()->a.submission(4L,23L)).getStatus());
    }
}
