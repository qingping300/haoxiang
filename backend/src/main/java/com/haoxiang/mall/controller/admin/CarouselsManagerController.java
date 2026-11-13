package com.haoxiang.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.Carousels;
import com.haoxiang.mall.core.service.CarouselsService;
import com.haoxiang.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理-轮播图管理
 */
@RestController
@RequestMapping("admin/carousels")
public class CarouselsManagerController extends BaseController {

    @Autowired
    private CarouselsService carouselsService;

    @GetMapping
    public R index(HttpServletRequest request) {
        request.setAttribute("path", "haoxiang_mall_carousel");
        return pageData(request);
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    @ResponseBody
    public IPage<Carousels> list(Carousels carousels, HttpServletRequest request) {
        Page<Carousels> page = getPage(request);
        return carouselsService.selectPage(page, carousels);
    }

    /**
     * 新增
     */
    @PostMapping("/save")
    @ResponseBody
    public R save(@RequestBody Carousels carousels) {
        baseFieldHandle(carousels, true);
        return R.result(carouselsService.save(carousels));
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ResponseBody
    public R update(@RequestBody Carousels carousels) {
        baseFieldHandle(carousels, false);
        return R.result(carouselsService.updateById(carousels));
    }

    /**
     * 详情
     */
    @GetMapping("/info/{id}")
    @ResponseBody
    public R Info(@PathVariable("id") Integer id) {
        return R.success().add("data", carouselsService.getById(id));
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ResponseBody
    public R delete(@RequestBody List<Integer> ids) {
        return R.result(carouselsService.removeByIds(ids));
    }
}
