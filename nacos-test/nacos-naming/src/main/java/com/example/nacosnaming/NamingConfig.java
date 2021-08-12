package com.example.nacosnaming;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by fenming.xue on 2021/8/12.
 */
@ConfigurationProperties(prefix="nacos.naming")
@Component
public class NamingConfig {
    private String serverAddr;

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }
}
