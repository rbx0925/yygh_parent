package com.atguigu.yygh.msm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.msm.service.MsmService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-28 16:31
 * @Description
 */
@Service
public class MsmServiceImpl implements MsmService {
    @Override
    public boolean send(String phone, Map<String, String> param) {
        //1.手机号验空
        if(StringUtils.isEmpty(phone)) return false;
        //2.创建客户端对象
        DefaultProfile profile = DefaultProfile.getProfile("default", "LTAI5tJ5Lnv7yi4wn7iZwmHt", "Aehh6v8cshZAiIDuMWwN1DpxtCbLmw");
        IAcsClient client = new DefaultAcsClient(profile);
        //3.创建请求对象, 设置参数
        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");

        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", "任宝鑫短信服务");//老师=我的谷粒在线教育网站 自己=任宝鑫短信服务
        request.putQueryParameter("TemplateCode", "SMS_268615776");//老师=SMS_183195440,自己=SMS_268615776
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(param));
        //4.调用客户端方法,发送请求,获取响应
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            //5.从响应对象中,获取结果
            return response.getHttpResponse().isSuccess();
        } catch (ClientException e) {
            e.printStackTrace();
            throw new YyghException(20001,"发送验证码失败");
        }
    }
}
