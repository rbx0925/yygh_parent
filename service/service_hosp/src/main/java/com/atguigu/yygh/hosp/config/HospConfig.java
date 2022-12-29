package com.atguigu.yygh.hosp.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author rbx
 * @title
 * @Create 2022-12-28 20:50
 * @Description
 */
@Configuration
@MapperScan("com.atguigu.yygh.hosp.mapper")
@EnableTransactionManagement
public class HospConfig {
}
