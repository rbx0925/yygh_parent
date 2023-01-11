package com.atguigu.yygh.hosp.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-10 20:46
 * @Description
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Override
    public void save(Map<String, Object> paraMap) {
        //1.借助工具转换对象
        String jsonString = JSONObject.toJSONString(paraMap);
        Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);
        //2.根据 hosecode hosScheduleId 参数查询排班
        Schedule targetSchedule = scheduleRepository.getByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());
        if (targetSchedule!=null){
            //3.有->更新
            schedule.setId(targetSchedule.getId());
            schedule.setCreateTime(targetSchedule.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(targetSchedule.getIsDeleted());
            scheduleRepository.save(schedule);
        }else {
            //4.无->新增
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }
    }
}
