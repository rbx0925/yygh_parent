package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-27 23:54
 * @Description
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        String openid = loginVo.getOpenid();
        //校验参数-(验空)
        if(StringUtils.isEmpty(phone) ||
                StringUtils.isEmpty(code)) {
            throw new YyghException(20001,"数据为空");
        }
        //校验校验验证码
        //根据手机号查询Redis获取验证码
        String redisCode = redisTemplate.opsForValue().get(phone);
        //校验验证码
        if (!code.equals(redisCode)){
            throw new YyghException(20001,"验证码有误");
        }
        UserInfo userInfo = new UserInfo();
        if (StringUtils.isEmpty(openid)){
            //手机号已被使用
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", phone);
            //获取会员
            userInfo = baseMapper.selectOne(queryWrapper);
            if(null == userInfo) {
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                baseMapper.insert(userInfo);
            }
        }else {
            //手机号已被使用
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("openid", openid);
            //获取会员
            userInfo = baseMapper.selectOne(wrapper);
            if(null == userInfo) {
                throw new YyghException(20001,"用户信息有误");
            }
            //更新用户信息
            userInfo.setPhone(phone);
            baseMapper.updateById(userInfo);
        }
        //校验是否被禁用
        if(userInfo.getStatus() == 0) {
            throw new YyghException(20001,"用户已经禁用");
        }
        //返回页面显示名称
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        //进行登录操作
        String token = JwtHelper.createToken(userInfo.getId(), name);
        map.put("name", name);
        map.put("token", token);
        return map;
    }

    //用户认证接口
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //1.根据userId查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //2.把userAuthVo存入userInfo
        BeanUtils.copyProperties(userAuthVo, userInfo);
        //3.设置认证状态
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //4.更新用户信息
        baseMapper.updateById(userInfo);
    }

    //根据用户ID获取信息接口
    @Override
    public UserInfo getUserInfo(Long userId) {
        UserInfo userInfo = this.packUserInfo(baseMapper.selectById(userId));
        return userInfo;
    }

    private UserInfo packUserInfo(UserInfo userInfo) {
        String statusNameByStatus = AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus());
        userInfo.getParam().put("authStatusString",statusNameByStatus);
        return userInfo;
    }
}
