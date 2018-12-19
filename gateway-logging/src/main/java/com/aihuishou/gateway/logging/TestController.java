package com.aihuishou.gateway.logging;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping
    public Mono<String> test(@RequestParam String name) {
        return Mono.just(name);
    }

    @GetMapping("ex")
    public Mono<String> exception(@RequestParam String name) {
        throw new RuntimeException("测试");

    }
}
