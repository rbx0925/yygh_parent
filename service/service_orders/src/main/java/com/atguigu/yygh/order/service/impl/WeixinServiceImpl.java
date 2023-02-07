package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.ConstantPropertiesUtils;
import com.atguigu.yygh.order.utils.HttpClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author rbx
 * @title
 * @Create 2023-02-05 11:24
 * @Description
 */
@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private RefundInfoService refundInfoService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 根据订单号下单，生成支付链接
     */
    @Override
    public Map<String, Object> createNative(Long orderId) {
        try {
            //1.根据orderId查询订单信息
            OrderInfo orderInfo = orderService.getById(orderId);
            //2.根据订单信息,交易方式生成交易记录
            paymentService.savePaymentInfo(orderInfo,PaymentTypeEnum.WEIXIN.getStatus());
            //3.封装调用微信接口参数
            Map<String,String> paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = orderInfo.getReserveDate() + "就诊"+ orderInfo.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");//为了测试
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");
            //4.生成客户端对象(设置URL)
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //5.向客户端设置参数(map=>xml)
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY));
            //6.调用客户端方法发送请求,获得响应(xml)
            client.setHttps(true);
            client.post();
            //7.转化响应类型(xml=>map)
            String xml = client.getContent();
            System.out.println("获取二维码的xml = " + xml);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //8.封装返回的结果信息
            HashMap<String, Object> map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, String> queryPayStatus(Long orderId, Integer paymentType) {
        try {
            //1.查询交易记录
            QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("order_id",orderId);
            wrapper.eq("payment_type",paymentType);
            PaymentInfo paymentInfo = paymentService.getOne(wrapper);
            //2.封装调用微信接口参数
            Map paramMap = new HashMap<>();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", paymentInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //3.创建客户端对象,设置URl
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            //4.向客户端设置参数(map=>xml)
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            //5.调用客户端方法发送请求,获得响应(xml)
            client.setHttps(true);
            client.post();
            //6.转化响应类型(xml=>map)返回
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //微信退款
    @Override
    public Boolean refund(Long orderId) {
        try {
            //1.查询交易记录
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            if (paymentInfo==null){
                throw new YyghException(20001,"交易记录有误");
            }
            //2.创建退款记录,判断退款记录状态
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
            if (refundInfo.getRefundStatus().intValue()== RefundStatusEnum.REFUND.getStatus()) {
                return true;
            }
            //3.封装退款参数
            Map<String,String> paramMap = new HashMap<>(8);
            paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号
            //       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            //       paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY);
            //4.创建客户端对象设置url
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            //5.向客户端设置参数(map=>xml),开启https
            client.setXmlParam(paramXml);
            client.setHttps(true);
            //6.开启证书,设置证书密码
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            //7.调用客户端方法发送请求,获得响应(xml)
            client.post();
            //8.转化响应类型(xml=>map)返回
            String xml = client.getContent();
            System.out.println("退款的xml = " + xml);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            //9.判断是否成功,如成功更新退款记录
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
