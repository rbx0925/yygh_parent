package com.atguigu.yygh.hosp.api;

import com.atguigu.yygh.common.Result;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.hosp.utils.MD5;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-09 16:03
 * @Description
 */
@RestController
@Api(tags = "医院管理API接口")
@RequestMapping("/api/hosp")
public class ApiController {
    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "上传医院")
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request) {
        //1.request中取出参数,转型
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paraMap = HttpRequestHelper.switchMap(parameterMap);
        //2.签名校验
        //2.1取出医院相关参数
        String hoscode = (String) paraMap.get("hoscode");
        String sign = (String) paraMap.get("sign");
        //2.2调用接口获取签名 , MD5加密
        String hospSignKey = hospitalSetService.getSignKey(hoscode);
        String hospSignMD5 = MD5.encrypt(hospSignKey);
        //2.3签名校验
        System.out.println("sign = " + sign);
        System.out.println("hospSignMD5 = " + hospSignMD5);
        if (!sign.equals(hospSignMD5)){
            throw new YyghException(20001,"签名校验不通过");
        }
        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String)paraMap.get("logoData");
        logoData = logoData.replaceAll(" ","+");
        paraMap.put("logoData",logoData);
        //3.调用医院接口方法 , 保存医院信息 , 返回结果
        hospitalService.save(paraMap);
        return Result.ok();
    }

    @ApiOperation(value = "获取医院信息")
    @PostMapping("hospital/show")
    public Result hospital(HttpServletRequest request) {
        //1.request中取出参数,转型
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paraMap = HttpRequestHelper.switchMap(parameterMap);
        //2.取出相关参数
        String hoscode = (String) paraMap.get("hoscode");
        String sign = (String) paraMap.get("sign");
        //3.签名校验
        //4调接口查询医院信息
        Hospital hospital = hospitalService.getHospByHoscode(hoscode);
        //5返回结果
        return Result.ok(hospital);
    }



    @ApiOperation(value = "上传科室")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        //request中取出参数,转型
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paraMap = HttpRequestHelper.switchMap(parameterMap);
        //调用接口方法保存科室信息
        departmentService.save(paraMap);
        return Result.ok();
    }

    @ApiOperation(value = "获取分页列表")
    @PostMapping("department/list")
    public Result department(HttpServletRequest request) {
        //request中取出参数,转型
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paraMap = HttpRequestHelper.switchMap(parameterMap);
        //取出参数 , 验空 , 封装
        String hoscode = (String) paraMap.get("hoscode");
        String sign = (String) paraMap.get("sign");
        int page = StringUtils.isEmpty(paraMap.get("page")) ? 1 : Integer.parseInt((String) paraMap.get("page"));
        int limit = StringUtils.isEmpty(paraMap.get("limit")) ? 10 : Integer.parseInt((String) paraMap.get("limit"));

        DepartmentQueryVo departmentQueryVo = new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);

        //根据参数查询科室数据
        Page<Department> pagemodel = departmentService.selectPage(page, limit, departmentQueryVo);
        return Result.ok(pagemodel);
    }

    @ApiOperation(value = "删除科室")
    @PostMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        //request中取出参数,转型
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paraMap = HttpRequestHelper.switchMap(parameterMap);
        //从paraMap中取出参数
        String hoscode = (String) paraMap.get("hoscode");
        String depcode = (String) paraMap.get("depcode");
        String sign = (String) paraMap.get("sign");
        //调用接口删除
        departmentService.remove(hoscode, depcode);
        //返回结果
        return Result.ok();
    }

    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        //request中取出参数,转型
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paraMap = HttpRequestHelper.switchMap(parameterMap);
        //保存排班信息
        scheduleService.save(paraMap);
        return Result.ok();
    }
}
