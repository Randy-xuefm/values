package com.person.xue;

import com.person.xue.entity.MouseXY;
import com.person.xue.task.AliveTask;
import com.person.xue.task.CaptureXYTask;
import com.person.xue.task.FightTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by fenming.xue on 2022/3/8.
 */
public class Main {

    private static Logger logger = Logger.getLogger("Main");

    public static void main(String[] args) throws Exception {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        CaptureXYTask captureXYTask = new CaptureXYTask();

        MouseXY anchor = captureXYTask.call();

        if(anchor == null){
            return;
        }

        logger.info(anchor.toString());
        executorService.scheduleAtFixedRate(new FightTask(anchor),120,120, TimeUnit.SECONDS);
        logger.info("月光与你同在！！！");

        MouseXY alive = captureXYTask.call();
        if(alive != null){
            logger.info(alive.toString());
            executorService.scheduleAtFixedRate(new AliveTask(anchor),30,30, TimeUnit.SECONDS);
            logger.info("大地母亲庇佑我！！！！");
        }

        Thread.currentThread().join();
    }


}
