package com.example;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.alibaba.nacos.client.naming.utils.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * Created by fenming.xue on 2021/8/12.
 */
public class NamingRegister implements ApplicationListener<ApplicationReadyEvent> {

    private final Logger logger = LoggerFactory.getLogger(NamingRegister.class);

    private final NacosProperties config;

    @Value("${spring.application.name}")
    private String serverName;

    @Value("${server.port}")
    private int port;

    public NamingRegister(NacosProperties config){
        this.config = config;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        NamingService namingService = null;
        try {
            namingService = new NacosNamingService(config.getServerAddr());
        } catch (NacosException e) {
            this.logger.error("create NamingServer error",e);
        }

        if(namingService == null){
            this.logger.warn("NamingServer is null,can't register to Nacos Naming.");
            return;
        }

        try {
            namingService.registerInstance(serverName, Constants.DEFAULT_GROUP, NetUtils.localIP(), port , Constants.DEFAULT_CLUSTER_NAME);
        } catch (NacosException e) {
            this.logger.error("register NamingServer error",e);
        }
    }
}
