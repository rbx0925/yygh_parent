package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-11 19:28
 * @Description
 */
@RestController
@Api(description = "医院接口")
//@CrossOrigin
@RequestMapping("/admin/hosp/hospital")
public class HospitalController {
    //注入service
    @Autowired
    private HospitalService hospitalService;

    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public R index(@PathVariable Integer page, @PathVariable Integer limit, HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> pageModel = hospitalService.selectPage(page,limit,hospitalQueryVo);
        return R.ok().data("pageModel",pageModel);
    }

    @ApiOperation(value = "更新上线状态")
    @GetMapping("updateStatus/{id}/{status}")
    public R updateStatus(
            @PathVariable("id") String id,
            @PathVariable("status") Integer status){
        hospitalService.updateStatus(id,status);
        return R.ok();
    }

    @ApiOperation(value = "获取医院详情")
    @GetMapping("show/{id}")
    public R show(@PathVariable String id) {
        Map<String,Object> map = hospitalService.show(id);
        return R.ok().data(map);
    }
}
