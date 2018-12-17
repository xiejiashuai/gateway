package com.aihuishou.gateway.filter.global;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 这种方式定义的全局过滤器 只要Order足够小 则可以高于内建的过滤器
 *
 * @author jiashuai.xie
 */
@Configuration
public class CustomizerGlobalFilter implements GlobalFilter ,Ordered {



    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        /* pre 操作*/

        System.out.println("CustomizerGlobalFilter  pre ...");

        return chain.filter(exchange).then(Mono.fromRunnable(() ->
             /* post 操作*/
                System.out.println("CustomizerGlobalFilter  post ...")
        ));

    }

    @Override
    public int getOrder() {

        return Ordered.HIGHEST_PRECEDENCE;
    }
}
