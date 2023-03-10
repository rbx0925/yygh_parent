package com.atguigu.yygh.cmn.test;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author rbx
 * @title
 * @Create 2023-01-06 15:36
 * @Description
 */
@Data
public class Stu {

    //设置表头名称
    @ExcelProperty(value = "学生编号",index = 0)
    private int sno;

    //设置表头名称
    @ExcelProperty(value = "学生姓名",index = 1)
    private String sname;

}
