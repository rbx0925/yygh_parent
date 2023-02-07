package com.atguigu.yygh.order.service;

import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-02-05 11:24
 * @Description
 */
public interface WeixinService {
    /**
     * 根据订单号下单，生成支付链接
     */
    Map<String, Object> createNative(Long orderId);
    //调用微信接口获取查询结果
    Map<String, String> queryPayStatus(Long orderId, Integer paymentType);

    /***
     * 退款
     * @param orderId
     * @return
     */
    Boolean refund(Long orderId);
}
