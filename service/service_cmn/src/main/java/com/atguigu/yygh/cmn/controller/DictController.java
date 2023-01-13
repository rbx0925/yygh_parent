package com.atguigu.yygh.cmn.controller;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author rbx
 * @title
 * @Create 2023-01-06 11:19
 * @Description
 */
@Api(description = "数据字典接口")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin
public class DictController {
    @Autowired
    private DictService dictService;

    @ApiOperation(value = "根据数据ID查询子数据列表")
    @GetMapping("/findChildData/{id}")
    public R findChildData(@PathVariable Long id){
        List<Dict> list = dictService.findChildData(id);
        System.out.println(list);
        return R.ok().data("list",list);
    }

    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public R findByDictCode(
            @PathVariable String dictCode) {
        List<Dict> list = dictService.findByDictCode(dictCode);
        return R.ok().data("list",list);
    }

    @ApiOperation(value="导出")
    @GetMapping(value = "/exportData")
    public void exportData(HttpServletResponse response) {
        dictService.exportData(response);
    }

    @ApiOperation(value = "导入")
    @PostMapping("/importData")
    public R importData(MultipartFile file) {
        dictService.importDictData(file);
        return R.ok();
    }

    @ApiOperation(value = "获取数据字典名称(自定义)")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getName(
            @PathVariable("parentDictCode") String parentDictCode,
            @PathVariable("value") String value) {
        return dictService.getNameByParentDictCodeAndValue(parentDictCode, value);
    }

    @ApiOperation(value = "获取数据字典名称(国标)")
    @GetMapping(value = "/getName/{value}")
    public String getName(
            @PathVariable("value") String value) {
        return dictService.getNameByParentDictCodeAndValue("", value);
    }
}
