package com.example.enableconfigurationproperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by fenming.xue on 2021/9/7.
 */
@Component
public class Validtion implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private Config config;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        System.out.println(config.getName());
    }
}
