package com.example.enableconfigurationproperties;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

/**
 * Created by fenming.xue on 2021/9/7.
 */
public class Validtion implements SmartApplicationListener {


    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

        if(applicationEvent instanceof ApplicationStartedEvent){
            Config config = ((ApplicationStartedEvent) applicationEvent).getApplicationContext().getBean(Config.class);
            System.out.println("config.name="+config.getName());


            Config1 config1 = ((ApplicationStartedEvent) applicationEvent).getApplicationContext().getBean(Config1.class);
            System.out.println("config1.name="+config1.getName());

            Config4Propery config4Propery = ((ApplicationStartedEvent) applicationEvent).getApplicationContext().getBean(Config4Propery.class);
            System.out.println("config4Propery.name="+config4Propery.getName());

            String name = ((ApplicationStartedEvent)applicationEvent).getApplicationContext().getEnvironment().getProperty("spring.example.name");
            System.out.println("ApplicationStartedEvent");
            System.out.println("name in environment," + name);

            String config1Name = ((ApplicationEnvironmentPreparedEvent)applicationEvent).getEnvironment().getProperty("spring.config1.name");
            System.out.println("config1 in environment," + config1Name);

            String config2 = ((ApplicationEnvironmentPreparedEvent)applicationEvent).getEnvironment().getProperty("spring.config2.name");
            System.out.println("config2 in environment," + config2);
        }else if(applicationEvent instanceof ApplicationEnvironmentPreparedEvent){
            String name = ((ApplicationEnvironmentPreparedEvent)applicationEvent).getEnvironment().getProperty("spring.example.name");
            System.out.println("ApplicationEnvironmentPreparedEvent");
            System.out.println("name in environment," + name);

            String config1 = ((ApplicationEnvironmentPreparedEvent)applicationEvent).getEnvironment().getProperty("spring.config1.name");
            System.out.println("config1 in environment," + config1);

            String config2 = ((ApplicationEnvironmentPreparedEvent)applicationEvent).getEnvironment().getProperty("spring.config2.name");
            System.out.println("config2 in environment," + config2);
        }



    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> aClass) {
        return ApplicationStartedEvent.class.isAssignableFrom(aClass) || ApplicationEnvironmentPreparedEvent.class.isAssignableFrom(aClass);
    }
}
