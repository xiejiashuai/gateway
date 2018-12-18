package com.aihuishou.gateway.dynamic.route;

import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 *
 * web 请求 动态路由
 *
 * @author jiashuai.xie
 */
@RestController
@RequestMapping(value = "/dynamic/route", consumes = "application/json;charset=utf-8", produces = "application/json;charset=utf-8")
public class DynamicRouteController {

    private final DynamicRouteHandler dynamicRouteHandler;

    public DynamicRouteController(DynamicRouteHandler dynamicRouteHandler) {
        this.dynamicRouteHandler = dynamicRouteHandler;
    }


    @PostMapping(value = "/add")
    public Mono<String> add(@RequestBody GatewayProperties gatewayProperties) {

        return Mono
                .fromRunnable(
                        () -> gatewayProperties.getRoutes().forEach(dynamicRouteHandler::add)
                )
                .then(Mono.just("success"));

    }

    @PostMapping(value = "/update")
    public Mono<String> update(@RequestBody RouteDefinition routeDefinition) {

        return Mono.just(dynamicRouteHandler.update(routeDefinition));

    }


    @GetMapping(value = "/delete")
    public Mono<ResponseEntity<Object>> delete(@RequestParam String id) {
        return dynamicRouteHandler.delete(id);
    }


}
