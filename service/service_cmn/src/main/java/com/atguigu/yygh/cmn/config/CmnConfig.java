package com.atguigu.yygh.cmn.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author rbx
 * @title
 * @Create 2023-01-06 11:21
 * @Description
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.atguigu.yygh.cmn.mapper")
public class CmnConfig {

}
