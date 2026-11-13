package com.haoxiang.mall.interceptor;

import com.alibaba.fastjson.JSON;
import com.haoxiang.mall.config.HaoxiangMallConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * 演示模式拦截器
 */
public class AdminViewModeInterceptor implements HandlerInterceptor {

    private final HaoxiangMallConfig haoxiangMallConfig;

    public AdminViewModeInterceptor(HaoxiangMallConfig haoxiangMallConfig) {
        this.haoxiangMallConfig = haoxiangMallConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        if (haoxiangMallConfig.isViewMode()) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 500);
            error.put("msg", haoxiangMallConfig.getViewModeTip());
            error.put("map", new HashMap<>());
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(JSON.toJSONString(error));
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
