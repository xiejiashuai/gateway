package com.aihuishou.gateway.dynamic.route;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
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
public class NacosDynamicRouteHandler {

    private final DynamicRouteHandler delegate;


    public NacosDynamicRouteHandler(DynamicRouteHandler dynamicRouteService) {
        this.delegate = dynamicRouteService;
        dynamicRouteByNacosListenerByYml("gateway", "crm-service");
    }


    /**
     * 监听Nacos Server下发的动态路由配置 使用原声的json 更新GatewayProperties
     *
     * @param dataId
     * @param group
     */
    public void dynamicRouteByNacosListenerByJson(String dataId, String group) {
        try {

            // todo
            ConfigService configService = NacosFactory.createConfigService("127.0.0.1:8848");
            String content = configService.getConfig(dataId, group, 5000);
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
            //todo 提醒:异常自行处理此处省略
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

            ConfigService configService = NacosFactory.createConfigService("127.0.0.1:8848");

            NacosFactory.createConfigService("127.0.0.1:8848");

            String content = configService.getConfig(dataId, group, 5000);

            System.out.println(content);

            configService.addListener(dataId, group, new Listener() {

                @Override
                public void receiveConfigInfo(String configInfo) {


                    YamlPropertySourceLoader yamlPropertySourceLoader = new YamlPropertySourceLoader();
                    List<PropertySource<?>> sources = null;
                    try {
                        sources = yamlPropertySourceLoader.load("gateway-yml", new InputStreamResource(getStringStream(configInfo)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


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

            ConfigService configService = NacosFactory.createConfigService("127.0.0.1:8848");

            NacosFactory.createConfigService("127.0.0.1:8848");

            String content = configService.getConfig(dataId, group, 5000);

            System.out.println(content);

            configService.addListener(dataId, group, new Listener() {

                @Override
                public void receiveConfigInfo(String configInfo) {

                    Properties properties = null;
                    try {
                        properties = PropertiesLoaderUtils.loadProperties(new InputStreamResource(getStringStream(configInfo)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    PropertiesPropertySource source = new PropertiesPropertySource("gateway", properties);

                    ConfigurableEnvironment configurableEnvironment=new StandardEnvironment();
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

        }
    }
    public InputStream getStringStream(String sInputString) {


        ByteArrayInputStream inputStream = new ByteArrayInputStream(sInputString.getBytes(Charset.forName("utf-8")));

        return inputStream;

    }

}
