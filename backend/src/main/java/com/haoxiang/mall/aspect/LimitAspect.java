package com.haoxiang.mall.aspect;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.ImmutableList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import com.haoxiang.mall.annotation.Limit;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.util.IpUtils;
import com.haoxiang.mall.util.R;
import com.haoxiang.mall.util.ServletUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 限制器切面
 */
@Slf4j
@Aspect
@Component
public class LimitAspect {

    @Autowired
    public RedisTemplate<Object, Object> redisTemplate;

    @Pointcut("@annotation(com.haoxiang.mall.annotation.Limit)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求对象
        HttpServletRequest request = ServletUtil.getRequest();

        // 反射获取目标方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method signatureMethod = signature.getMethod();
        Limit limit = signatureMethod.getAnnotation(Limit.class);
        // 获取限制类型
        LimitType limitType = limit.limitType();
        // 获取限制的key
        String key = limit.key();
        // 如果key为空，则使用默认的key
        if (StringUtils.isEmpty(key)) {
            if (limitType == LimitType.IP) {
                key = IpUtils.getIpAddr(request);//按IP限流
            } else {
                key = signatureMethod.getName();//按方法名限流
            }
        }
        // 拼接key
        ImmutableList<Object> keys = ImmutableList.of(StringUtils.join(limit.prefix(), "_", key, "_", request.getRequestURI().replaceAll("/", "_")));

        // 获取lua脚本
        String luaScript = buildLuaScript();
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long count = redisTemplate.execute(redisScript, keys, limit.count(), limit.period());
        if (null != count && count.intValue() <= limit.count()) {
            log.debug("第{}次访问key为 {}，描述为 [{}] 的接口", count, keys, limit.name());
            return joinPoint.proceed();
        } else {
            // throw new BusinessException("访问次数受限制");
            return R.error("访问次数受限制");
        }
    }

    /**
     * 限流脚本
     */
    private String buildLuaScript() {
        return """
                local c
                c = redis.call('get',KEYS[1])
                if c and tonumber(c) > tonumber(ARGV[1]) then
                return c;
                end
                c = redis.call('incr',KEYS[1])
                if tonumber(c) == 1 then
                redis.call('expire',KEYS[1],ARGV[2])
                end
                return c;""";
    }
}
