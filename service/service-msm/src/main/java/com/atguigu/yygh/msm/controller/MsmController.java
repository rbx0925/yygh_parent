package com.atguigu.yygh.msm.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.utils.RandomUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author rbx
 * @title
 * @Create 2023-01-28 16:31
 * @Description
 */
@Api(tags = "短信发送")
@RestController
@RequestMapping("/api/msm")
public class MsmController {
    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @ApiOperation(value = "发送短信验证码")
    @GetMapping(value = "/send/{phone}")
    public R code(@PathVariable String phone) {
        //1.从Redis根据phone获取验证码
        String code = redisTemplate.opsForValue().get(phone);
        //2.验证码存在 , 直接返回成功
        if(!StringUtils.isEmpty(code)) return R.ok();
        //3.验证码不存在 , 生成验证码
        code = RandomUtil.getFourBitRandom();
        Map<String,String> param = new HashMap<>();
        param.put("code", code);
        //4.调用方法发送验证码
        boolean isSend = msmService.send(phone, param);
        //5.发送成功 ,  在Redis里保存验证码(设置3分钟时效)
        if(isSend) {
            redisTemplate.opsForValue().set(phone, code,3, TimeUnit.MINUTES);
            return R.ok();
        } else {
            return R.error().message("发送短信失败");
        }
    }

}
