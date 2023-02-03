package com.atguigu.yygh.hosp.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.Result;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author rbx
 * @title
 * @Create 2023-01-10 16:09
 * @Description
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void save(Map<String, Object> paraMap) {
        //1.参数转型 paraMap => Department
        String jsonString = JSONObject.toJSONString(paraMap);
        Department department = JSONObject.parseObject(jsonString, Department.class);
        //2.根据hoscode depcode查询科室信息
        Department targetDepartment = departmentRepository.getByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());
        if (targetDepartment!=null){
            //3.有->更新
            department.setId(targetDepartment.getId());
            department.setCreateTime(targetDepartment.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(targetDepartment.getIsDeleted());
            departmentRepository.save(department);
        }else {
            //4.无->新增
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }

    }

    @Override
    public Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo) {
        //1.创建分页对象
        //1.1创建排序对象
        Sort sort = Sort.by(Sort.Direction.DESC,"createTime");
        //1.2创建分页对象
        Pageable pageable = PageRequest.of(page-1,limit,sort);
        //2.创建查询条件模板
        //2.1把查询条件存入实体对象
        Department department = new Department();
        BeanUtils.copyProperties(departmentQueryVo,department);
        //2.2创建模板构造器
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        //2.3创建查询条件模板
        Example<Department> example = Example.of(department,matcher);
        //3.带条件带分页查询
        Page<Department> pageModel = departmentRepository.findAll(example, pageable);
        //4.返回分页对象
        return pageModel;
    }

    @Override
    public void remove(String hoscode, String depcode) {
        //1.先查询
        Department department = departmentRepository.getByHoscodeAndDepcode(hoscode, depcode);
        //2.根据id删除
        if (department!=null){
            departmentRepository.deleteById(department.getId());
        }
    }

    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        //1.创建返回对象
        List<DepartmentVo> result = new ArrayList<>();
        //2.根据参数hoscode查询所有科室信息
        List<Department> departmentList = departmentRepository.getByHoscode(hoscode);
        //3.所有科室信息进行数据分组,根据大科室编号进行分组
        //List<Department> => Map<bigcode,List<Department>>
        Map<String,List<Department>> departmentMap = departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));
        //4.遍历Map封装大科室信息DepartmentVo
        for (Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
            DepartmentVo bigDepVo = new DepartmentVo();
            bigDepVo.setDepcode(entry.getKey());
            bigDepVo.setDepname(entry.getValue().get(0).getDepname());
            //5.封装小科室信息List<DepartmentVo>
            List<DepartmentVo> depVoList = new ArrayList<>();
            List<Department> depList = entry.getValue();
            for (Department department : depList) {
                DepartmentVo departmentVo = new DepartmentVo();
                BeanUtils.copyProperties(department, departmentVo);
                depVoList.add(departmentVo);
            }
            //6.封装好的小科室集合存入大科室对象
            bigDepVo.setChildren(depVoList);
            //7.把大科室对象存入最终返回对象
            result.add(bigDepVo);
        }
        return result;
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getByHoscodeAndDepcode(hoscode,depcode);
        if (department==null){
            throw new YyghException(20001,"科室信息有误");
        }
        return department.getDepname();
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        Department department = departmentRepository.getByHoscodeAndDepcode(hoscode,depcode);
        if (department==null){
            throw new YyghException(20001,"科室信息有误");
        }
        return department;
    }
}
