package com.haoxiang.mall.config;

import com.haoxiang.mall.annotation.RepeatSubmit;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.interceptor.AdminLoginInterceptor;
import com.haoxiang.mall.interceptor.AdminViewModeInterceptor;
import com.haoxiang.mall.interceptor.MallLoginValidateInterceptor;
import com.haoxiang.mall.interceptor.impl.SameUrlDataInterceptor;
import com.haoxiang.mall.interceptor.threadlocal.AdminLoginThreadLocal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebConfigInterceptorsTest {

    private final HandlerMethod handler = new HandlerMethod(new TestController(),
            TestController.class.getDeclaredMethod("save"));

    WebConfigInterceptorsTest() throws NoSuchMethodException {
    }

    @Test
    void webConfigAndJacksonInitializeWithoutCircularDependency() {
        HaoxiangMallConfig config = new HaoxiangMallConfig();
        config.setUploadDir("./data/upload");
        config.setFrontendUrl("http://127.0.0.1:5173/");
        config.setViewModeTip("演示模式只允许查看");

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .withBean(HaoxiangMallConfig.class, () -> config)
                .withUserConfiguration(WebConfig.class, SameUrlDataInterceptor.class)
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    assertTrue(context.containsBean("javaTimeJsonCustomizer"));
                    assertTrue(context.containsBean("webConfig"));
                });
    }

    @Test
    void adminRequiresSessionAndClearsThreadLocal() throws Exception {
        AdminLoginInterceptor interceptor = new AdminLoginInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/goods/save");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, handler));
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("请登陆"));
        assertEquals("请登陆", request.getSession().getAttribute("errorMsg"));

        request.getSession().setAttribute("loginUserId", 7);
        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), handler));
        assertEquals(7, AdminLoginThreadLocal.get());
        assertNull(request.getSession().getAttribute("errorMsg"));
        interceptor.afterCompletion(request, response, handler, null);
        assertEquals(0, AdminLoginThreadLocal.get());
    }

    @Test
    void mallRequiresSession() throws Exception {
        MallLoginValidateInterceptor interceptor = new MallLoginValidateInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/shopCart");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, handler));
        assertEquals(401, response.getStatus());
        request.getSession().setAttribute(Constants.MALL_USER_SESSION_KEY, new Object());
        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), handler));
    }

    @Test
    void viewModeBlocksMatchedRequests() throws Exception {
        HaoxiangMallConfig config = new HaoxiangMallConfig();
        config.setViewMode(true);
        config.setViewModeTip("演示模式只允许查看");
        AdminViewModeInterceptor interceptor = new AdminViewModeInterceptor(config);

        MockHttpServletResponse read = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(new MockHttpServletRequest("GET", "/admin/goods/list"),
                read, handler));
        assertEquals(200, read.getStatus());
        assertTrue(read.getContentAsString().contains("\"code\":500"));
        MockHttpServletResponse blocked = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(new MockHttpServletRequest("POST", "/admin/goods/save"),
                blocked, handler));
        assertEquals(200, blocked.getStatus());
        config.setViewMode(false);
        assertTrue(interceptor.preHandle(new MockHttpServletRequest("POST", "/admin/goods/save"),
                new MockHttpServletResponse(), handler));
    }

    @Test
    void repeatSubmitChecksFormParameters() throws Exception {
        SameUrlDataInterceptor interceptor = new SameUrlDataInterceptor();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/admin/goods/save");
        request.setParameter("name", "item-a");
        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), handler));
        MockHttpServletResponse duplicate = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(request, duplicate, handler));
        assertEquals(200, duplicate.getStatus());
        assertTrue(duplicate.getContentAsString().contains("\"code\":500"));

        request.setParameter("name", "item-b");
        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), handler));

    }

    static class TestController {
        @RepeatSubmit
        public void save() {
        }
    }
}
