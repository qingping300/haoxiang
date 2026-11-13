package com.haoxiang.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.MallUser;
import com.haoxiang.mall.core.service.MallUserService;
import com.haoxiang.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 后台管理-会员管理
 */
@RestController
@RequestMapping("admin/users")
public class MemberManagerController extends BaseController {

    @Autowired
    private MallUserService mallUserService;

    @GetMapping
    public R index(HttpServletRequest request) {
        request.setAttribute("path", "users");
        return pageData(request);
    }

    @ResponseBody
    @GetMapping("/list")
    public IPage<MallUser> list(MallUser mallUser, HttpServletRequest request) {
        Page<MallUser> page = getPage(request);
        return mallUserService.selectPage(page, mallUser);
    }

    /**
     * 用户禁用与解除禁用(0-未锁定 1-已锁定)
     */
    @RequestMapping(value = "/lock/{lockStatus}", method = RequestMethod.POST)
    @ResponseBody
    public R delete(@RequestBody List<Integer> ids, @PathVariable int lockStatus) {
        if (ids.size() < 1) {
            return R.error("参数异常！");
        }
        if (lockStatus != 0 && lockStatus != 1) {
            return R.error("操作非法！");
        }
        boolean update = mallUserService.update().set("locked_flag ", lockStatus).in("user_id", ids).update();
        return R.result(update);
    }
}
