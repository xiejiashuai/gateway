package com.aihuishou.gateway.dynamic.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.CachingRouteDefinitionLocator;
import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 动态更新路由信息
 * <p>
 * {@link RouteDefinitionWriter} 路由信息写入数据源 用于动态写入路由信息
 * {@link InMemoryRouteDefinitionRepository}提供的唯一路由写入数据源实现 内存版本
 * {@link ApplicationEventPublisher} 用于发布事件 对于动态路由只要发送路由刷新事件{@link RefreshRoutesEvent} 让{@link CachingRouteDefinitionLocator}清除缓存即可
 *
 * @author jiashuai.xie
 */
@Service
public class DynamicRouteHandler implements ApplicationEventPublisherAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicRouteHandler.class);

    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 路由定义写
     */
    private final RouteDefinitionWriter routeDefinitionWriter;

    public DynamicRouteHandler(RouteDefinitionWriter routeDefinitionWriter) {
        this.routeDefinitionWriter = routeDefinitionWriter;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 动态新增路由
     *
     * @return
     */
    public String add(RouteDefinition routeDefinition) {

        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();

        // 发送路由刷新事件
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));

        return "success";

    }

    /**
     * 更新路由
     *
     * @param routeDefinition 路由定义元信息
     * @return
     */
    public String update(RouteDefinition routeDefinition) {

        try {

            routeDefinitionWriter.delete(Mono.just(routeDefinition.getId())).subscribe();

        } catch (Exception e) {

            if (!(e instanceof NotFoundException)) {

                LOGGER.error("failed to update route,ex:{}", e.getMessage(), e);

                throw e;

            }

        }

        try {

            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();

            // 发送路由刷新事件
            applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));

            return "success";

        } catch (Exception e) {

            LOGGER.error("failed to update route,ex:{}", e.getMessage(), e);

            return "fail";

        }

    }

    /**
     * 删除路由
     */
    public Mono<ResponseEntity<Object>> delete(String id) {
        return routeDefinitionWriter.delete(Mono.just(id))
                .then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
                .onErrorResume(t -> t instanceof NotFoundException, t -> Mono.just(ResponseEntity.notFound().build()));
    }

}
