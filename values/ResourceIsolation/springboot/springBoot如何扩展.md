# springBoot如何扩展

首先从springBoot启动类```SpringApplication```谈起,看看整个springBoot启动过程.

从```SpringApplicationRunListeners```看看整个启动过程
```java
class SpringApplicationRunListeners {
    /**
     *springBoot开始启动
     * 对应事件:ApplicationStartingEvent
     */
    void starting(ConfigurableBootstrapContext bootstrapContext, Class<?> mainApplicationClass) {
        doWithListeners("spring.boot.application.starting", (listener) -> listener.starting(bootstrapContext),
                        (step) -> {
                            if (mainApplicationClass != null) {
                                step.tag("mainApplicationClass", mainApplicationClass.getName());
                            }
                        });
    }

    /**
     *springBoot环境准备完毕
     * 对应事件:ApplicationEnvironmentPreparedEvent
     */
    void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
        doWithListeners("spring.boot.application.environment-prepared",
                        (listener) -> listener.environmentPrepared(bootstrapContext, environment));
    }

    /**
     *springBoot Context容器准备完毕
     * 对应事件:ApplicationContextInitializedEvent
     */
    void contextPrepared(ConfigurableApplicationContext context) {
        doWithListeners("spring.boot.application.context-prepared", (listener) -> listener.contextPrepared(context));
    }

    /**
     *springBoot ConText容器加载完毕
     * 对应事件:ApplicationPreparedEvent
     */
    void contextLoaded(ConfigurableApplicationContext context) {
        doWithListeners("spring.boot.application.context-loaded", (listener) -> listener.contextLoaded(context));
    }

    /**
     * springboot启动完毕
     * 对应事件:ApplicationStartedEvent
     */
    void started(ConfigurableApplicationContext context) {
        doWithListeners("spring.boot.application.started", (listener) -> listener.started(context));
    }

    /**
     * springBoot运行中
     * 对应事件:ApplicationReadyEvent
     */
    void running(ConfigurableApplicationContext context) {
        doWithListeners("spring.boot.application.running", (listener) -> listener.running(context));
    }

    /**
     *springBoot启动失败
     * 对应事件:ApplicationFailedEvent
     */
    void failed(ConfigurableApplicationContext context, Throwable exception) {
        doWithListeners("spring.boot.application.failed",
                        (listener) -> callFailedListener(listener, context, exception), (step) -> {
                    step.tag("exception", exception.getClass().toString());
                    step.tag("message", exception.getMessage());
                });
    }
}
```

接下来详细分析一下,每个阶段springboot做了哪些事情.
- **ApplicationStartingEvent**

在springBoot中用于初始化```Logger```相关的一些操作.
- **ApplicationEnvironmentPreparedEvent**

在springBoot中,不同的容器使用不同的Environment,在这个阶段,会将配置文件加载到Environment,包括环境变量.
```ConfigDataEnvironmentPostProcessor```读取知道目录下的配置文件,参数```spring.config.name```允许配置多个名称,默认为application.
还可以用```spring.config.import```来扩展,同样可以读取多个配置文件.

- **ApplicationContextInitializedEvent**



- **ApplicationPreparedEvent**


- **ApplicationStartedEvent**


- **ApplicationReadyEvent**


- **ApplicationFailedEvent**

