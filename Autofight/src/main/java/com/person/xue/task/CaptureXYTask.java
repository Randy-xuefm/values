package com.person.xue.task;

import com.person.xue.entity.MouseXY;

import java.awt.*;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Created by fenming.xue on 2022/3/9.
 */
public class CaptureXYTask implements Callable<MouseXY>,ITask {

    private Logger logger = Logger.getLogger("CaptureXYTask");

    @Override
    public MouseXY call() throws Exception {
        MouseXY old = new MouseXY();
        int count = 0;
        logger.info("持续10s不动，即可抓取坐标");
        long startTime = System.currentTimeMillis();
        while (true){
            long nowTime = System.currentTimeMillis();
            if(nowTime -startTime > 30*1000){
                logger.info("30s未能成功抓取鼠标位置，请重试！！！！");
                logger.info("请保持鼠标不动！！！！");
                return null;
            }
            Thread.sleep(1000);
            if(count == 10){
                break;
            }
            MouseXY now = new MouseXY(MouseInfo.getPointerInfo().getLocation());
            if(now.equals(old)){
                count++;
            }else{
                old = now;
                count = 0;
            }

        }
        return old;
    }
}
