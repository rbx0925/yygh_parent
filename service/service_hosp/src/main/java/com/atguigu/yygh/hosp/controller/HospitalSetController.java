package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * @author rbx
 * @title
 * @Create 2022-12-28 20:44
 * @Description
 */
@Api(description = "医院设置接口")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@CrossOrigin
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;

    @ApiOperation(value = "模拟登录")
    @PostMapping("login")
    public R login(){
        return R.ok().data("token","admin-token");
    }

    @ApiOperation(value = "模拟获取用户信息")
    @PostMapping("info")
    public R info(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("roles","admin");
        map.put("introduction","I am a super administrator");
        map.put("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        map.put("name","Super Admin");
        return R.ok().data("token","admin-token");
    }

    @ApiOperation(value = "医院设置列表")
    @GetMapping("/findAll")
    public R findAll(){
        try {
            System.out.println(10/0);
        } catch (Exception e) {
            throw new YyghException(20001,"自定义异常处理");
        }
        List<HospitalSet> list = hospitalSetService.list();
        System.out.println("list =" + list);
        return R.ok().data("list",list);
    }

    @ApiOperation(value = "医院设置删除")
    @DeleteMapping("{id}")
    public R removeById(@PathVariable String id){
        boolean b = hospitalSetService.removeById(id);
        if (b)
            return R.ok();
        return R.error();
    }

    @ApiOperation(value = "分页医院设置列表")
    @GetMapping("{page}/{limit}")
    public R pageList(@PathVariable Long page, @PathVariable Long limit){
        Page<HospitalSet> hospitalSetPage = new Page<>(page, limit);
        hospitalSetService.page(hospitalSetPage);
        List<HospitalSet> records = hospitalSetPage.getRecords();
        long total = hospitalSetPage.getTotal();
        return R.ok().data("list",records).data("rows",total);
    }

    @ApiOperation(value = "带条件带分页医院设置列表")
    @PostMapping("{page}/{limit}")
    public R pageQuery(@PathVariable Long page, @PathVariable Long limit,
                       @RequestBody HospitalQueryVo hospitalQueryVo
                       ){
        String hosname = "";
        String hoscode = "";
        if (hospitalQueryVo.getHosname()!=null){
            hosname = hospitalQueryVo.getHosname();
        }
        if (hospitalQueryVo.getHoscode()!=null){
            hoscode = hospitalQueryVo.getHoscode();
        }
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(hosname)){
            wrapper.like("hosname",hosname);
        }
        if (!StringUtils.isEmpty(hoscode)){
            wrapper.like("hoscode",hoscode);
        }
        Page<HospitalSet> hospitalSetPage = new Page<>(page, limit);
        hospitalSetService.page(hospitalSetPage,wrapper);
        List<HospitalSet> records = hospitalSetPage.getRecords();
        long total = hospitalSetPage.getTotal();
        return R.ok().data("list",records).data("rows",total);
    }

    @ApiOperation(value = "新增医院设置")
    @PostMapping("/save")
    public R save(@RequestBody HospitalSet hospitalSet){
        boolean save = hospitalSetService.save(hospitalSet);
        System.out.println(11);
        if (save)
            return R.ok();
        return R.error();
    }

    @ApiOperation(value = "根据ID查询医院设置")
    @GetMapping("/{id}")
    public R getById(@PathVariable String id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return R.ok().data("hospitalSet",hospitalSet);
    }

    @ApiOperation(value = "根据ID修改医院设置")
    @PostMapping("/update")
    public R update(@RequestBody HospitalSet hospitalSet){
        boolean b = hospitalSetService.updateById(hospitalSet);
        if (b)
            return R.ok();
        return R.error();
    }

    //批量删除医院设置
    @ApiOperation(value = "根据ID批量删除医院设置")
    @DeleteMapping("/batchRemove")
    public R batchRemoveHospitalSet(@RequestBody List<Long> idList) {
        boolean b = hospitalSetService.removeByIds(idList);
        if (b)
            return R.ok();
        return R.error();
    }

    // 医院设置锁定和解锁
    @ApiOperation(value = "医院设置锁定和解锁")
    @PutMapping("lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(@PathVariable Long id,
                             @PathVariable Integer status) {
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        boolean b = hospitalSetService.updateById(hospitalSet);
        if (b)
            return R.ok();
        return R.error();
    }
}
