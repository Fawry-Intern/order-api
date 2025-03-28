package com.fawry.order_api.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalActionException.class)
    public ResponseEntity<Map<String, String>> handleIllegalActionException(
            IllegalActionException e
    ) {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("error", e.getMessage());
        return ResponseEntity.status(BAD_REQUEST).body(info);
    }

}
