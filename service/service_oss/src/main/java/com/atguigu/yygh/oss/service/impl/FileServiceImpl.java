package com.atguigu.yygh.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.yygh.common.handler.YyghException;
import com.atguigu.yygh.oss.service.FileService;
import com.atguigu.yygh.oss.utils.ConstantPropertiesUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author rbx
 * @title
 * @Create 2023-01-30 14:20
 * @Description
 */
@Service
public class FileServiceImpl implements FileService {
    @Override
    public String upload(MultipartFile file) {
        //1.获取配置参数
        String endPoint = ConstantPropertiesUtil.END_POINT;
        String accessKeyId = ConstantPropertiesUtil.ACCESS_KEY_ID;
        String accessKeySecret = ConstantPropertiesUtil.ACCESS_KEY_SECRET;
        String bucketName = ConstantPropertiesUtil.BUCKET_NAME;
        //2.创建客户端对象
        OSS ossClient = new OSSClientBuilder().build(endPoint, accessKeyId, accessKeySecret);
        try {
            //3.准备相关参数
            //3.1输入流
            InputStream inputStream = file.getInputStream();
            //3.2文件名
            String fileName = file.getOriginalFilename();
            //3.3为避免文件重名,生成随机唯一值，使用uuid，添加到文件名称里面
            String uuid = UUID.randomUUID().toString().replaceAll("-","");
            fileName = uuid+fileName;
            //3.4拼接目录path=2023/01/30/fileName
            String path = new DateTime().toString("yyyy/MM/dd");
            fileName = path+"/"+fileName;
            //4.使用客户端方法,发送请求,获取响应
            ossClient.putObject(bucketName, fileName, inputStream);
            //5.获取URL返回
            //https://rbx.oss-cn-beijing.aliyuncs.com/ca4d180529ce47b2a1589d9f9d4d0b0f.jpg
            String url = "https://"+bucketName+"."+endPoint+"/"+fileName;
            return url;
        } catch (IOException e) {
            throw new YyghException(20001,"上传文件失败");
        } finally {
            //6.关闭客户端
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
