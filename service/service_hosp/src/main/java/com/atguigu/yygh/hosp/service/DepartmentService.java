package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-10 16:09
 * @Description
 */
public interface DepartmentService {
    //保存科室信息
    void save(Map<String, Object> paraMap);

    Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo);

    void remove(String hoscode, String depcode);
}
