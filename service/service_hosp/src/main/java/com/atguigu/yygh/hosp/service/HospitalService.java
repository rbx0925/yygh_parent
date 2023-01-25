package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

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

    Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id, Integer status);

    Map<String, Object> show(String id);

    //获取医院名称
    String getHospName(String hoscode);
}
