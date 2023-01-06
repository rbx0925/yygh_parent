package com.atguigu.yygh.common.handler;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author rbx
 * @title
 * @Create 2022-12-30 9:22
 * @Description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YyghException extends RuntimeException{
    @ApiModelProperty(value = "状态码")
    private Integer code;

    private String msg;
}
