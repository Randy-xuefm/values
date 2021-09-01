package com.example;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by fenming.xue on 2021/8/31.
 */
@ConfigurationProperties(prefix = "nacos.naming")
public class NacosProperties {
    private String serverAddr;

    private String contextPath;

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
}
