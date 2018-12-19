package com.aihuishou.gateway.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;

@SpringBootApplication
public class GatewayLoggingApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayLoggingApplication.class, args);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) //过滤器顺序
    public WebFilter webFilter() {
        return (exchange, chain) -> chain.filter(new PayloadServerWebExchangeDecorator(exchange));
    }
}
