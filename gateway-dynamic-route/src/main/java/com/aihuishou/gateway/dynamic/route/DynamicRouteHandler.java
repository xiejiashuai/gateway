package com.aihuishou.gateway.dynamic.route;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DynamicRouteHandler implements ApplicationEventPublisherAware {

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
     * @return
     */
    public String add(RouteDefinition routeDefinition) {

        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();

        // 发送路由刷新事件
        applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));

        return "success";

    }

    public String update(RouteDefinition routeDefinition) {

        try {

            routeDefinitionWriter.delete(Mono.just(routeDefinition.getId())).subscribe();

        } catch (Exception e) {

            // NotFoundException

            System.out.println(e.getMessage());
        }

        try {

            routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();

            // 发送路由刷新事件
            applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));

            return "success";

        } catch (Exception e) {

            return "update route  fail";

        }

    }

    //删除路由
    public Mono<ResponseEntity<Object>> delete(String id) {
        return routeDefinitionWriter.delete(Mono.just(id))
                .then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
                .onErrorResume(t -> t instanceof NotFoundException, t -> Mono.just(ResponseEntity.notFound().build()));
    }

}
