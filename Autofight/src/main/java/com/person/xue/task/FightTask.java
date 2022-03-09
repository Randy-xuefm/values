package com.person.xue.task;

import com.person.xue.entity.MouseXY;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

/**
 * Created by fenming.xue on 2022/3/9.
 */
public class FightTask implements Runnable,ITask{

    private Logger logger = Logger.getLogger("FightTask");

    private MouseXY anchor;

    public FightTask(MouseXY xy){
        this.anchor = xy;
    }

    @Override
    public void run() {
        try {
            if(anchor == null){
                logger.info("未获取到锚点，任务退出！！！");
                return;
            }

            Robot robot = null;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                logger.info("未知错误！！！！");
                e.printStackTrace();
                return;
            }

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

                robot.mousePress(KeyEvent.BUTTON1_MASK);
                robot.mouseRelease(KeyEvent.BUTTON1_MASK);
                robot.mousePress(KeyEvent.BUTTON1_MASK);
                robot.mouseRelease(KeyEvent.BUTTON1_MASK);
            }
        } catch (HeadlessException e) {
            logger.info("任务失败！");
            logger.info(e.getMessage());
        }

        logger.info("任务完成！");

    }
}
