package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.HospitalSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author rbx
 * @title
 * @Create 2022-12-28 20:44
 * @Description
 */
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;

    @GetMapping("/findAll")
    public List<HospitalSet> findAll(){
        List<HospitalSet> list = hospitalSetService.list();
        return list;
    }

    @DeleteMapping("{id}")
    public boolean removeById(@PathVariable String id){
        return hospitalSetService.removeById(id);
    }
}
