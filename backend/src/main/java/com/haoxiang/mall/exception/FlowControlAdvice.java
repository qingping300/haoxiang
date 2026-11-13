package com.haoxiang.mall.exception;
import com.haoxiang.mall.util.R;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/** 保留统一响应体，同时提供可识别的 HTTP 状态和重试提示。 */
@Order(-100)
@RestControllerAdvice
public class FlowControlAdvice {
    @ExceptionHandler(FlowControlException.class)
    public ResponseEntity<R> handle(FlowControlException error) {
        R body=R.error(error.getMessage()).add("retryAfter",error.getRetryAfter());
        body.setCode(error.getStatus());
        return ResponseEntity.status(error.getStatus()).header("Retry-After",String.valueOf(error.getRetryAfter())).body(body);
    }
}
