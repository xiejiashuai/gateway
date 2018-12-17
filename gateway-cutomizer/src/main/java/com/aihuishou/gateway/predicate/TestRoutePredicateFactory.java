package com.aihuishou.gateway.predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.function.Predicate;

/**
 * 自定义断言  也可以实现{@link RoutePredicateFactory}
 *
 *
 * <note>
 *  必须显示的用在某个特定路由上 否则不生效
 * </note>
 * <p>
 * 必须使用{@link Component}注解
 * <p>
 * 不能使用{@link Configuration}注解  使用该注解后 生成的名字不符合{@link NameUtils}
 * <p>
 * </note>
 * <p>
 *
 *  <note>
 * 顺序通过在响应的路由中声明的顺序进行控制  参见配置文件
 *  也可以通过Java代码配置
 *
 *   return builder.routes()
 *            .route(r ->
 *                        r.path("/after_or_before_or_between/**")
 *                   .and()
 *                       .predicate(new TestRoutePredicateFactory().apply(new TestRoutePredicateFactory.Config()))
 *                    //.and()
 *                      //  .asyncPredicate()
 *            .uri(HTTP_LOCALHOST_8081))
 *            .build();
 * </note>
 *
 *
 * @author jiashuai.xie
 */
@Component
public class TestRoutePredicateFactory extends AbstractRoutePredicateFactory<TestRoutePredicateFactory.Config> {


    public TestRoutePredicateFactory() {
        super(TestRoutePredicateFactory.Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {

        return exchange -> {

            URI uri = exchange.getRequest().getURI();

            System.out.println(uri);

            return true;
        };

    }

    public static class Config {


    }

}
