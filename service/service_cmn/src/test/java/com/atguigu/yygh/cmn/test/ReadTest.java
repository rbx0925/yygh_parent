package com.atguigu.yygh.cmn.test;

import com.alibaba.excel.EasyExcel;

/**
 * @author rbx
 * @title
 * @Create 2023-01-06 16:12
 * @Description
 */
public class ReadTest {
    public static void main(String[] args) {
        String fileName = "D:\\a.xlsx";
        EasyExcel.read(fileName, Stu.class,new ExcelListener()).sheet().doRead();
    }
}
