package com.haoxiang.mall.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.AdminUser;
import com.haoxiang.mall.core.service.AdminUserService;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.util.security.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import com.haoxiang.mall.util.R;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理系统个人资料管理控制器
 */
@RestController
@RequestMapping("admin/profile")
public class ProfileManagerController extends BaseController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public R profile(HttpServletRequest request) {
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        AdminUser adminUser = adminUserService.getById(loginUserId);
        if (adminUser == null) {
            return R.error("请先登录");
        }
        request.setAttribute("path", "profile");
        request.setAttribute("loginUserName", adminUser.getLoginUserName());
        request.setAttribute("nickName", adminUser.getNickName());
        return pageData(request);
    }

    @PostMapping("/password")
    @ResponseBody
    public String passwordUpdate(HttpServletRequest request,
                                 @RequestParam("originalPassword") String originalPassword,
                                 @RequestParam("newPassword") String newPassword) {
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        AdminUser adminUser = adminUserService.getById(loginUserId);
        String hash = Md5Utils.hash(originalPassword);
        if (adminUser == null || !adminUser.getLoginPassword().equals(hash)) {
            throw new BusinessException("原密码输入错误");
        }
        if (adminUserService.update()
                .set("login_password", Md5Utils.hash(newPassword))
                .eq("admin_user_id", loginUserId)
                .update()) {
            //修改成功后清空session中的数据，前端控制跳转至登录页
            request.getSession().removeAttribute("loginUserId");
            request.getSession().removeAttribute("loginUser");
            request.getSession().removeAttribute("errorMsg");
            return "success";
        } else {
            return "修改失败";
        }
    }

    @PostMapping("/name")
    @ResponseBody
    public String nameUpdate(HttpServletRequest request,
                             @RequestParam("loginUserName") String loginUserName,
                             @RequestParam("nickName") String nickName) {
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        AdminUser adminUser = adminUserService.getById(loginUserId);
        if (adminUser == null) {
            throw new BusinessException("服务器内部错误");
        }
        if (adminUserService.update()
                .set("login_user_name", loginUserName)
                .set("nick_name", nickName)
                .eq("admin_user_id", loginUserId)
                .update()) {
            // 修改成功后清空session中的数据，前端控制跳转至登录页
            request.getSession().setAttribute("loginUser", nickName);
            return "success";
        } else {
            return "修改失败";
        }
    }
}
