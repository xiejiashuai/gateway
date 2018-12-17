package com.aihuishou.gateway.filter.global;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

/**
 * customizerGlobalFilter1 定义的全局过滤器 高于 customizerGlobalFilter2定义的全局过滤器
 *
 * <note>
 *
 *     但是这这种形式定义的过滤器 的优先级低于 内建的过滤器
 *
 *     参见{@link CustomizerGlobalFilter}
 *
 * </note>
 *
 * @author jiashuai.xie
 */
@Configuration
public class GlobalFilterConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GlobalFilterConfiguration.class);


    @Bean
    @Order(-1)
    public GlobalFilter customizerGlobalFilter1() {
        return (exchange, chain) -> {
            logger.info("first pre filter");
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                logger.info("second post filter");
            }));
        };
    }

    @Bean
    @Order(1)
    public GlobalFilter customizerGlobalFilter2() {
        return (exchange, chain) -> {
            logger.info("second pre filter");
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                logger.info("first post filter");
            }));
        };
    }

}
