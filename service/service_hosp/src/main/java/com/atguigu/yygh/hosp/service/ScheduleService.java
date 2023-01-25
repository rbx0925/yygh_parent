package com.atguigu.yygh.hosp.service;

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
}
