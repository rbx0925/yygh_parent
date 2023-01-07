package com.atguigu.yygh.cmn.test;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.Map;

/**
 * @author rbx
 * @title
 * @Create 2023-01-06 16:08
 * @Description
 */
public class ExcelListener extends AnalysisEventListener<Stu> {
    @Override
    public void invoke(Stu stu, AnalysisContext analysisContext) {
        System.out.println(stu);
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("表头信息:"+headMap);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
