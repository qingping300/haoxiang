package com.haoxiang.mall.controller.mall;

import com.haoxiang.mall.constant.Constants;
import com.haoxiang.mall.controller.base.BaseController;
import com.haoxiang.mall.core.entity.MallUser;
import com.haoxiang.mall.core.entity.Order;
import com.haoxiang.mall.core.entity.vo.MallUserVO;
import com.haoxiang.mall.core.service.MallUserService;
import com.haoxiang.mall.core.service.OrderService;
import com.haoxiang.mall.enums.OrderStatusEnum;
import com.haoxiang.mall.enums.PayStatusEnum;
import com.haoxiang.mall.enums.PayTypeEnum;
import com.haoxiang.mall.exception.BusinessException;
import com.haoxiang.mall.util.R;
import com.haoxiang.mall.util.security.Md5Utils;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 前台商城-订单支付相关控制器
 */
@RestController
public class MallPayController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MallUserService mallUserService;

    @GetMapping("/selectPayType")
    public R selectPayType(@RequestParam("orderNo") String orderNo, HttpSession session) {
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.judgeOrderUserId(orderNo, mallUserVO.getUserId());
        // 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
            throw new BusinessException("订单结算异常");
        }
        return R.success().add("orderNo", orderNo).add("totalPrice", order.getTotalPrice());
    }

    @PostMapping("/payPage")
    public R payOrder(@RequestParam("orderNo") String orderNo,
                      @RequestParam("password") String password,
                      @RequestParam(value = "payType", defaultValue = "1") int payType,
                      HttpSession session) {
        if (payType != PayTypeEnum.PASSWORD_VERIFY.getPayType() || StringUtils.isBlank(password)) {
            throw new BusinessException("支付参数异常");
        }
        MallUserVO mallUserVO = (MallUserVO) session.getAttribute(Constants.MALL_USER_SESSION_KEY);
        Order order = orderService.judgeOrderUserId(orderNo, mallUserVO.getUserId());
        // 判断订单状态
        if (order.getOrderStatus() != OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus()
                || order.getPayStatus() != PayStatusEnum.PAY_ING.getPayStatus()) {
            throw new BusinessException("订单结算异常");
        }
        MallUser user = mallUserService.getById(mallUserVO.getUserId());
        if (user == null || !StringUtils.equals(user.getPasswordMd5(), Md5Utils.hash(password))) {
            throw new BusinessException("登录密码错误");
        }
        Date now = new Date();
        boolean updated = orderService.update()
                .set("order_status", OrderStatusEnum.OREDER_PAID.getOrderStatus())
                .set("pay_status", PayStatusEnum.PAY_SUCCESS.getPayStatus())
                .set("pay_type", PayTypeEnum.PASSWORD_VERIFY.getPayType())
                .set("pay_time", now)
                .set("update_time", now)
                .eq("order_id", order.getOrderId())
                .eq("order_status", OrderStatusEnum.ORDER_PRE_PAY.getOrderStatus())
                .eq("pay_status", PayStatusEnum.PAY_ING.getPayStatus())
                .update();
        if (!updated) {
            throw new BusinessException("订单状态更新异常");
        }
        return R.success().add("orderNo", orderNo);
    }
}
