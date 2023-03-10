package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rbx
 * @title
 * @Create 2023-01-06 11:17
 * @Description
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {
    @Autowired
    private DictListener dictListener;

    @Override
    @Cacheable(value = "dict", key = "'selectIndexList'+#id")
    public List<Dict> findChildData(Long id) {
        //1.拼接查询条件
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        //2.调方法查询数据
        List<Dict> dicts = baseMapper.selectList(wrapper);
        //3.遍历集合,查询是否有子数据
        for (Dict dict : dicts) {
            boolean hasChildren = this.isChild(dict.getId());
            dict.setHasChildren(hasChildren);
        }
        return dicts;
    }

    @Override
    public void exportData(HttpServletResponse response) {
        try {
            //1设置response基本参数
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");
            //2查询Dict所有数据
            List<Dict> dictList = baseMapper.selectList(null);
            List<DictEeVo> dictVoList = new ArrayList<>(dictList.size());
            for(Dict dict : dictList) {
                DictEeVo dictVo = new DictEeVo();
                BeanUtils.copyProperties(dict,dictVo);
                dictVoList.add(dictVo);
            }
            //3使用工具导出
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
            throw new YyghException(20001,"数据导出失败");
        }
    }

    @Override
    public void importDictData(MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            EasyExcel.read(inputStream, DictEeVo.class,dictListener).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
            throw new YyghException(20001,"导入数据失败");
        }
    }

    @Override
    public String getNameByParentDictCodeAndValue(String parentDictCode, String value) {
        //1.判断是国标数据还是自定义数据
        if (StringUtils.isEmpty(parentDictCode)){
            //2.实现国标数据翻译
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("value", value));
            if (dict != null){
                return dict.getName();
            }
        }else {
            //3.实现自定义数据翻译
            //3.1根据数据字典编码查询上级数据
            Dict parentDict = this.getDictByDictCode(parentDictCode);
            if (parentDict == null){
                return "";
            }
            //3.2根据上级id+value查询字典数据
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("parent_id",parentDict.getId()).eq("value", value));
            if (dict != null){
                return dict.getName();
            }
        }
        return "";
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict parentDict = this.getDictByDictCode(dictCode);
        if (parentDict==null){
            throw new YyghException(20001,"获取下级节点失败");
        }
        List<Dict> list = this.findChildData(parentDict.getId());
        return list;
    }

    //根据字典编码查询数据
    private Dict getDictByDictCode(String parentDictCode) {
        Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", parentDictCode));
        return dict;
    }

    private boolean isChild(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(wrapper);
        return count>0;
    }


}
