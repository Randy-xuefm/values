package com.example.springbootconfigprocess;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by fenming.xue on 2021/9/1.
 */
@Component
public class ReadConfigService implements InitializingBean {

    @Autowired
    private Environment environment;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.print("读取配置文件,spring.application.name=");
        System.out.println(environment.getProperty("spring.application.name",String.class));
        System.out.print("读取配置文件,test=");
        System.out.println(environment.getProperty("test",String.class));

        System.out.print("读取配置文件,dev=");
        System.out.println(environment.getProperty("dev",String.class));


        System.out.print("读取系统参数,JAVA_HOME=");
        System.out.println(environment.getProperty("JAVA_HOME",String.class));


    }
}
