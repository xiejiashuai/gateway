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

    private final DynamicRouteHandler dynamicRouteService;

    public DynamicRouteController(DynamicRouteHandler dynamicRouteService) {
        this.dynamicRouteService = dynamicRouteService;
    }


    @PostMapping(value = "/add")
    public Mono<String> add(@RequestBody GatewayProperties gatewayProperties) {

        return Mono
                .fromRunnable(
                        () -> gatewayProperties.getRoutes().forEach(dynamicRouteService::add)
                )
                .then(Mono.just("success"));

    }

    @PostMapping(value = "/update")
    public Mono<String> update(@RequestBody RouteDefinition routeDefinition) {

        return Mono.just(dynamicRouteService.update(routeDefinition));

    }


    @GetMapping(value = "/delete")
    public Mono<ResponseEntity<Object>> update(@RequestParam String id) {
        return dynamicRouteService.delete(id);
    }


}
