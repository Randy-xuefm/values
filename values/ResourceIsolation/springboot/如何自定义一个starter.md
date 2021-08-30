# 如何自定义一个starter
[toc]

Spring Boot makes it easy to create stand-alone, production-grade Spring based Applications that you can "just run".

## Features

- Create stand-alone Spring applications

- Embed Tomcat, Jetty or Undertow directly (no need to deploy WAR files)

- **Provide opinionated 'starter' dependencies to simplify your build configuration**

- **Automatically configure Spring and 3rd party libraries whenever possible**

- Provide production-ready features such as metrics, health checks, and externalized configuration

- Absolutely no code generation and no requirement for XML configuration

## springBoot AutoConfigure自动装配

- maven支持依赖jar可选
- springBoot Annotation支持
- springBoot自动扫描spring.factories配置文件

## springBoot Starter

- 引入springBoot AutoConfigure
- 引入相关依赖jar包

## 详细分析
***maven支持依赖jar可选***
```xml
<dependency>
    <groupId>com.alibaba.boot</groupId>
    <artifactId>nacos-spring-boot-base</artifactId>
    
    <optional>true</optional>
</dependency>
```
当autoConfigure项目存在多个Configuration时,需要通过starter来确认装配那些configuration.这个时候maven optional是必须的

***spring IOC在初始化Bean时可动态通过条件来判断是否加载Bean***

springboot通过
```
@ConditionalOnClass
@ConditionalOnBean
@ConditionalOnMissingBean
@ConditionalOnMissingClass
@ConditionalOnProperty
...
```
这一系列的注解来实现.其底层是spring```@Conditional```提供的.spring Boot对```@Conditional```做了封装.

***不同jar中的configuration注解如何被spring扫描到***

spring Boot通过```SpringFactoriesLoader```加载各个jar包中```META-INF/spring.factories```配置文件,最终通过
```@EnableAutoConfiguration```处理,生成spring Bean.

***starter只是用来进一步封装条件,如默认策略,ConditionalOnClass等.从底层源码可以发现,大部分starter仅仅包含一个pom文件***

## demo
