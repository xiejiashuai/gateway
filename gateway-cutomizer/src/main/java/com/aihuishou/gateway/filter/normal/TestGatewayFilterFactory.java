package com.aihuishou.gateway.filter.normal;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 普通过滤器 还可以继承{@link GatewayFilterFactory}的抽象类
 * <p>
 * <note>
 * 不同于全局过滤器
 * 该过滤器必须显示的用在某个特定路由上 否则不生效
 * </note>
 * <p>
 * <p>
 * 和全局过滤器一样 普通过滤器也有顺序
 * <p>
 * 顺序的定义由Order制定 {@link Order} or {@link Ordered}
 * </p>
 * <p>
 * <p>
 * 创建有顺序的过滤器 还可以通过{@link OrderedGatewayFilter}类实现
 * <p>
 * </p>
 * <p>
 * <note>
 * <p>
 * 必须使用{@link Component}注解
 * <p>
 * 不能使用{@link Configuration}注解  使用该注解后 生成的名字不符合{@link NameUtils}
 * <p>
 * </note>
 *
 * <note>
 *  顺序通过在响应的路由中声明的顺序进行控制
 * </note>
 *
 * @author jiashuai.xie
 */
@Component
public class TestGatewayFilterFactory extends AbstractGatewayFilterFactory<TestGatewayFilterFactory.Config> {

    public TestGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

         /*在这里对GatewayFilter初始化做一些其他事情*/

        return (exchange, chain) -> {

            System.out.println("CustomizerGatewayFilter pre ....");

            return chain.filter(exchange).then(Mono.fromRunnable(() ->

                    System.out.println("CustomizerGatewayFilter post ....")

            ));
        };
    }


    public static class Config {


    }
}
