package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author rbx
 * @title
 * @Create 2023-01-09 16:00
 * @Description
 */
@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {

    //根据hoscode查询医院信息
    Hospital getByHoscode(String hoscode);

    //根据医院名称获取医院列表
    List<Hospital> getByHosnameLike(String hosname);
}
