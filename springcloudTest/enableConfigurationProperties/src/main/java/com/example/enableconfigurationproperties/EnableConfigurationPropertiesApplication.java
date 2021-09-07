package com.example.enableconfigurationproperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//这里不用写EnableConfigurationProperties,是因为在autoconfigure项目中,用@EnableAutoConfiguration已经自动导入
//如果在EnableConfigurationProperties中指定value,在Config.java上边就可以不声明为bean
//@EnableConfigurationProperties(value = Config.class)
public class EnableConfigurationPropertiesApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnableConfigurationPropertiesApplication.class, args);
    }

}
