# 基础

## 基本概念

Spring Cloud Gateway 主要由

- 路由
  - 网关中最基本的概念，由`id`  `目的地url`   `一组断言工厂` `一组过滤器`组成
- 断言
  - 如果请求url匹配断言，则转发到目的地url上。
- 过滤器
  - 匹配断言成功后，在转发到目的地url之前和之后进行特殊处理

组成。



## 基本对象

- 路由断言工厂

  - `RoutePredicateFactory`

- 路由过滤器

  - 过滤器分为`pre`和`post`类型
  - `GatewayFilterFactory`
    - 路由过滤器工厂类
  - `GatewayFilter`
    - 普通过滤器
    - 必须指定在某个具体路由上
  - `GlobalFilter`
    - 全局过滤器
    - 声明为Spring Bean就可以

- 路由上下文

  - `ServerWebExchange`

## 基本使用

### 断言

Spring Cloud GateWay 提供了很多中断言，位于`org.springframework.cloud.gateway.handler.predicate`

#### 常用的断言

- `PathRoutePredicateFactory`
  - 根据请求路径进行断言
  - 支持的路径参加`PathPattern`
    - 支持Ant风格
  - 对路径的处理参见`PathPatternParser`
- `HeaderRoutePredicateFactory`
  - 请求中必须包含指定的请其头
  - 使用正则表达式进行断言
- `CookieRoutePredicateFactory`
  - 请求中必须包含指定的cookie
- `RemoteAddrRoutePredicateFactory`
  - 允许配置的客户端的ip地址进行访问
- `WeightRoutePredicateFactory`
  - 权重进行路由

#### 示例

> 比较多，只给出一个例子

##### 外部化配置

properties配置

```properties
# 路由ID
spring.cloud.gateway.routes[0].id=jd_route
# 断言的顺序
spring.cloud.gateway.routes[0].order=1
# 根据PathRoutePredicateFactory进行断言 name 取值 参考文末总结
spring.cloud.gateway.routes[0].predicates[0].name=Path
# PathRoutePredicateFactory的规则 args key取值 参考文末总结
spring.cloud.gateway.routes[0].predicates[0].args.pattern=/crm-service/**
# 断言匹配后 转发的地址
spring.cloud.gateway.routes[0].uri=http://www.jd.com
```

yml配置

```yml
spring:
  cloud:
    gateway:
      routes:
      - id: jd_route
        uri: http://www.jd.com
        predicates:
        - Path=/crm-service/**
```

##### Java代码

```java
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(
                        r -> r
                                .path("crm-service/**")
                                .uri("http://www.jd.com")
                )
                .build();
    }
```

### 过滤器

Spring Cloud GateWay 提供了很多过滤器，位于`org.springframework.cloud.gateway.filter`包下，

#### 常用过滤器

- 转发Url过滤器
  - `NettyRoutingFilter`	
    - 主要用于转发到目标url
  - `LoadBalancerClientFilter`
    - 从目标urls根据负载均衡策略选择一个url
    - 只是选择url，不执行转发
    - 优先级高于`NettyRoutingFilter`

- `Header`相关过滤器

  - `XForwardedHeadersFilter`
  - `ForwardedHeadersFilter`
  - `RemoveHopByHopHeadersFilter`

- 限流相关

  - `RateLimiter`
    - `RedisLimiter`

- 工厂相关

  - 请求头/响应头 相关
    - 安全相关
      - `SecureHeadersGatewayFilterFactory`
    - 添加和设置
      - `AddRequestHeaderGatewayFilterFactory`
      - `AddResponseHeaderGatewayFilterFactory`
      - `SetResponseHeaderGatewayFilterFactory`
      - `SetRequestHeaderGatewayFilterFactory`
    - 删除
      - `RemoveRequestHeaderGatewayFilterFactory`
      - `RemoveResponseHeaderGatewayFilterFactory`
    - 重写响应头
      - `RewriteResponseHeaderGatewayFilterFactory`
  - 路径相关
    - `PrefixPathGatewayFilterFactory`
      - 转发前添加前缀
    - `StripPrefixGatewayFilterFactory`
      - 转发前剔除前缀
    - `RedirectToGatewayFilterFactory`
      - 转发后重定向
      - post类型
    - `RewritePathGatewayFilterFactory`
      - 转发前重写路径
      - pre类型
    - `SetPathGatewayFilterFactory`
      - 按照模板设置路径
  - Session
    - `SaveSessionGatewayFilterFactory`
  - 重试和容错降级
    - `RetryGatewayFilterFactory`
    - `HystrixGatewayFilterFactory`
      - 容错降级
  - 请求体和响应体
    - `ModifyRequestBodyGatewayFilterFactory`
    - `ModifyResponseBodyGatewayFilterFactory`

#### 示例

> 同断言类似



### 总结

- 外部化配置类`GatewayProperties`

  - 路由定义元(meta-data)信息

  - 全局过滤器

- `RouteDefinition`

  - 路由定义元信息
  - 目的地url
  - 路由断言元(meta-data)信息
  - 路由过滤器元(meta-data)信息

- `PredicateDefinition`

  - 路由断言元(meta-data)信息

  ```java
  public class PredicateDefinition {
  	/**
  	 * name 不是随便乱写的，有一定的规则
  	 * name = XXXRoutePrdicateFactory - RoutePrdicateFactory =XXX
  	 * name = NameUtils#normalizeRoutePredicateName(...)=上面
  	 */
      @NotNull
  	private String name;
      /**
       * 该Map主要用于保存对应的路由断言工厂的参数
       * Map中的key 取自于对应的断言工厂中的`Config`内部类中的参数名称
       * 比如当name=Path时 args中的key取自于 PathRoutePredicateFactory内部类Config的  		 *      pattern参数名称
       */
  	private Map<String, String> args = new LinkedHashMap<>();
  }
  ```

- `FilterDefinition`

  - 路由过滤器元信息

  ```java
  public class FilterDefinition {
      /**
       * 同上
       * name = XXXGatewayFilterFactory-GatewayFilterFactory=xxx
       * name=NameUtils.normalizeFilterFactoryName(...);
       */
  	@NotNull
  	private String name;
      /**
       * 同上 key也是取自对应的工厂中的Config参数名称
       *
       */
  	private Map<String, String> args = new LinkedHashMap<>();
  }
  ```



