package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
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

    ////根据医院编号，查询医院所有科室列表
    List<DepartmentVo> findDeptTree(String hoscode);
}
