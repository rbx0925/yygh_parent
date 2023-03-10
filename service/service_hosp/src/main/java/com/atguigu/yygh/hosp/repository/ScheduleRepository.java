package com.atguigu.yygh.hosp.repository;

import com.atguigu.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author rbx
 * @title
 * @Create 2023-01-10 20:45
 * @Description
 */
@Repository
public interface ScheduleRepository extends MongoRepository<Schedule,String> {
    Schedule getByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    List<Schedule> getByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date workDate);
}
