package com.haoxiang.mall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 商城地址、上传目录和后台访问规则。
 */
@Data
@Component
@ConfigurationProperties(prefix = "haoxiang")
public class HaoxiangMallConfig {

    private String frontendUrl;
    private String uploadDir;
    private boolean viewMode;
    private String viewModeTip;
}
