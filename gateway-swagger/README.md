## Gateway聚合Swagger文档

Swagger暂时不支持与Spring WebFlux的聚合，所以需要些特殊整合。



### 配置Swagger文档源

- 实现`SwaggerResourcesProvider`
  - 创建`SwaggerResource`
    - 重点在于设置`SwaggerResource#location`属性
    - `swagger-ui`页面将会向该`location`发出请求，获取json信息
      - `location`一般等于`http://ip:port/gateway-contextpath/服务-contextpath/v2/api-docs`

- 声明为`Spring Bean`

- 实例参考工程中的`GatewaySwaggerProvider`





### 配置Swagger所需的接口

- 提供`/swagger-resources/configuration/security`接口
  - 入参:空
  - 出参:`ResponseEntity<SecurityConfiguration>`
  - method:`GET`
- 提供`/swagger-resources/configuration/ui`
  - 入参:空
  - 出参:`ResponseEntity<UiConfiguration>`
  - method:`GET`
- 提供`/swagger-resources`接口
  - 入参:空
  - 出参:`ResponseEntity`

- 实例参考工程`SwaggerHandler`类



### Swagger文档`try-out`404问题

#### 问题产生原因描述

##### 前提

假设位于`Spring Cloud Gateway`后的服务是`crm-service`  现在要通过`Gateway`获取`crm-service`的`swagger`文档信息

##### 路由断言

`/api-gateway/crm-service/**`

##### 路由过滤器

`StripPrefixGatewayFilterFactory`

##### 描述

```
由于配置了{@link StripPrefixGatewayFilterFactory} 在访问crm-service的时候  会剔除掉/api-gateway
```

```
所以crm-service中的{@link Swagger2Controller}设置basePath为/crm-service/** 缺少/api-gateway前缀
```

```
因此，在执行try-out的时候 swagger-ui给拼装的路径为ip:port/crm-service/** 缺少了/api-gateway前缀 所以导致404
```

##### 解决办法

原理

```
{@link Swagger2Controller}在获取basePath之前会调用HostNameProvider#componentsFrom(...)方法
进而调用 XForwardPrefixPathAdjuster#adjustedPath(...)方法 该方法会获取请求头X-Forwarded-Prefix的值作为basePath
```

因此

```
只需要在转发请求前 写入X-Forwarded-Prefix请求头的值为/api-gateway/crm-service/即可
```

##### 实现

- 参考工程中的`AddXForwardedPrefixHeaderGatewayFilterFactory`

  > 一定要保证`AddXForwardedPrefixHeaderGatewayFilterFactory`过滤器的优先级高于`StripPrefixGatewayFilterFactory`过滤器，否则设置的请求头的值仍然不包含`/api-gateway  ` `context-path`

  > 顺序的保证参考工程`gateway-customizer`
