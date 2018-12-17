package com.aihuishou.gateway.route;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.ipresolver.RemoteAddressResolver;
import org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZonedDateTime;

/**
 * 基于Java代码的路由断言示例
 *
 * <note>
 *
 *     多个断言之间可以使用 and or negate 逻辑操作符
 *
 * </note>
 *
 * @author jiashuai.xie
 * @since 2018/12/11 21:07
 */
@Configuration
public class RouteFilterSamples {

    public static final String HTTP_LOCALHOST_8081 = "http://localhost:8081";

    /**
     * 时间区间规则路由,根据当前时间进行路由分配
     * 多条规则按照order进行排序，优先级同{@link org.springframework.core.Ordered}
     * - Path指定了路径匹配,如不配置则匹配所有请求
     * <p>
     * after predicate
     * </p>
     * <p>
     * 请求路径在2018-09-13T13:00:00.000+08:00[Asia/Shanghai以后的时间
     * 且匹配/after_or_before_or_between/**断言 允许访问
     * <p>
     * 将转发到HTTP_LOCALHOST_8081/after_or_before_or_between/** url上
     * <p/>
     *
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator afterRouteLocator(RouteLocatorBuilder builder) {

        return builder.routes()
                .route(r ->
                        r.path("/after_or_before_or_between/**")
                                .and()
                                .after(ZonedDateTime.parse("2018-09-13T13:00:00.000+08:00[Asia/Shanghai]"))
                                .uri(HTTP_LOCALHOST_8081))
                .build();
    }

    /**
     * 查询参数断言规则
     * 查询参数的值判断是通过正则表达式 标准java 正则判断
     * 匹配断言规则 则进行转发 不匹配断言XXXRoutePredicateFactory则为404
     *
     * <p>
     * 请求必须匹配 /shopping/** 且 含有查询参数mall值符合正则表达式jd
     * <p>
     * 匹配后转发到HTTP_LOCALHOST_8081/shopping/**url上
     * </p>
     *
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator queryRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("query_route_jd",
                        r -> r.path("/shopping/**")
                                .and()
                                .query("mall", "jd")
                                .uri(HTTP_LOCALHOST_8081))
                .build();

    }

    /**
     * header断言路由规则
     * <p>
     * 请求必须含有指定header且必须符合指定的正则表达式
     * </p>
     *
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator headerRouteLocator(RouteLocatorBuilder builder) {

        return builder.routes()
                .route("header-route-locator",
                        r -> r.path("/header/**")
                                .and()
                                .header("access-token", "token")
                                .uri(HTTP_LOCALHOST_8081))
                .build();

    }

    /**
     * cookie 断言
     * <p>
     * 请求必须含有指定cookie 且值必须符合正则表达式
     * <p>
     * remoteAddr 断言
     * <p>
     * 请求的客户端Ip必须符合指定Ip地址 可用于白名单
     * <p>
     * 请求头Host代表客户端主机名
     *
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator cookieRouteLocator(RouteLocatorBuilder builder) {

        return builder.routes()
                .route("cookie-route-locator",
                        r -> r.path("/cookie/**")
                                .and()
                                .cookie("access_token", "eyJhbGciOiJIUzI1NiJ9.eyJuYW1lIjpudWxsLCJ1c2VySWQiOm51bGx9.9acmQ0NYYAQLuZGUw-vXbWKdXWKi5KwSCs4iN9xTQUc")
                                .and()
                                // 客户端Ip
                                // may not match the actual client IP address 因为可能会被Apache Nginx代理 可使用{@link RemoteAddressResolver}获取真实的Ip
                                .remoteAddr(customizerRemoteAddressResolver(), "127.0.0.1", "localhost", "0.0.0.1")
                                .uri(HTTP_LOCALHOST_8081))
                .build();

    }

    /**
     * {@link XForwardedRemoteAddressResolver} implement {@link RemoteAddressResolver} use X-Forwarded-For header 获取真实Ip
     *
     * @return
     */
    @Bean
    public RemoteAddressResolver customizerRemoteAddressResolver() {
        return XForwardedRemoteAddressResolver.maxTrustedIndex(2);

    }


    /**
     * 权重断言
     * <p>
     * 符合其他断言时，按照权重进行分发
     *
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator weightRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("weight-1-route-locator",
                        r -> r.path("/w/**")
                                .and()
                                // weight 所属分组
                                .weight("weight", 50)
                                .uri("ttp://localhost:8081/weight/v1"))
                .route("weight-2-route-locator",
                        r -> r.path("/w/**")
                                .and()
                                .weight("weight", 50)
                                .uri("http://localhost:8081/weight/v2"))
                .build();

    }

}
