package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;

import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-09 16:01
 * @Description
 */
public interface HospitalService {
    //保存医院信息
    void save(Map<String, Object> paraMap);

    Hospital getHospByHoscode(String hoscode);
}
