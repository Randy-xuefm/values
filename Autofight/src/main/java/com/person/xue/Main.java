package com.person.xue;

import com.person.xue.entity.MouseXY;
import com.person.xue.task.CaptureXYTask;
import com.person.xue.task.FightTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Created by fenming.xue on 2022/3/8.
 */
public class Main {

    private static Logger logger = Logger.getLogger("Main");

    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Condition STOP = LOCK.newCondition();

    public static void main(String[] args) throws Exception {

        CaptureXYTask captureXYTask = new CaptureXYTask();

        MouseXY anchor = captureXYTask.call();

        if(anchor == null){
            return;
        }

        logger.info(anchor.toString());

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new FightTask(anchor),0,120, TimeUnit.SECONDS);


        Thread.currentThread().join();
    }


}
