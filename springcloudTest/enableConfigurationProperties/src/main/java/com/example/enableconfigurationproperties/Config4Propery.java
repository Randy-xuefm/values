package com.example.enableconfigurationproperties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Created by fenming.xue on 2021/9/7.
 */
@PropertySource(value = {"classpath:/config2.properties"})
@Component
public class Config4Propery {

    @Value("${spring.config2.name}")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
