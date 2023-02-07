package com.atguigu.yygh.msm.service;

import com.atguigu.yygh.vo.msm.MsmVo;

import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-28 16:31
 * @Description
 */
public interface MsmService {
    //发送验码
    boolean send(String phone, Map<String, String> param);

    //发送短信接口
    boolean send(MsmVo msmVo);
}
