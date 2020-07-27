package com.jd.jr.jtalk;

import java.awt.*;

/**
 * 常量
 *
 * @author fuqinqin3
 * @date 2020-05-25
 * */
public interface CommonConst {
    /**
     * @author fuqinqin3
     * */
    interface JFrameSize {
        Integer WIDTH = 1500;
        Integer HEIGHT = 840;
    }

    /**
     * @author fuqinqin3
     * */
    interface Color{
        // 背景色
        java.awt.Color BACKGROUND_COLOR = java.awt.Color.decode("#CFCFCF");
        // 不可编辑背景色
        java.awt.Color UNEDITABLE_COLOR = java.awt.Color.decode("#E8E8E8");
        // 连接处于激活状态
        java.awt.Color CONN_ACTIVE = java.awt.Color.decode("#98FB98");
        // 连接处于关闭状态
        java.awt.Color CONN_CLOSE = java.awt.Color.decode("#F08080");
    }
}
