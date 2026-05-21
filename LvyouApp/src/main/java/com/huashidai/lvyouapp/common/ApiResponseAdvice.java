package com.huashidai.lvyouapp.common;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 自动将所有 controller 的成功响应包装为 ApiResponse。
 * 失败响应由 GlobalExceptionHandler 统一处理。
 */
@RestControllerAdvice(basePackages = "com.huashidai.lvyouapp")
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body == null) {
            return ApiResponse.ok(null);
        }

        // 避免重复包装
        if (body instanceof ApiResponse) {
            return body;
        }

        // String 由 StringHttpMessageConverter 处理，包装会导致类型不匹配，直接跳过
        if (body instanceof String) {
            return body;
        }

        return ApiResponse.ok(body);
    }
}

