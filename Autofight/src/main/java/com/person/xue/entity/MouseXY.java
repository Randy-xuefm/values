package com.person.xue.entity;

import java.awt.*;

/**
 * Created by fenming.xue on 2022/3/9.
 */
public class MouseXY {
    private int x;

    private int y;

    public MouseXY(){

    }

    public MouseXY(Point point){
        this.x = (int)point.getX();
        this.y = (int)point.getY();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "MouseXY{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MouseXY)) {
            return false;
        }

        MouseXY mouseXY = (MouseXY) o;

        if (Double.compare(mouseXY.getX(), getX()) != 0) {
            return false;
        }
        return Double.compare(mouseXY.getY(), getY()) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getX());
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
