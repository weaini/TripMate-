package com.huashidai.lvyouapp.exception;

public enum ErrorCode {
    SUCCESS("SUCCESS"),
    BUSINESS_ERROR("BUSINESS_ERROR"),
    AUTHENTICATION_ERROR("AUTHENTICATION_ERROR"),
    ACCESS_DENIED("ACCESS_DENIED"),
    VALIDATION_ERROR("VALIDATION_ERROR"),
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED"),
    FILE_UPLOAD_ERROR("FILE_UPLOAD_ERROR"),
    BIND_ERROR("BIND_ERROR"),
    INTERNAL_ERROR("INTERNAL_ERROR"),
    /** 积分不足（评论、点赞等消耗积分场景） */
    POINTS_INSUFFICIENT("POINTS_INSUFFICIENT");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

