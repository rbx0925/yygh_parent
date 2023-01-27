package com.atguigu.yygh.hosp.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.common.Result;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.utils.HttpRequestHelper;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private DictFeignClient dictFeignClient;

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

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        //1.创建分页对象
        //1.1创建排序对象
        //1.2创建分页对象
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, limit, sort);
        //2.创建查询条件模板
        //2.1封装查询条件
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);
        //2.2创建模板构造器
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        //2.3创建查询条件模板
        Example<Hospital> example = Example.of(hospital, matcher);
        //3.带条件带分页的查询
        Page<Hospital> pageModel = hospitalRepository.findAll(example, pageable);
        //4.翻译字段
        pageModel.getContent().forEach(item ->{
            this.packHospital(item);
        });
        return pageModel;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        if (status.intValue() ==0 || status.intValue()==1){
            //1.先查询
            Hospital hospital = hospitalRepository.findById(id).get();
            //2.后修改
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Map<String, Object> show(String id) {
        //1.查询数据翻译字段
        Hospital hospital = this.packHospital(hospitalRepository.findById(id).get());
        //2.取出预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        hospital.setBookingRule(null);
        //3.封装结果返回
        HashMap<String, Object> map = new HashMap<>();
        map.put("hospital",hospital);
        map.put("bookingRule", bookingRule);
        return map;
    }

    @Override
    public String getHospName(String hoscode) {
        Hospital targetHospital = hospitalRepository.getByHoscode(hoscode);
        if (targetHospital == null) {
            throw new YyghException(20001,"医院信息有误");
        }
        return targetHospital.getHosname();
    }

    @Override
    public List<Hospital> findByHosnameLike(String hosname) {
        List<Hospital> list = hospitalRepository.getByHosnameLike(hosname);
        return list;
    }

    @Override
    public Map<String, Object> getHospInfo(String hoscode) {
        //1.查询数据翻译字段
        Hospital hospital = this.packHospital(hospitalRepository.getByHoscode(hoscode));
        //2.取出预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        hospital.setBookingRule(null);
        //3.封装结果返回
        HashMap<String, Object> map = new HashMap<>();
        map.put("hospital",hospital);
        map.put("bookingRule", bookingRule);
        return map;
    }

    //翻译医院信息
    private Hospital packHospital(Hospital hospital) {
        String hosTypeString = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(), hospital.getHostype());
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());
        hospital.getParam().put("hostypeString", hosTypeString);
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString + hospital.getAddress());
        return hospital;
    }
}
