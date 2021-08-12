package com.example.nacosnaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class NacosNamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(NacosNamingApplication.class, args);
    }

}
