package com.haoxiang.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.IndexConfig;
import com.haoxiang.mall.core.service.IndexConfigService;
import com.haoxiang.mall.enums.IndexConfigTypeEnum;
import com.haoxiang.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理-首页配置管理
 */
@RestController
@RequestMapping("admin/indexConfigs")
public class IndexCfgManagerController extends BaseController {

    @Autowired
    private IndexConfigService indexConfigService;

    @GetMapping
    public R index(HttpServletRequest request, @RequestParam("configType") int configType) {
        IndexConfigTypeEnum indexConfigTypeEnumByType = IndexConfigTypeEnum.getIndexConfigTypeEnumByType(configType);
        request.setAttribute("path", indexConfigTypeEnumByType.getName());
        request.setAttribute("configType", configType);
        return pageData(request);
    }

    @ResponseBody
    @GetMapping("/list")
    public IPage<IndexConfig> list(IndexConfig indexConfig, HttpServletRequest request) {
        Page<IndexConfig> page = getPage(request);
        return indexConfigService.selectPage(page, indexConfig);
    }

    /**
     * 保存
     *
     * @param indexConfig
     * @return
     */
    @ResponseBody
    @PostMapping("/save")
    public R save(@RequestBody IndexConfig indexConfig) {
        baseFieldHandle(indexConfig, true);
        return R.result(indexConfigService.save(indexConfig));
    }


    /**
     * 更新
     *
     * @param indexConfig
     * @return
     */
    @ResponseBody
    @PostMapping("/update")
    public R update(@RequestBody IndexConfig indexConfig) {
        baseFieldHandle(indexConfig, false);
        return R.result(indexConfigService.updateById(indexConfig));
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ResponseBody
    public R delete(@RequestBody List<Integer> ids) {
        return R.result(indexConfigService.removeByIds(ids));
    }
}
