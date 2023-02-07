package com.atguigu.yygh.hosp.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public void save(Map<String, Object> paraMap) {
        //1.借助工具转换对象
        String jsonString = JSONObject.toJSONString(paraMap);
        Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);
        //2.根据 hosecode hosScheduleId 参数查询排班
        Schedule targetSchedule = scheduleRepository.getByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        if (targetSchedule != null) {
            //3.有->更新
            schedule.setId(targetSchedule.getId());
            schedule.setCreateTime(targetSchedule.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(targetSchedule.getIsDeleted());
            scheduleRepository.save(schedule);
        } else {
            //4.无->新增
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }
    }

    //根据医院编号 和 科室编号 ，分页查询排班统计数据
    @Override
    public Map<String, Object> getScheduleRule(long page, long limit, String hoscode, String depcode) {
        //1.创建返回结果对象
        Map<String, Object> result = new HashMap<>();
        //2.根据筛选条件,分页条件进行聚合查询(List)
        //2.1创建查询条件对象
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //2.2拼写聚合查询条件对象
        Aggregation agg = Aggregation.newAggregation(
                //2.2.1设置筛选条件
                Aggregation.match(criteria),
                //2.2.2设置聚合字段,明确查询字段
                Aggregation.group("workDate").first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                Aggregation.sort(Sort.Direction.ASC, "workDate"),
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );
        //2.3进行聚合查询
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();
        //3.根据筛选条件进行聚合查询(total)
        Aggregation aggTotal = Aggregation.newAggregation(
                //2.2.1设置筛选条件
                Aggregation.match(criteria),
                //2.2.2设置聚合字段,明确查询字段
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> aggregateTotal = mongoTemplate.aggregate(aggTotal, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> totalList = aggregateTotal.getMappedResults();
        int total = totalList.size();
        //4.根据排班日期转换成周记
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = this.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //5.封装数据返回
        result.put("bookingScheduleRuleList", bookingScheduleRuleVoList);
        result.put("total", total);

        //获取医院名称
        String hosName = hospitalService.getHospName(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hosName);
        result.put("baseMap", baseMap);
        return result;
    }

    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, Date workDate) {
        //1.根据条件查询数据
        List<Schedule> list = scheduleRepository.getByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, workDate);
        //2.遍历集合翻译字段
        list.forEach(item -> {
            this.packSchedule(item);
        });
        return list;
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //1.根据hoscode查询医院信息,取出预约规则
        Hospital hospital = hospitalService.getHospByHoscode(hoscode);
        if (hospital == null) {
            throw new YyghException(20001, "医院信息有误");
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //2.根据分页参数+预约规则,分页查询可以预约的日期分页对象(Ipage<Date>)
        IPage<Date> iPage = this.getDateListPage(page, limit, bookingRule);
        List<Date> dateList = iPage.getRecords();
        //3.参考后台排班管理,进行聚合查询,获取可以预约的号源信息(List<BookingScheduleRuleVo>)
        //3.1创建查询条件对象
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        //3.2创建聚合查询条件对象
        Aggregation agg = Aggregation.newAggregation(
            //2.2.1设置筛选条件
            Aggregation.match(criteria),
            //2.2.2设置聚合字段,明确查询字段
            Aggregation.group("workDate").first("workDate").as("workDate")
                    .count().as("docCount")
                    .sum("reservedNumber").as("reservedNumber")
                    .sum("availableNumber").as("availableNumber")
        );
        //3.3进行聚合查询(List)
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = aggregate.getMappedResults();
        //3.4把统计结果进行转型List->Map(workDate,List<BookingScheduleRuleVo>)
        Map<Date,BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(scheduleVoList)){
            scheduleVoMap = scheduleVoList.stream().collect(Collectors.toMap(
                    BookingScheduleRuleVo::getWorkDate,
                    BookingScheduleRuleVo -> BookingScheduleRuleVo
            ));
        }
        //4.实现步骤2(dateList)和步骤3的数据整合(scheduleVoMap)
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        //4.1遍历dateList,取出每天date
        for (int i = 0,let=dateList.size(); i < let; i++) {
            Date date = dateList.get(i);
            //4.2拿date从scheduleVoMap取出对应的统计结果BookingScheduleRuleVo
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            //4.3如果bookingScheduleRuleVo需要完成初始化操作
            if (bookingScheduleRuleVo==null){
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数  -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            //4.4统一设置排班日期
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //4.5根据排班日期算出周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
            //4.6根据相关时间判断数据状态
            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
            if (page==iPage.getPages()&&i==let-1){
                bookingScheduleRuleVo.setStatus(1);
            }else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //第一页,第一条,判断是否已过停止挂号时间,已过状态为-1,当天已停止挂号
            if (page==1&&i==0){
                DateTime stopDateTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopDateTime.isBeforeNow()){
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            //4.7bookingScheduleRuleVo存入List
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        //5.封装数据
        Map<String, Object> result = new HashMap<>();
        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    //根据排班id获取排班详情
    @Override
    public Schedule findScheduleById(String id) {
        Schedule schedule = scheduleRepository.findById(id).get();
        return this.packSchedule(schedule);
    }

    //根据排班id获取预约下单数据
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        //1.scheduleId查询排班记录
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        if (schedule==null) {
            throw new YyghException(20001,"排班信息有误");
        }
        //2.根据hoscode查询医院信息 -> 获取预约规则
        Hospital hospital = hospitalService.getHospByHoscode(schedule.getHoscode());
        if (hospital==null) {
            throw new YyghException(20001,"医院信息有误");
        }
        //拿到预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        //3.基础数据封装
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepartment(schedule.getHoscode(), schedule.getDepcode()).getDepname());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());
        //4.根据预约规则计算时间相关数据进行封装
        //计算退号截止时间点
        DateTime quitDay = new DateTime(schedule.getWorkDate()).plusDays(bookingRule.getQuitDay());
        DateTime quitDateTime = this.getDateTime(quitDay.toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitDateTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(stopTime.toDate());

        return scheduleOrderVo;
    }

    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        //主键一致就是更新
        scheduleRepository.save(schedule);
    }

    @Override
    public Schedule getScheduleByIds(String hoscode, String scheduleId) {
        Schedule schedule = scheduleRepository.getByHoscodeAndHosScheduleId(hoscode, scheduleId);
        return schedule;
    }

    //根据分页参数+预约规则,分页查询可以预约的日期分页对象(Ipage<Date>)
    private IPage<Date> getDateListPage(Integer page, Integer limit, BookingRule bookingRule) {
        //1.获取放号时间(当前系统日期+放号时间)
        DateTime ReleaseDateTime = this.getDateTime(new Date(), bookingRule.getReleaseTime()); //8:30
        //2.取出预约周期,判断是否已过放号时间,如果已过周期+1
        Integer cycle = bookingRule.getCycle(); //10
        if (ReleaseDateTime.isBeforeNow()) cycle += 1;
        //3.根据周期推算可以挂号的日期集合(List<Date>)
        List<Date> dateList = new ArrayList<>(); //日期集合
        for (int i = 0; i < cycle; i++) {
            DateTime plusDays = new DateTime().plusDays(i);
            String dateString = plusDays.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        //4.准备分页的参数
        int start = (page - 1) * limit;
        int end = (page - 1) * limit + limit;
        if (end > dateList.size()) end = dateList.size();
        //5.取出分页后的日期集合(List<Date>)
        List<Date> datePageList = new ArrayList<>();
        for (int i = start; i < end; i++) {   // start = 0  , end = 7
            datePageList.add(dateList.get(i));
        }
        //6.数据封装到IPage返回
        IPage<Date> iPage = new Page<>(page, limit, dateList.size());
        iPage.setRecords(datePageList);
        return iPage;
    }

    //拼接日期+时间
    private DateTime getDateTime(Date date, String releaseTime) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + releaseTime;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    //翻译字段
    private Schedule packSchedule(Schedule schedule) {
        //设置医院名称
        schedule.getParam().put("hosname", hospitalService.getHospName(schedule.getHoscode()));
        //设置科室名称
        schedule.getParam().put("depname",
                departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek", this.getDayOfWeek(new DateTime(schedule.getWorkDate())));

        return schedule;
    }

    /**
     * 根据日期获取周几数据
     *
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }
}
