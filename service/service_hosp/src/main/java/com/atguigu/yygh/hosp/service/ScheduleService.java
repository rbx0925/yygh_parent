package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-10 20:46
 * @Description
 */
public interface ScheduleService {
    void save(Map<String, Object> paraMap);

    //根据医院编号 和 科室编号 ，分页查询排班统计数据
    Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode);

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    List<Schedule> getScheduleDetail(String hoscode, String depcode, Date workDate);

    //获取可预约排班数据
    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    //根据排班id获取排班详情
    Schedule findScheduleById(String id);

    //根据排班id获取预约下单数据
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    /**
     * 修改排班
     */
    void update(Schedule schedule);

    Schedule getScheduleByIds(String hoscode, String scheduleId);
}
