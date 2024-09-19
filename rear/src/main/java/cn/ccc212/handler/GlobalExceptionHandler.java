package cn.ccc212.handler;

import cn.ccc212.exception.BizException;
import cn.ccc212.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public Result bizExceptionHandler(BizException ex) {
        log.info("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
}