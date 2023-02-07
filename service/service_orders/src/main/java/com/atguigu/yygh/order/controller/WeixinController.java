package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-02-05 11:27
 * @Description
 */
@Api(tags = "微信支付接口")
@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {
    @Autowired
    private WeixinService weixinPayService;

    @Autowired
    private PaymentService paymentService;
    /**
     * 下单 生成二维码
     */
    @GetMapping("/createNative/{orderId}")
    public R createNative(@PathVariable("orderId") Long orderId) {
        Map<String, Object> map = weixinPayService.createNative(orderId);
        return R.ok().data(map);
    }


    @ApiOperation(value = "查询支付状态")
    @GetMapping("/queryPayStatus/{orderId}")
    public R queryPayStatus(@PathVariable("orderId") Long orderId) {
        //1.根据参数调用微信接口获取查询结果
        Map<String,String>  resultMap = weixinPayService.queryPayStatus(orderId, PaymentTypeEnum.WEIXIN.getStatus());
        //2.判断是否支付失败
        if (resultMap==null){
            return R.error().message("支付失败");
        }
        //3.判断支付成功后,更新交易记录和订单记录
        if ("SUCCESS".equals(resultMap.get("trade_state"))){
            String out_trade_no = resultMap.get("out_trade_no");
            paymentService.paySuccess(out_trade_no,PaymentTypeEnum.WEIXIN.getStatus(),resultMap);
            return R.ok().message("支付成功");
        }
        //4.支付中情况
        return R.ok().message("支付中...");
    }
}
