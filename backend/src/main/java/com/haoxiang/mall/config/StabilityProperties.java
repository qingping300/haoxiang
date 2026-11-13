package com.haoxiang.mall.config;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/** 缓存、流量与订单后台任务的有界资源配置。 */
@Data
@Component
@ConfigurationProperties(prefix = "haoxiang.stability")
public class StabilityProperties {
    private int detailTtlSeconds = 300;
    private int detailJitterSeconds = 60;
    private int nullTtlSeconds = 30;
    private int nullJitterSeconds = 10;
    private int rebuildLockMillis = 3000;
    private int rebuildWaitMillis = 200;
    private int rebuildMaxConcurrent = 4;
    /** 单实例秒杀入口的本地速率。 */
    private double localPermitsPerSecond = 20;
    /** 同一用户、同一活动的连续窗口。 */
    private int windowSeconds = 10;
    private int windowMaxRequests = 5;
    /** 所有实例共享的秒杀令牌补充速率和桶容量。 */
    private int bucketRefillPerSecond = 20;
    private int bucketCapacity = 20;
    private String rateLimitPrefix = "haoxiang:limit";
    @PostConstruct
    public void validate() {
        if (detailTtlSeconds <= detailJitterSeconds || detailJitterSeconds < 0
                || nullTtlSeconds <= nullJitterSeconds || nullJitterSeconds < 0
                || rebuildLockMillis < 100 || rebuildLockMillis > 30000
                || rebuildWaitMillis < 0 || rebuildWaitMillis > 1000
                || rebuildMaxConcurrent < 1 || rebuildMaxConcurrent > 8
                || !Double.isFinite(localPermitsPerSecond) || localPermitsPerSecond <= 0 || localPermitsPerSecond > 10000
                || windowSeconds < 1 || windowSeconds > 3600 || windowMaxRequests < 1 || windowMaxRequests > 1000
                || rateLimitPrefix == null || !rateLimitPrefix.matches("[a-zA-Z0-9:_-]{1,100}")
                || bucketRefillPerSecond < 1 || bucketRefillPerSecond > 10000 || bucketCapacity < 1 || bucketCapacity > 10000
) {
            throw new IllegalArgumentException("haoxiang.stability 配置超出允许范围");
        }
    }
}
