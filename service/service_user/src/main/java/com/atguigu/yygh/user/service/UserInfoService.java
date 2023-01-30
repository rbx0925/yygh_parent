package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-27 23:53
 * @Description
 */
public interface UserInfoService extends IService<UserInfo> {
    //会员登录
    Map<String, Object> login(LoginVo loginVo);

    //用户认证接口
    void userAuth(Long userId, UserAuthVo userAuthVo);

    //根据用户ID获取信息接口
    UserInfo getUserInfo(Long userId);
}
