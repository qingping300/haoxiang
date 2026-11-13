package com.haoxiang.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.AdminUser;
import com.haoxiang.mall.core.service.AdminUserService;
import com.haoxiang.mall.util.security.Md5Utils;
import com.haoxiang.mall.util.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 管理端登录，登出
 */
@RestController
@RequestMapping("admin")
public class AdminIndexController extends BaseController {
    @Autowired
    private AdminUserService adminUserService;

    @RequestMapping
    public R index(HttpServletRequest request) {
        request.setAttribute("path", "index");
        return pageData(request);
    }

    @RequestMapping("login")
    public R login() {
        return R.success();
    }

    @PostMapping(value = "/login")
    public R login(@RequestParam("userName") String userName,
                        @RequestParam("password") String password,
                        @RequestParam("verifyCode") String verifyCode,
                        HttpSession session) {
        if (StringUtils.isEmpty(verifyCode)) {
            session.setAttribute("errorMsg", "验证码不能为空");
            return R.error((String) session.getAttribute("errorMsg"));
        }
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password)) {
            session.setAttribute("errorMsg", "用户名或密码不能为空");
            return R.error((String) session.getAttribute("errorMsg"));
        }
        // 获取session中的验证码
        String kaptchaCode = (String) session.getAttribute(Constants.MALL_VERIFY_CODE_KEY);
        if (StringUtils.isEmpty(kaptchaCode) || !verifyCode.equals(kaptchaCode)) {
            session.setAttribute("errorMsg", "验证码错误");
            return R.error((String) session.getAttribute("errorMsg"));
        }
        AdminUser adminUser = adminUserService.getOne(new QueryWrapper<AdminUser>()
                .eq("login_user_name", userName)
                .eq("login_password", Md5Utils.hash(password)));//密码MD5加密后和数据库中的密码进行比较
        if (adminUser != null) {
            session.setAttribute("loginUser", adminUser.getNickName());
            session.setAttribute("loginUserId", adminUser.getAdminUserId());
            return R.success().add("loginUser", adminUser.getNickName());
        } else {
            session.setAttribute("errorMsg", "用户名或密码错误");
            return R.error((String) session.getAttribute("errorMsg"));
        }
    }

    @GetMapping("/logout")
    public R logout(HttpServletRequest request) {
        request.getSession().removeAttribute("loginUserId");
        request.getSession().removeAttribute("loginUser");
        request.getSession().removeAttribute("errorMsg");
        return R.success();
    }

}
