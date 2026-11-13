package com.haoxiang.mall.controller.mall;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.MallUser;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.service.MallUserService;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.util.R;
import com.haoxiang.mall.util.security.Md5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

/**
 * 前台商城-用户模块
 */
@RestController
public class MallUserController extends BaseController {

    @Autowired
    private MallUserService mallUserService;

    @GetMapping("/personal")
    public R personalPage(HttpServletRequest request) {
        request.setAttribute("path", "personal");
        return pageData(request);
    }

    @GetMapping("/logout")
    public R logout(HttpSession httpSession) {
        httpSession.removeAttribute(Constants.MALL_USER_SESSION_KEY);
        return R.success();
    }

    @GetMapping("/login")
    public R loginPage(HttpServletRequest request) {
        return R.success();
    }

    @ResponseBody
    @PostMapping("/login")
    public R doLogin(MallUserVO mallUserVO,
                     @RequestParam("destPath") String destPath,
                     HttpSession session) {
        R success = R.success();
        MallUser user = mallUserService.getOne(Wrappers.<MallUser>lambdaQuery()
                .eq(MallUser::getLoginName, mallUserVO.getLoginName())
                .eq(MallUser::getPasswordMd5, Md5Utils.hash(mallUserVO.getPassword())));
        if (user == null) {
            return R.error("账户名称或者密码错误");
        }
        if (user.getLockedFlag() == 1) {
            return R.error("该账户已被禁用");
        }
        BeanUtils.copyProperties(user, mallUserVO);
        mallUserVO.setPassword(null);
        session.setAttribute(Constants.MALL_USER_SESSION_KEY, mallUserVO);
        int timeout = session.getMaxInactiveInterval();
        if (mallUserVO.isRememberme()) {
            session.setMaxInactiveInterval(timeout * 24 * 7);
        } else {
            session.setMaxInactiveInterval(timeout);
        }
        if (StringUtils.isNotEmpty(destPath) && StringUtils.contains(destPath, "=")) {
            success.add("destPath", destPath.split("=")[1].substring(1));
        }
        return success;
    }

    @GetMapping("/register")
    public R registerPage() {
        return R.success();
    }

    @ResponseBody
    @PostMapping("/register")
    public R register(@RequestParam("loginName") String loginName,
                      @RequestParam("verifyCode") String verifyCode,
                      @RequestParam("password") String password,
                      @RequestParam("repassword") String repassword,
                      HttpSession session) {
        String kaptchaCode = (String) session.getAttribute(Constants.MALL_VERIFY_CODE_KEY);
        if (!StringUtils.equalsIgnoreCase(verifyCode, kaptchaCode)) {
            return R.error("验证码错误");
        }

        // 判断密码是否一致
        if (!StringUtils.equals(repassword, password)) {
            return R.error("两次密码不一致");
        }

        // 查询用户账号是否已经注册
        long count = mallUserService.count(new QueryWrapper<MallUser>()
                .eq("login_name", loginName));
        if (count > 0) {
            return R.error("该账户名已存在");
        }
        return R.result(mallUserService.register(loginName, password));
    }

    @PostMapping("/personal/updateInfo")
    @ResponseBody
    public R updateInfo(@RequestBody MallUser mallUser) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        mallUser.setUserId(mallUserVO.getUserId());
        if (!mallUserService.updateById(mallUser)) {
            throw new BusinessException("修改用户信息异常");
        }
        MallUser user = mallUserService.getById(mallUserVO.getUserId());
        MallUserVO tempUser = new MallUserVO();
        BeanUtils.copyProperties(user, tempUser);
        session.setAttribute(Constants.MALL_USER_SESSION_KEY, tempUser);
        return R.success();
    }

    @GetMapping("/personal/info")
    @ResponseBody
    public R info() {
        Object obj = session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        if (obj instanceof MallUserVO mallUserVO) {
            return R.success().add("userInfo", mallUserVO);
        }
        return R.error();
    }
}
