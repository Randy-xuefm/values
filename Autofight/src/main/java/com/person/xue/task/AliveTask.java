package com.person.xue.task;

import com.person.xue.entity.MouseXY;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

/**
 * Created by fenming.xue on 2022/3/9.
 */
public class AliveTask implements Runnable{

    private Logger logger = Logger.getLogger("AliveTask");

    private MouseXY anchor;
    private MouseXY magic;

    public AliveTask(MouseXY anchor){
        this.magic.setX(anchor.getX());
        this.magic.setY(anchor.getY()+4);
    }


    @Override
    public void run() {
        if(this.anchor == null){
            logger.info("未获取锚点，任务退出！！！");
            return;
        }

        try {
            Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                logger.info("未知错误！！！！");
                e.printStackTrace();
                return;
            }

            alive(robot);

            magic(robot);

        } catch (HeadlessException e) {
            logger.info("任务失败！");
            logger.info(e.getMessage());
        }
        logger.info("任务完成！");

    }

    private void alive(Robot robot){
        while (true){
            MouseXY now = new MouseXY(MouseInfo.getPointerInfo().getLocation());
            if(now.equals(this.anchor)){
                logger.info("鼠标已经移动到锚点！");
                break;

            }else{
                robot.mouseMove(this.anchor.getX(),this.anchor.getY());
            }
        }

        for (int i = 0; i < 10; i++) {
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_TAB);


            robot.keyRelease(KeyEvent.VK_TAB );
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.delay(500);

            robot.mousePress(KeyEvent.BUTTON2_MASK);
            robot.mouseRelease(KeyEvent.BUTTON2_MASK);
            robot.mousePress(KeyEvent.BUTTON2_MASK);
            robot.mouseRelease(KeyEvent.BUTTON2_MASK);
        }
        logger.info("信春哥得永生！！！");
    }

    private void magic(Robot robot){

        while (true){
            MouseXY now = new MouseXY(MouseInfo.getPointerInfo().getLocation());
            if(now.equals(this.magic)){
                logger.info("鼠标已经移动到锚点！");
                break;

            }else{
                robot.mouseMove(this.anchor.getX(),this.anchor.getY());
            }
        }

        for (int i = 0; i < 10; i++) {
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_TAB);


            robot.keyRelease(KeyEvent.VK_TAB );
            robot.keyRelease(KeyEvent.VK_CONTROL);
            robot.delay(500);

            robot.mousePress(KeyEvent.BUTTON2_MASK);
            robot.mouseRelease(KeyEvent.BUTTON2_MASK);
            robot.mousePress(KeyEvent.BUTTON2_MASK);
            robot.mouseRelease(KeyEvent.BUTTON2_MASK);
        }

        this.logger.info("脉动脉动，活力无限！！！！");
    }
}
