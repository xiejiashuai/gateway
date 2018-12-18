# Spring Cloud Gateway 动态路由

Spring Cloud Gateway官方给出的动态路由并不是很完善，而且是基于`Actuator`端点做的。

参考`GatewayControllerEndpoint`

参考`GatewayControllerEndpoint`实现基于配置中新的动态路由。

> 配置中心以Ali Nacos为例



## 类的讲解

- `RouteDefinition`
  - 路由元信息(路由配置源信息)
  - 由id/目标url/断言元信息/过滤器元信息组成
- `RouteLocator`
  - 路由定位器
  - 提供了获取路由信息的方法
- `RouteDefinitionLocator`
  - 路由元信息定位器
  - 用于获取路由元信息
  - 实现类
    - `InMemoryRouteDefinitionRepository`
      - 用于获取内存版本的路由元信息
    - `PropertiesRouteDefinitionLocator`
      - 用于获取`GatewayProperties`中的路由元信息
    - `DiscoveryClientRouteDefinitionLocator`
      - 服务发现相关
    - `CachingRouteDefinitionLocator`
      - 缓存路由元信息，内部委托给其他`RouteDefinitionLocator`处理
- `RouteDefinitionWriter`
  - 路由元信息写入数据源 
  - 用于动态写入路由元信息
  - {@link InMemoryRouteDefinitionRepository}提供的唯一路由元信息写入数据源实现
    -  内存版本
- `RefreshRoutesEvent`
  - 路由刷新事件
  - `CachingRouteDefinitionLocator`监听该事件，当动态增加或修改路由元信息时，清空缓存



## 实现

- 工程中的`DynamicRouteHandler`
  - 用于更新路由元信息，及发送`RefreshRoutesEvent`
- 工程中的`NacosDynamicRouteHandler`
  - 用于监听`Nacos`事件，动态更新路由元信息
  - 内部委托给`DynamicRouteHandler`实现
- 工程中的`DynamicRouteController`
  - 提供了Rest接口用于更新路由元信息