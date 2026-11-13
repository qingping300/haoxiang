package com.haoxiang.mall.annotation;

import com.haoxiang.mall.aspect.LimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义接口限流
 *
 * @author jacky
 */
@Target(ElementType.METHOD)// 方法级别
@Retention(RetentionPolicy.RUNTIME)// 运行时保留，可以被反射读出来
public @interface Limit {

    /**
     * 资源名称，用于描述接口功能
     */
    String name() default "";

    /**
     * 资源 key
     */
    String key() default "";

    /**
     * key prefix
     */
    String prefix() default "";

    /**
     * 时间的，单位秒
     */
    int period();

    /**
     * 限制访问次数
     */
    int count();

    /**
     * 限制类型(接口方法维度/ip用户维度)
     */
    LimitType limitType() default LimitType.CUSTOMER;

}
