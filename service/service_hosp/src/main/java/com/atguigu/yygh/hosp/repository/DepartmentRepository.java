package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author rbx
 * @title
 * @Create 2023-01-10 16:08
 * @Description
 */
@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {
    //根据hoscode depcode查询科室信息
    Department getByHoscodeAndDepcode(String hoscode, String depcode);

    //根据hoscode查询所有科室信息
    List<Department> getByHoscode(String hoscode);
}
