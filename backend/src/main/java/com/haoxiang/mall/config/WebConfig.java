package com.haoxiang.mall.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.haoxiang.mall.interceptor.AdminLoginInterceptor;
import com.haoxiang.mall.interceptor.AdminViewModeInterceptor;
import com.haoxiang.mall.interceptor.MallLoginValidateInterceptor;
import com.haoxiang.mall.interceptor.RepeatSubmitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 配置前端入口、上传文件访问、日期格式和接口拦截器。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    @Autowired
    private HaoxiangMallConfig haoxiangMallConfig;

    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", haoxiangMallConfig.getFrontendUrl());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = haoxiangMallConfig.getUploadDir();
        if (uploadDir == null || uploadDir.isBlank()) {
            throw new IllegalArgumentException("haoxiang.upload-dir 不能为空");
        }
        // 获取上传目录的绝对路径
        String uploadLocation = Path.of(uploadDir).toAbsolutePath().normalize().toUri().toString();
        // 确保上传目录路径以斜杠结尾
        if (!uploadLocation.endsWith("/")) {
            uploadLocation += "/";
        }
        registry.addResourceHandler("/upload/**", "/goods-img/**")
                .addResourceLocations(uploadLocation);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 商城前台登录拦截器
        registry.addInterceptor(new MallLoginValidateInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/", "/index", "/login", "/logout", "/register", "/personal/info",
                        "/search", "/coupon", "/goods/**", "/shopCart/getUserShopCartCount",
                        "/seckill/list", "/seckill/detail/*", "/seckill/time/now", "/seckill/*/exposer",
                        "/upload/**", "/goods-img/**", "/common/**", "/mall/**", "/admin/**", "/tianai/**"
                );


        // 添加一个拦截器，拦截以/admin为前缀的url路径（后台登陆拦截）
        registry.addInterceptor(new AdminLoginInterceptor()).addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login").excludePathPatterns("/admin/dist/**")
                .excludePathPatterns("/admin/plugins/**");

        // 防止重复提交拦截器
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");

        // 后台演示模式拦截器
        registry.addInterceptor(new AdminViewModeInterceptor(haoxiangMallConfig))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login").excludePathPatterns("/admin")
                .excludePathPatterns("/admin/statistics").excludePathPatterns("/admin/goods")
                .excludePathPatterns("/admin/goods/add").excludePathPatterns("/admin/goods/edit/*")
                .excludePathPatterns("/admin/goods/list").excludePathPatterns("/admin/users")
                .excludePathPatterns("/admin/users/list").excludePathPatterns("/admin/carousels")
                .excludePathPatterns("/admin/carousels/list").excludePathPatterns("/admin/indexConfigs")
                .excludePathPatterns("/admin/indexConfigs/list").excludePathPatterns("/admin/categories")
                .excludePathPatterns("/admin/categories/list").excludePathPatterns("/admin/orders")
                .excludePathPatterns("/admin/categories/listForSelect").excludePathPatterns("/admin/orders")
                .excludePathPatterns("/admin/orders/list").excludePathPatterns("/admin/coupon")
                .excludePathPatterns("/admin/coupon/list").excludePathPatterns("/admin/seckill")
                .excludePathPatterns("/admin/seckill/list").excludePathPatterns("/admin/profile")
                .excludePathPatterns("/admin/logout").excludePathPatterns("/admin/dist/**")
                .excludePathPatterns("/admin/plugins/**");
    }

    /**
     * 统一 Java 时间类型的 JSON 表示，不覆盖 Spring Boot 的其他 Jackson 配置。
     */
    @Bean
    public static Jackson2ObjectMapperBuilderCustomizer javaTimeJsonCustomizer() {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);

        return builder -> builder
                .serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormat))
                .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormat))
                .serializerByType(LocalDate.class, new LocalDateSerializer(dateFormat))
                .deserializerByType(LocalDate.class, new LocalDateDeserializer(dateFormat))
                .serializerByType(LocalTime.class, new LocalTimeSerializer(timeFormat))
                .deserializerByType(LocalTime.class, new LocalTimeDeserializer(timeFormat))
                .featuresToDisable(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                        DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
