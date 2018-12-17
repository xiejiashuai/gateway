package com.aihuishou.gateway.swagger;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.StripPrefixGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import springfox.documentation.swagger2.web.Swagger2Controller;

/**
 * 把bast-path放入到请求头X-Forwarded-Prefix中去
 * <p>
 * 不然在执行try-out的时候报404
 * 原因 会经过{@link StripPrefixGatewayFilterFactory}过滤器的处理，该处理器会把前缀给剔除掉
 * <p>
 * 而swagger会获取X-Forwarded-Prefix请求头的值作为前缀进行拼接
 * <p>
 * <p>
 * 假设位于Spring Cloud Gateway后的服务是crm-service  现在要通过Gateway获取crm-service的swagger文档信息
 * <p>
 * 断言:/api-gateway/crm-service/**
 * <p>
 * 由于配置了{@link StripPrefixGatewayFilterFactory} 在访问crm-service的时候  会剔除掉/api-gateway
 * <p>
 * 所以crm-service中的{@link Swagger2Controller}设置basePath为/crm-service/** 缺少/api-gateway前缀
 * <p>
 * 在执行try-out的时候 swagger-ui给拼装的路径为ip:port/crm-service/** 缺少了/api-gateway前缀 所以导致404
 * <p>
 * 解决办法
 * <p>
 * {@link Swagger2Controller}
 * 在获取basePath之前会调用HostNameProvider#componentsFrom(...)方法
 * 进而调用 XForwardPrefixPathAdjuster#adjustedPath(...)方法 该方法会获取请求头X-Forwarded-Prefix的值作为basePath
 * <p>
 * 所以只需要在转发请求前 写入X-Forwarded-Prefix请求头的值为/api-gateway/crm-service/即可
 * <p>
 * 这样crm-service中的{@link Swagger2Controller}设置的basePath就是/api-gateway/crm-service
 * <p>
 * <p>
 * 详细 可debug 断点 Swagger2Controller#getDocumentation(String, HttpServletRequest)方法
 * <p>
 * </p>
 * <p>
 * <p>
 * <note>
 * <p>
 * 一定要保证该过滤器的执行早于 内建的{@link StripPrefixGatewayFilterFactory}过滤器
 * <p>
 *      顺序的保证 参见 gateway-customizer工程
 * <p>
 * </note>
 *
 * @author jiashuai.xie
 */
@Component
public class AddXForwardedPrefixHeaderGatewayFilterFactory implements GlobalFilter, Ordered {

    private static final String HEADER_NAME = "X-Forwarded-Prefix";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        // 该请求不是获取Swagger文档
        if (!StringUtils.endsWithIgnoreCase(path, GatewaySwaggerProvider.API_URI)) {
            return chain.filter(exchange);
        }

        // 该请求是在执行swagger文档上的try-out 需要把basePath追加到header中
        String basePath = path.substring(0, path.lastIndexOf(GatewaySwaggerProvider.API_URI));
        ServerHttpRequest newRequest = request.mutate().header(HEADER_NAME, basePath).build();
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
