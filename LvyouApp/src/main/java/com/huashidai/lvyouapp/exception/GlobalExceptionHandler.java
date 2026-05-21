package com.huashidai.lvyouapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.HashMap;
import java.util.Map;

import com.huashidai.lvyouapp.common.ApiResponse;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
        if (ErrorCode.POINTS_INSUFFICIENT.getCode().equals(e.getCode())) {
            log.debug("积分不足: {}", e.getMessage());
        } else {
            log.warn("业务异常: {}", e.getMessage());
        }
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(e.getCode(), e.getMessage(), e.getData()));
    }
    
    /**
     * 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException e) {
        log.error("认证异常: {}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(ErrorCode.AUTHENTICATION_ERROR.getCode(), "认证失败，请重新登录"));
    }
    
    /**
     * 处理权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限异常: {}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(ErrorCode.ACCESS_DENIED.getCode(), "权限不足，无法访问该资源"));
    }
    
    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("文件上传大小超限: {}", e.getMessage(), e);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(ErrorCode.FILE_SIZE_EXCEEDED.getCode(), "文件大小超出限制，请选择小于10MB的文件"));
    }
    
    /**
     * 处理文件上传异常
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Object>> handleMultipartException(MultipartException e) {
        log.error("文件上传异常: {}", e.getMessage(), e);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "文件上传失败，请检查文件格式和大小"));
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        log.error("参数验证异常: {}", e.getMessage(), e);

        Map<String, String> fieldErrors = new HashMap<>();
        StringBuilder errorMessage = new StringBuilder();

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
            if (errorMessage.length() > 0) {
                errorMessage.append("；");
            }
            errorMessage.append(error.getDefaultMessage());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("fieldErrors", fieldErrors);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(ErrorCode.VALIDATION_ERROR.getCode(), errorMessage.toString(), data));
    }
    
    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Object>> handleBindException(BindException e) {
        log.error("绑定异常: {}", e.getMessage(), e);

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> data = new HashMap<>();
        data.put("fieldErrors", fieldErrors);

        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(ErrorCode.BIND_ERROR.getCode(), "参数绑定失败", data));
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        log.error("系统异常: {}", e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR.getCode(), "系统内部错误，请稍后重试"));
    }
}