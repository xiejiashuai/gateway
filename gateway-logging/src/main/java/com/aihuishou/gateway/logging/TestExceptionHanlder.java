package com.aihuishou.gateway.logging;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class TestExceptionHanlder {

    @ExceptionHandler({RuntimeException.class})
    public String errr(RuntimeException ex, ServerWebExchange exchange) {
        return ex.getMessage();
    }

}
