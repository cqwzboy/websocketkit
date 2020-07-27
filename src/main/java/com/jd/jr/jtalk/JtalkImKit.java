package com.jd.jr.jtalk;

import javax.swing.*;

/**
 * Jtalk IM 工具
 *
 * @author fuqinqin3
 * @date 2020-05-25
 * */
public class JtalkImKit extends JFrame {
    public JtalkImKit(String name){
        super(name);

        // tab
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("LongPoll", new LongPollKit().buildLongPollKitPanel());
        tabbedPane.add("WebSocket", new WebSocketKit().buildWebSocketKitPanel());
        getContentPane().add(tabbedPane);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(CommonConst.JFrameSize.WIDTH, CommonConst.JFrameSize.HEIGHT);
        setLocation(200, 100);
        setResizable(false);
        setVisible(true);
    }

    public static void main(String[] args) {
        new JtalkImKit("JtalkImKit");
    }
}
