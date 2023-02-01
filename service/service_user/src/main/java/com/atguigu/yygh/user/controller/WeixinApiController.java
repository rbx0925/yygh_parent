package com.atguigu.yygh.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.ConstantPropertiesUtil;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-29 15:11
 * @Description
 */
@Controller
@RequestMapping("/api/ucenter/wx")
public class WeixinApiController {

    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取微信登录参数
     */
    @GetMapping("/getLoginParam")
    @ResponseBody
    public R genQrConnect(HttpSession session) throws UnsupportedEncodingException {
        //处理回调地址
        String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
        //封装参数返回
        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis()+"");//System.currentTimeMillis()+""
        return R.ok().data(map);
    }

    @GetMapping("callback")
    public String callback(String code, String state, HttpSession session) {
        //1.获取微信临时验证码code
        System.out.println("code = " + code);
        System.out.println("state = " + state);
        //2.根据code和相关参数调用微信接口 ,  获取 access_token  openid
        //2.1拼写url(请求地址+参数)
        //方式一:
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
        //方式二:
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String  accessTokenUrl = String.format(baseAccessTokenUrl.toString()
                ,ConstantPropertiesUtil.WX_OPEN_APP_ID
                ,ConstantPropertiesUtil.WX_OPEN_APP_SECRET
                ,code);
        try {
            //2.2通过工具发送请求获取响应
            String accessTokenStr = HttpClientUtils.get(accessTokenUrl);
            System.out.println("accessTokenStr = " + accessTokenStr);
            //2.3获取响应结果,转型,获取参数
            JSONObject accessTokenJson = JSONObject.parseObject(accessTokenStr);
            String accessToken = accessTokenJson.getString("access_token");
            String openid = accessTokenJson.getString("openid");
            //3.拿openid作为参数,查询userinfo信息
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("openid",openid);
            UserInfo userInfo = userInfoService.getOne(wrapper);
            //4.userinfo为空走注册步骤
            if (userInfo==null){
                //5.根据access_token  openid参数调用微信接口获取用户信息,进行注册
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token=%s" +
                        "&openid=%s";
                String userInfoUrl = String.format(baseUserInfoUrl, accessToken, openid);
                String resultInfo = HttpClientUtils.get(userInfoUrl);
                System.out.println("resultInfo:"+resultInfo);
                JSONObject resultUserInfoJson = JSONObject.parseObject(resultInfo);
                //解析用户信息
                //用户昵称
                String nickname = resultUserInfoJson.getString("nickname");
                //用户头像
                String headimgurl = resultUserInfoJson.getString("headimgurl");
                //进行注册
                userInfo = new UserInfo();
                userInfo.setOpenid(openid);
                userInfo.setNickName(nickname);
                userInfo.setStatus(1);
                userInfoService.save(userInfo);
            }
            //6.验证用户状态 , 如果为0 锁定 , 结束登录
            if (userInfo.getStatus()==0){
                throw new YyghException(20001,"用户已锁定");
            }
            HashMap<String, String> map = new HashMap<>();
            //7.验证用户是否需要绑定手机号
            //如果需要绑定手机号给openid赋值
            //如果不需要绑定手机号给openid="" 空串
            if (StringUtils.isEmpty(userInfo.getPhone())){
                map.put("openid",openid);
            }else {
                map.put("openid","");
            }
            //8.补全用户信息 , 走登录步骤
            String name = userInfo.getName();
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            map.put("name",name);
            //使用jwt生成token字符串
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token", token);
            //9.重定向到相关页面
            String s = "redirect:http://localhost:3000/weixin/callback?token=" + map.get("token") + "&openid=" + map.get("openid") + "&name=" + URLEncoder.encode(map.get("name"), "utf-8");
            System.out.println("s = " + s);
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
