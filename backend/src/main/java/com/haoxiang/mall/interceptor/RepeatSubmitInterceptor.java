package com.haoxiang.mall.interceptor;

import com.alibaba.fastjson.JSON;
import com.haoxiang.mall.annotation.RepeatSubmit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 防重复提交拦截器
 */
@Component
public abstract class RepeatSubmitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            Method method = handlerMethod.getMethod();
            // 获取方法上的注解
            RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
            if (annotation != null) {
                if (this.isRepeatSubmit(request)) {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf-8");
                    response.getWriter().print(JSON.toJSONString(Map.of(
                            "code", 500, "msg", "不允许重复提交，请稍后再试", "map", Map.of())));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 验证是否重复提交由子类实现具体地防重复提交的规则
     *
     * @param request 请求对象
     * @return boolean
     */
    public abstract boolean isRepeatSubmit(HttpServletRequest request) throws Exception;
}
