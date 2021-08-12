# springboot内嵌容器如何自动启动

目前springboot支持的容器有:tomcat,jetty,undertow,netty.其中tomcat,jetty,undertow基于
Servlt实现的,netty是webflux的容器.

我们都知道,springboot开发的web项目可以用命令行```java -jar webapp.jar```来启动.
接下来分析springboot内嵌容器是如何自动启动的.

### 内嵌容器
springboot提供了多个spring context.在```SpringApplication```中通过属性
```webApplicationType```来决定使用哪个容器.代码如下:
```
protected ConfigurableApplicationContext createApplicationContext() {
    return this.applicationContextFactory.create(this.webApplicationType);
}

ApplicationContextFactory DEFAULT = (webApplicationType) -> {
    try {
        switch (webApplicationType) {
        case SERVLET:
            return new AnnotationConfigServletWebServerApplicationContext();
        case REACTIVE:
            return new AnnotationConfigReactiveWebServerApplicationContext();
        default:
            return new AnnotationConfigApplicationContext();
        }
    }
    catch (Exception ex) {
        throw new IllegalStateException("Unable create a default ApplicationContext instance, "
        + "you may need a custom ApplicationContextFactory", ex);
    }
};
```
以```AnnotationConfigServletWebServerApplicationContext```为例,从源码中看一下如何实现的.
```
//ServletWebServerApplicationContext.java
@Override
public final void refresh() throws BeansException, IllegalStateException {
    try {
        //spring 容器启动入口
        super.refresh();
    }
    catch (RuntimeException ex) {
        WebServer webServer = this.webServer;
        if (webServer != null) {
            webServer.stop();
        }
        throw ex;
    }
}

@Override
protected void onRefresh() {
    super.onRefresh();
    try {
        createWebServer();
    }
    catch (Throwable ex) {
        throw new ApplicationContextException("Unable to start web server", ex);
    }
}

private void createWebServer() {
    WebServer webServer = this.webServer;
    ServletContext servletContext = getServletContext();
    if (webServer == null && servletContext == null) {
        StartupStep createWebServer = this.getApplicationStartup().start("spring.boot.webserver.create");
        ServletWebServerFactory factory = getWebServerFactory();
        createWebServer.tag("factory", factory.getClass().toString());
        this.webServer = factory.getWebServer(getSelfInitializer());
        createWebServer.end();
        getBeanFactory().registerSingleton("webServerGracefulShutdown",
                new WebServerGracefulShutdownLifecycle(this.webServer));
        getBeanFactory().registerSingleton("webServerStartStop",
                new WebServerStartStopLifecycle(this, this.webServer));
    }
    else if (servletContext != null) {
        try {
            getSelfInitializer().onStartup(servletContext);
        }
        catch (ServletException ex) {
            throw new ApplicationContextException("Cannot initialize servlet context", ex);
        }
    }
    initPropertySources();
}
```

### 内嵌容器自动启动
容器的自动启动是通过```SmartLifecycle```接口实现的.具体代码如下:
```
//WebServerStartStopLifecycle.java
@Override
public void start() {
    this.webServer.start();
    this.running = true;
    this.applicationContext
            .publishEvent(new ServletWebServerInitializedEvent(this.webServer, this.applicationContext));
}
```
接下来主要分析下```SmartLifecycle```相关bean是在什么时候调用的.

