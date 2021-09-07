package com.example.enableconfigurationproperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by fenming.xue on 2021/9/7.
 */
@Component
@ConfigurationProperties(prefix = "spring.example")
public class Config {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
