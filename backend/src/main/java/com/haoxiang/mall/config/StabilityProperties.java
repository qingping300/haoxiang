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
    @PostConstruct
    public void validate() {
        if (detailTtlSeconds <= detailJitterSeconds || detailJitterSeconds < 0
                || nullTtlSeconds <= nullJitterSeconds || nullJitterSeconds < 0
                || rebuildLockMillis < 100 || rebuildLockMillis > 30000
                || rebuildWaitMillis < 0 || rebuildWaitMillis > 1000
                || rebuildMaxConcurrent < 1 || rebuildMaxConcurrent > 8
) {
            throw new IllegalArgumentException("haoxiang.stability 配置超出允许范围");
        }
    }
}
