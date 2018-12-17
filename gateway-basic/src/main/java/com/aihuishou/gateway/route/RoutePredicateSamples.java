package com.aihuishou.gateway.route;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 基于Java代码的路由过滤器示例
 *
 * <note>
 * <p>
 * 多个断言之间可以使用 and or negate 逻辑操作符
 *
 * </note>
 *
 * @author jiashuai.xie
 * @since 2018/12/11 21:07
 */
@Configuration
public class RoutePredicateSamples {

    public static final String HTTP_LOCALHOST_8081 = "http://localhost:8081";

    /**
     * 匹配断言后 转发请求到HTTP_LOCALHOST_8081/header-add/**增加请求头 Add-Header=Add-Header-Value
     * <p>
     * 操作请求头有很多过滤器 不一一示例
     *
     * @return RouteLocator
     */
    @Bean
    public RouteLocator addHeaderFilterRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r ->
                        r.path("/request-header-add/**")
                                .filters(f -> f.addRequestHeader("Add-Header", "Add-Header-Value"))
                                .uri(HTTP_LOCALHOST_8081))
                .build();
    }

    /**
     * 匹配断言后转发请求钱 剔除路径
     * <p>
     * 转发到HTTP_LOCALHOST_8081/crm-service/**url上
     * </p>
     * 操作路径的有很多
     *
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator stripPrefixPathRouteFilterLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/strip-path/crm-service/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri(HTTP_LOCALHOST_8081))
                .build();

    }


    /**
     * 组合过滤器路由示例
     *
     * 过滤器可以相互组合
     *
     * <note>
     *     过滤器执行有顺序 由{@link Order#value()}值决定
     * </note>
     *
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator compositeRouteFilterLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r ->
                        r.path("/strip-path/crm-service/**")
                                .filters(f -> f
                                        .stripPrefix(1)
                                        .addResponseHeader("Add-Header", "Add-Header-Value")
                                        // 添加自定义过滤器
//                                        .filter()
                                )
                                .uri(HTTP_LOCALHOST_8081))
                .build();

    }


}
