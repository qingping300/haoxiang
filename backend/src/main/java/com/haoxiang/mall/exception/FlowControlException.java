package com.haoxiang.mall.exception;
import lombok.Getter;
/** 区分流量拒绝与依赖暂时不可用。 */
@Getter
public class FlowControlException extends RuntimeException {
    private final int status;
    private final int retryAfter;
    public FlowControlException(int status, int retryAfter, String message) {
        super(message); this.status=status; this.retryAfter=Math.max(1,retryAfter);
    }
}
