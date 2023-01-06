package com.atguigu.yygh.common.handler;

import com.atguigu.yygh.common.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author rbx
 * @title
 * @Create 2022-12-29 19:57
 * @Description
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R error(Exception e){
        e.printStackTrace();
        return R.error();
    }

    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public R error(ArithmeticException e){
        e.printStackTrace();
        return R.error().message("特殊异常处理");
    }

    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public R error(YyghException e){
        e.printStackTrace();
        return R.error().code(e.getCode()).message(e.getMsg());
    }
}
