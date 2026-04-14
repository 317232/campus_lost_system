package com.campus.lostfound.common.exception;

import com.campus.lostfound.common.api.ResultCode;

public class BusinessException extends RuntimeException {

    private final ResultCode resultCode;

    // 默认使用 BAD_REQUEST
    public BusinessException(String message) {
        super(message);
        this.resultCode = ResultCode.BAD_REQUEST;
    }

    // 支持指定特定的 ResultCode (比如未登录时传入 UNAUTHORIZED)
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }
}
