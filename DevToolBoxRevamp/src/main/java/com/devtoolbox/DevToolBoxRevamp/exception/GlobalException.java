package com.devtoolbox.DevToolBoxRevamp.exception;


import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalException {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleNotValidException(MethodArgumentNotValidException msg){
        String errMes=msg.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(m->m.getField()+":"+m.getDefaultMessage())
                .collect(Collectors.joining(","));
        log.error(errMes);
        return ResponseEntity.ok(ResponseHelper.error(errMes));
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse> handleBadRequestException(BadRequestException msg){
        log.error(msg.getMessage(),msg);
        return  ResponseEntity.ok(ResponseHelper.error(msg.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException msg){
        log.error(msg.getMessage(),msg);
        return ResponseEntity.ok(ResponseHelper.error("Illegal Argument......"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException msg){
        log.error(msg.getMessage(),msg);
        return ResponseEntity.ok(ResponseHelper.error("Some internal error occurred"));
    }
    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ApiResponse> handleServletException(ServletException msg){
        log.error(msg.getMessage(),msg);
        return ResponseEntity.ok(ResponseHelper.error("Token is not valid"));
    }
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse> handleIOException(IOException msg){
        log.error(msg.getMessage(),msg);
        return ResponseEntity.ok(ResponseHelper.error("Internal error occurred"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception msg){
        log.error(msg.getMessage(),msg);
        return ResponseEntity.ok(ResponseHelper.error(msg.getMessage()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolationException(DataIntegrityViolationException msg){
        log.error(msg.getMessage(),msg);
        return ResponseEntity.ok(ResponseHelper.error("Cannot create same thing again........"));
    }
}
