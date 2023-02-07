package com.atguigu.yygh.task.scheduled;

import com.atguigu.yygh.common.service.MqConst;
import com.atguigu.yygh.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author rbx
 * @title
 * @Create 2023-02-06 14:23
 * @Description
 */
@Component
@EnableScheduling
public class ScheduledTask {
//    @Scheduled(cron = "0/5 * * * * ?")
//    public void Test(){
//        System.out.println("ScheduledTask!!!");
//    }
    @Autowired
    private RabbitService rabbitService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void task8(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK, MqConst.ROUTING_TASK_8, "");
    }
}
