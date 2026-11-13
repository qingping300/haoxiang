package com.haoxiang.mall.exception;

import com.haoxiang.mall.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {


    //todo
    /**
     * 处理404异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public Object handle404Exception(Exception e) {
        log.error(e.getMessage(), e);
        return R.error("您请求路径不存在，请检查url！");
    }

    /**
     * 处理自定义异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler({BusinessException.class})
    public Object handleBusinessException(BusinessException e) {
        log.error(e.getMessage(), e);
        return R.error(e.getMessage());
    }

    /**
     * 处理文件上传过大bug
     *
     * @param e
     * @return
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public Object handleBusinessException(MaxUploadSizeExceededException e) {
        log.error(e.getMessage(), e);
        return R.error(e.getMessage());
    }

    /**
     * 处理全局异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler({Exception.class})
    public Object handleException(Exception e) {
        log.error(e.getMessage(), e);
        return R.error("服务器内部错误！");
    }
}
