# 自定义路由断言和过滤器

## 自定义路由过滤器

Spring Cloud Gateway中的过滤器

从作用域上分为两类

- 全局过滤器`GlobalFilter`
- 普通过滤器`GatewayFilter`

从过程执行上分为三类

- `pre`类型
- `post`类型
- `pre`和`post`结合



### 自定义全局过滤器

#### 方式一

- 实现`GlobalFilter`接口

  > 如果想控制顺序，可实现`Ordered`接口

- 声明为`Spring Bean`
- 实例参考工程中的`CustomizerGlobalFilter`

> 此种方式定义的全局过滤器，优先级可以高于内建的过滤器

#### 方式2

- 参考工程中的`GlobalFilterConfiguration`



### 自定义普通过滤器

>  `普通`过滤器意味着必须显示的配置在某个特定路由上，否则不生效

- 继承`AbstractGatewayFilterFactory`类或者实现`GatewayFilterFactory`接口

- 声明为`Spring Bean`

- 实例参考工程中的`TestGatewayFilterFactory`
  > 注意该类上的类注释



## 自定义断言

- `继承`AbstractRoutePredicateFactory类
- 声明`Spring Bean`

- 实例参考工程中的`TestRoutePredicateFactory`



配置方式

- 外部化配置

- Java Config 

  ```java
  return builder.routes()
               .route(r ->
                           r.path("/after_or_before_or_between/**")
                      .and()
                          .predicate(new TestRoutePredicateFactory().apply(new TestRoutePredicateFactory.Config()))
                       //.and()
                         //  .asyncPredicate()
               .uri(HTTP_LOCALHOST_8081))
               .build();
  ```




> 注意：
>
> ​	1 无论是自定义普通过滤器还是自定义断言都需要显示的配置在某个特定路由上
>
> ​	2 通过外部化配置普通过滤器或者断言时，`name` `args`参数配置遵循一定规则，参考`gateway-basic`		  中的READDE文档