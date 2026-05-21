package com.huashidai.lvyouapp.exception;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {
    
    private String code;
    private Object data;
    
    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.BUSINESS_ERROR.getCode();
    }
    
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
    
    public BusinessException(String code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }
    
    public String getCode() {
        return code;
    }
    
    public Object getData() {
        return data;
    }
}
