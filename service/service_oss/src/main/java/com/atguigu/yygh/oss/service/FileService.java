package com.atguigu.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author rbx
 * @title
 * @Create 2023-01-30 14:20
 * @Description
 */
public interface FileService {

    /**
     * 文件上传至阿里云
     */
    String upload(MultipartFile file);
}
