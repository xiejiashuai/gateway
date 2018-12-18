package com.aihuishou.gateway.dynamic.route;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

@Configuration
public class NacosDynamicRouteHandler implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosDynamicRouteHandler.class);

    private final DynamicRouteHandler delegate;

    /**
     * 类似application.properties
     */
    private final String dataId;

    /**
     * 应用名称
     */
    private final String group;

    private final ConfigService configService;

    public NacosDynamicRouteHandler(DynamicRouteHandler dynamicRouteService,
                                    @Value("${spring.nacos.server.addr}") String serverAddr,
                                    @Value("${spring.nacos.namespace:}") String namespace,
                                    @Value("${spring.nacos.dataId}") String dataId,
                                    @Value("${spring.nacos.group}") String group) throws NacosException {
        this.delegate = dynamicRouteService;
        this.dataId = dataId;
        this.group = group;
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.NAMESPACE, namespace);
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        this.configService = NacosFactory.createConfigService(properties);
    }

    /**
     * 监听Nacos Server下发的动态路由配置 使用原声的json 更新GatewayProperties
     *
     * @param dataId
     * @param group
     */
    public void dynamicRouteByNacosListenerByJson(String dataId, String group) {

        try {

            // content 是json格式
            String content = configService.getConfig(dataId, group, 5000);

            LOGGER.info("nacos dynamic route receive config:{}", content);

            configService.addListener(dataId, group, new Listener() {

                @Override
                public void receiveConfigInfo(String configInfo) {

                    GatewayProperties gatewayProperties = JSON.parseObject(configInfo, GatewayProperties.class);

                    if (gatewayProperties != null && !CollectionUtils.isEmpty(gatewayProperties.getRoutes())) {

                        gatewayProperties.getRoutes().forEach(delegate::update);

                    }
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }

            });

        } catch (NacosException e) {

            LOGGER.error("get config error,ex:{}", e.getErrMsg(), e);

        }

    }


    /**
     * 监听Nacos Server下发的动态路由配置 读取的格式是yml
     *
     * @param dataId
     * @param group
     */
    public void dynamicRouteByNacosListenerByYml(String dataId, String group) {

        try {

            String content = configService.getConfig(dataId, group, 5000);

            LOGGER.info("nacos dynamic route receive config:{}", content);

            configService.addListener(dataId, group, new Listener() {

                @Override
                public void receiveConfigInfo(String configInfo) {

                    YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();

                    List<PropertySource<?>> sources;

                    try {

                        sources = yamlPropertySourceLoader.load("gateway-yml", new InputStreamResource(getStringStream(configInfo)));

                    } catch (IOException e) {

                        LOGGER.error("load yml config error,ex:{}", e.getMessage(), e);

                        return;

                    }

                    /**
                     * 不能使用Spring 内建的ConfigurableEnvironment
                     * 不然Spring 会自动帮我们创建GatewayProperties bean
                     * 继而会创建{@link org.springframework.cloud.gateway.config.PropertiesRouteDefinitionLocator}
                     * 继而当我们在Nacos上动态更新路由的时候 会导致更新前的路由和更新后的路由都有效的BUG
                     *
                     * */
                    ConfigurableEnvironment configurableEnvironment = new StandardEnvironment();

                    sources.forEach(source -> configurableEnvironment.getPropertySources().addFirst(source));

                    GatewayProperties gatewayProperties = Binder.get(configurableEnvironment).bind("spring.cloud.gateway", GatewayProperties.class).get();

                    if (gatewayProperties != null && !CollectionUtils.isEmpty(gatewayProperties.getRoutes())) {

                        gatewayProperties.getRoutes().forEach(delegate::update);

                    }

                }

                @Override
                public Executor getExecutor() {
                    return null;
                }

            });
        } catch (NacosException e) {

            LOGGER.error("get config error,ex:{}", e.getErrMsg(), e);
        }
    }

    /**
     * 监听Nacos Server下发的动态路由配置  读取的是properties
     *
     * @param dataId
     * @param group
     */
    public void dynamicRouteByNacosListenerByProperties(String dataId, String group) {
        try {

            String content = configService.getConfig(dataId, group, 5000);

            LOGGER.info("nacos dynamic route receive config:{}", content);

            configService.addListener(dataId, group, new Listener() {

                @Override
                public void receiveConfigInfo(String configInfo) {

                    Properties properties;

                    try {

                        properties = PropertiesLoaderUtils.loadProperties(new InputStreamResource(getStringStream(configInfo)));

                    } catch (IOException e) {

                        LOGGER.error("load yml config error,ex:{}", e.getMessage(), e);

                        return;

                    }

                    PropertiesPropertySource source = new PropertiesPropertySource("gateway", properties);

                    /**
                     * 不能使用Spring 内建的ConfigurableEnvironment
                     * 不然Spring 会自动帮我们创建GatewayProperties bean
                     * 继而会创建{@link org.springframework.cloud.gateway.config.PropertiesRouteDefinitionLocator}
                     * 继而当我们在Nacos上动态更新路由的时候 会导致更新前的路由和更新后的路由都有效的BUG
                     *
                     * */
                    ConfigurableEnvironment configurableEnvironment = new StandardEnvironment();
                    configurableEnvironment.getPropertySources().addFirst(source);

                    GatewayProperties gatewayProperties = Binder.get(configurableEnvironment).bind("spring.cloud.gateway", GatewayProperties.class).get();

                    if (gatewayProperties != null && !CollectionUtils.isEmpty(gatewayProperties.getRoutes())) {

                        gatewayProperties.getRoutes().forEach(delegate::update);

                    }

                }

                @Override
                public Executor getExecutor() {
                    return null;
                }

            });
        } catch (NacosException e) {

            LOGGER.error("get config error,ex:{}", e.getErrMsg(), e);

        }
    }

    public InputStream getStringStream(String sInputString) {

        ByteArrayInputStream inputStream = new ByteArrayInputStream(sInputString.getBytes(Charset.forName("utf-8")));

        return inputStream;

    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        dynamicRouteByNacosListenerByYml(dataId,group);
    }


}
