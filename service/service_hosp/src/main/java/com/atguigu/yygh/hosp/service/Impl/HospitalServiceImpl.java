package com.atguigu.yygh.hosp.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.Result;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.Hospital;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-09 16:02
 * @Description
 */
@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    @Override
    public void save(Map<String, Object> paraMap) {
        //1.参数转型 paraMap => Hospital
        String jsonString = JSONObject.toJSONString(paraMap);
        Hospital hospital = JSONObject.parseObject(jsonString, Hospital.class);
        //2.根据hoscode查询医院信息
        Hospital targetHospital = hospitalRepository.getByHoscode(hospital.getHoscode());

        if (targetHospital != null) {
            //3.医院存在进行更新
            hospital.setId(targetHospital.getId());
            hospital.setCreateTime(targetHospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setStatus(targetHospital.getStatus());
            hospital.setIsDeleted(targetHospital.getIsDeleted());
            hospitalRepository.save(hospital);
        }else {
            //4.医院不存在进行新增
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setStatus(0);
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }

    }

    @Override
    public Hospital getHospByHoscode(String hoscode) {
        Hospital targetHospital = hospitalRepository.getByHoscode(hoscode);
        if (targetHospital == null) {
            throw new YyghException(20001,"医院信息有误");
        }
        return targetHospital;
    }
}
