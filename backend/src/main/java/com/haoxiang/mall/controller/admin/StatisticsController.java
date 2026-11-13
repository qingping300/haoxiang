package com.haoxiang.mall.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.vo.DayTransactionAmountVO;
import com.haoxiang.mall.core.service.OrderService;
import com.haoxiang.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * 管理员统计模块
 */
@RestController
@RequestMapping("admin/statistics")
public class StatisticsController extends BaseController {

    private static final Integer DEFAULT_DAY_NUM = 365;

    @Autowired
    private OrderService orderService;

    @GetMapping
    public R index(HttpServletRequest request) {
        List<DayTransactionAmountVO> countMallVOS = orderService.countMallTransactionAmount(DEFAULT_DAY_NUM);
        ArrayList<String> xAxisData = new ArrayList<>();
        ArrayList<Long> seriesData = new ArrayList<>();
        for (DayTransactionAmountVO countMallVO : countMallVOS) {
            xAxisData.add(countMallVO.getDays());
            seriesData.add(countMallVO.getTotalPrice());
        }
        request.setAttribute("defaultDayNum", DEFAULT_DAY_NUM);
        request.setAttribute("xAxisData", xAxisData);
        request.setAttribute("seriesData", seriesData);
        return pageData(request);
    }

    @ResponseBody
    @GetMapping("/transactionAmount/{dayNum}")
    public R selectDayTransactionAmount(@PathVariable("dayNum") Integer dayNum) {
        List<DayTransactionAmountVO> countMallVOS = orderService.countMallTransactionAmount(dayNum);
        ArrayList<String> xAxisData = new ArrayList<>();
        ArrayList<Long> seriesData = new ArrayList<>();
        for (DayTransactionAmountVO countMallVO : countMallVOS) {
            xAxisData.add(countMallVO.getDays());
            seriesData.add(countMallVO.getTotalPrice());
        }
        return R.success().add("xAxisData", xAxisData).add("seriesData", seriesData);
    }
}
