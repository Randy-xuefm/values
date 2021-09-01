package com.example;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by fenming.xue on 2021/8/31.
 */
@Configuration
@ConditionalOnProperty(value = "nacos.naming.enable" ,matchIfMissing = true)
@EnableConfigurationProperties(NacosProperties.class)
public class NacosAutoConfiguration {

    @Bean
    public NamingRegister namingRegister(NacosProperties config){
        return new NamingRegister(config);
    }
}
