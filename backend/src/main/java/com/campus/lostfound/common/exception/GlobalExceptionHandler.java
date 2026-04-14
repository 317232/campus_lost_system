package com.campus.lostfound.common.exception;

import com.campus.lostfound.common.api.ApiResponse;
import com.campus.lostfound.common.api.ResultCode;

import jakarta.validation.ConstraintViolationException;

import java.util.stream.Collectors;
import org.springframework.validation.BindException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    /**
     * 1. 处理自定义业务异常 (BusinessException)
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        log.warn("业务异常: {}", exception.getMessage());
        
        // 根据不同类型的业务报错，映射不同的 HTTP 状态码
        HttpStatus status = HttpStatus.BAD_REQUEST; // 默认 400
        if (exception.getResultCode() == ResultCode.UNAUTHORIZED) {
            status = HttpStatus.UNAUTHORIZED; // 401
        } else if (exception.getResultCode() == ResultCode.FORBIDDEN) {
            status = HttpStatus.FORBIDDEN; // 403
        }

        return ResponseEntity.status(status)
                .body(ApiResponse.failure(exception.getResultCode(), exception.getMessage()));
    }

    /**
     * 2. 优化：精准处理 @Valid 参数校验异常
     * 提取 DTO 中定义的 message (如 "学号不能为空")
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        // 将所有校验失败的字段信息拼接成一个友好的字符串
        String errorMessage = exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
                
        log.warn("参数校验失败: {}", errorMessage);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, errorMessage));
    }

    /**
     * 3. 处理其他的请求/解析异常
     */
    @ExceptionHandler({
        BindException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
        log.warn("请求体解析或绑定异常", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(ResultCode.BAD_REQUEST, "请求参数格式错误或不可读"));
    }

    /**
     * 4. 处理未知异常（兜底异常）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("系统发生未知异常", exception);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(ResultCode.INTERNAL_ERROR, "服务器内部错误，请联系管理员"));
    }
}
