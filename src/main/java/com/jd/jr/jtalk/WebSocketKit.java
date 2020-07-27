package com.jd.jr.jtalk;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket工具类
 *
 *
 * @author fuqinqin
 * @date 2020-05-17
 * */
public class WebSocketKit {
    private WebSocketClient webSocketClient;

    private JTextField urlTextField;                            // URL栏
    private JTextArea logTextArea;                              // 日志面板
    private JTextField heartbeatIntervalTextField;              // 心跳间隔
    private TextArea heartbeatTextArea;                         // 心跳报文
    private JTextField headerTextField;                         // header
    private JTextArea messageTextArea;                          // 信息面板
    private JTextArea requestTextArea;                          // 请求数据报文
    private JTextField connectionStatusTextField;               // 连接状态

    public JPanel buildWebSocketKitPanel(){
        JPanel webSocketPanel = new JPanel();
        webSocketPanel.setLayout(new BorderLayout());
        webSocketPanel.add(buildNorthTab(), BorderLayout.NORTH);
        webSocketPanel.add(buildCenter(), BorderLayout.CENTER);
        webSocketPanel.add(buildEast(), BorderLayout.EAST);
        webSocketPanel.add(buildSouth(), BorderLayout.SOUTH);
        return webSocketPanel;
    }

    /**
     * 构建top栏
     * */
    private JPanel buildNorthTab(){
        JPanel northPanel = new JPanel();
        northPanel.setSize(CommonConst.JFrameSize.WIDTH, CommonConst.JFrameSize.HEIGHT/5);
        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);
        northPanel.setLayout(layout);
        northPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
        northPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);

        JLabel jLabel = new JLabel("URL：");
        northPanel.add(jLabel);

        urlTextField = new JTextField(50);
        urlTextField.setEditable(true);
        northPanel.add(urlTextField);

        JButton connect = new JButton("连接");
        connect.addActionListener(connectListener);
        JButton disconnect = new JButton("断开连接");
        disconnect.addActionListener(disconnectListener);
        northPanel.add(connect);
        northPanel.add(disconnect);

        connectionStatusTextField = new JTextField(8);
        connectionStatusTextField.setEditable(false);
        northPanel.add(connectionStatusTextField);

        return northPanel;
    }

    private JPanel buildCenter(){
        JPanel centerPanel = new JPanel();
        BorderLayout layout = new BorderLayout();   // 样式
        centerPanel.setLayout(layout);
        centerPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);

        // headers
        JPanel top = new JPanel();
        top.setLayout(new FlowLayout(FlowLayout.LEFT));
        top.setBorder(new TitledBorder("Headers"));
        top.setBackground(CommonConst.Color.BACKGROUND_COLOR);
        headerTextField = new JTextField(72);
        top.add(headerTextField);
        centerPanel.add(top, BorderLayout.NORTH);

        // request area
        JPanel requestPanel = new JPanel();
        requestTextArea = new JTextArea(30, 38);
        requestPanel.add(new JScrollPane(requestTextArea));
        requestPanel.setBorder(new TitledBorder("请求报文"));
        requestPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);
        centerPanel.add(requestPanel, BorderLayout.CENTER);

        // heartbeat and sendButton
        JPanel heartbeatAndSendButtonPanel = new JPanel();
        heartbeatAndSendButtonPanel.setLayout(new BorderLayout());
        heartbeatAndSendButtonPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);
        JPanel heartbeatPanel = new JPanel(new BorderLayout());
        heartbeatPanel.setBorder(new TitledBorder("心跳报文"));
        heartbeatPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);
        JPanel intervalPanel = new JPanel();
        intervalPanel.add(new JLabel("Interval(毫秒)："));
        intervalPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);
        heartbeatIntervalTextField = new JTextField(20);
        intervalPanel.add(heartbeatIntervalTextField);
        heartbeatPanel.add(intervalPanel, BorderLayout.NORTH);
        heartbeatTextArea = new TextArea(20, 50);
        JScrollPane heartbeatScrollPane = new JScrollPane(heartbeatTextArea);
        heartbeatScrollPane.setBackground(CommonConst.Color.BACKGROUND_COLOR);
        heartbeatPanel.add(heartbeatScrollPane, BorderLayout.CENTER);
        heartbeatAndSendButtonPanel.add(heartbeatPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        FlowLayout buttonLayout = new FlowLayout();
        buttonLayout.setAlignment(FlowLayout.RIGHT);
        buttonPanel.setLayout(buttonLayout);
        buttonPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);
        JButton clearLogButton = new JButton("清空日志");
        clearLogButton.addActionListener(clearLogListener);
        buttonPanel.add(clearLogButton);
        JButton clearMessagePanelButton = new JButton("清空信息面板");
        clearMessagePanelButton.addActionListener(clearMessagePanelListener);
        buttonPanel.add(clearMessagePanelButton);
        JButton button = new JButton("发送");
        button.addActionListener(sendMsgListener);
        buttonPanel.add(button);
        heartbeatAndSendButtonPanel.add(buttonPanel, BorderLayout.SOUTH);
        centerPanel.add(heartbeatAndSendButtonPanel, BorderLayout.EAST);

        return centerPanel;
    }

    private JPanel buildEast(){
        JPanel eastPanel = new JPanel();
        eastPanel.setBorder(new TitledBorder("信息面板"));
        GridLayout layout = new GridLayout(1, 1);
        eastPanel.setLayout(layout);
        eastPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);

        messageTextArea = new JTextArea(30, 60);
        messageTextArea.setBackground(CommonConst.Color.UNEDITABLE_COLOR);
        messageTextArea.setEditable(false);     // 不可被编辑
        eastPanel.add(new JScrollPane(messageTextArea));

        return eastPanel;
    }

    private JPanel buildSouth(){
        JPanel southPanel = new JPanel();
        southPanel.setBorder(new TitledBorder("日志"));
        southPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);

        logTextArea = new JTextArea(10, 134);
        logTextArea.setBackground(CommonConst.Color.UNEDITABLE_COLOR);                     // 背景色
        logTextArea.setEditable(false);                                 // 日志文本域不可编辑
        JScrollPane scrollPane = new JScrollPane(logTextArea);
        southPanel.add(scrollPane);

        return southPanel;
    }

    /*************************************************************** event begin ******************************************************************/
    private ActionListener connectListener = e -> {
        if(webSocketClient!=null && webSocketClient.isOpen()){
            log("connectListener", "WebSocket长连接已建立，不可重复创建...");
            return;
        }

        // url
        String url = urlTextField.getText();
        if(StringUtils.isBlank(url)){
            log("connectListener", "URL为空...");
            return;
        }

        // headers
        String headerJson = headerTextField.getText();
        Map<String, String> headers = new HashMap<>();
        if(StringUtils.isBlank(headerJson)){
            log("connectListener", "headers为空");
        }else{
            JSONObject headerJsonObject;
            try{
                headerJsonObject = JSON.parseObject(headerJson);
            }catch (Exception ex){
                ex.printStackTrace();
                log("connectListener", "解析header失败，"+ex.getMessage());
                return;
            }
            for (String key : headerJsonObject.keySet()) {
                headers.put(key, (String) headerJsonObject.get(key));
            }
        }

        // 建立连接
        try{
            webSocketClient = new WebSocketClient(new URI(url), headers){
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    log("connectListener", "建立连接成功");
                    messageTextArea.append("["+DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss.SSS")+"]\t建立连接成功...\n");
                    connectionStatusTextField.setText("连接激活状态");
                    connectionStatusTextField.setBackground(Color.decode("#98FB98"));

                    // 心跳报文区域，间隔时间，headers，URL变成不可编辑
                    heartbeatTextArea.setEditable(false);
                    heartbeatTextArea.setBackground(CommonConst.Color.UNEDITABLE_COLOR);
                    heartbeatIntervalTextField.setEditable(false);
                    heartbeatIntervalTextField.setBackground(CommonConst.Color.UNEDITABLE_COLOR);
                    headerTextField.setEditable(false);
                    headerTextField.setBackground(CommonConst.Color.UNEDITABLE_COLOR);
                    urlTextField.setEditable(false);
                    urlTextField.setBackground(CommonConst.Color.UNEDITABLE_COLOR);
                }

                @Override
                public void onMessage(String s) {
                    log("connectListener", "收到及时消息："+s);
                    responseDataLog(s);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    log("disconnectListener", "连接已关闭...");
                    messageTextArea.append("["+now()+"]\t连接已关闭\n");
                    connectionStatusTextField.setText("连接已关闭");
                    connectionStatusTextField.setBackground(Color.decode("#F08080"));

                    // 心跳报文区域，间隔时间，headers，URL变成可编辑状态
                    heartbeatTextArea.setEditable(true);
                    heartbeatTextArea.setBackground(Color.WHITE);
                    heartbeatIntervalTextField.setEditable(true);
                    heartbeatIntervalTextField.setBackground(Color.WHITE);
                    headerTextField.setEditable(true);
                    headerTextField.setBackground(Color.WHITE);
                    urlTextField.setEditable(true);
                    urlTextField.setBackground(Color.WHITE);
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    log("connectListener", "#77345 发生异常，"+e.getMessage());
                }
            };
        }catch (URISyntaxException ex){
            ex.printStackTrace();
            log("connectListener", "#88345 发生异常，"+ex.getMessage());
            return;
        }
        webSocketClient.connect();
        while (!webSocketClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)){
            log("connectListener", "正在连接...");
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                log("connectListener", "#77345 发生异常，"+ex.getMessage());
            }
        }

        // 心跳信息
        String heartbeatIntervalString = heartbeatIntervalTextField.getText();
        String heartbeatDataString = heartbeatTextArea.getText();
        if(StringUtils.isNotBlank(heartbeatIntervalString) && StringUtils.isNotBlank(heartbeatDataString)){
            log("connectListener", "心跳信息不为空，heartbeatIntervalString="+heartbeatIntervalString+", heartbeatDataString="+heartbeatDataString);
            new Thread(() -> {
                while (true) {
                    if(webSocketClient.isClosed()){
                        break;
                    }
                    webSocketClient.send(heartbeatDataString);
                    requestDataLog("["+now()+"] 发送心跳 ["+heartbeatDataString+"]");
                    log("connectListener", "发送心跳 ["+heartbeatDataString+"]");
                    try {
                        Thread.sleep(Integer.parseInt(heartbeatIntervalString));
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        log("connectListener", "触发心跳逻辑异常，"+ex.getMessage());
                        break;
                    }
                }
            }).start();
        }else{
            log("connectListener", "心跳信息为空，heartbeatIntervalString="+heartbeatIntervalString+", heartbeatDataString="+heartbeatDataString);
        }
    };

    private ActionListener disconnectListener = e -> {
        if(webSocketClient == null){
            log("disconnectListener", "尚未建立连接...");
            return;
        }
        webSocketClient.close();
    };

    private ActionListener sendMsgListener = e -> {
        try{
            String requestDate = requestTextArea.getText();
            if(StringUtils.isBlank(requestDate)){
                log("sendMsgListener", "请求报文为空...");
                return;
            }
            webSocketClient.send(requestDate);
            log("sendMsgListener", "发送消息："+requestDate);
            requestDataLog(requestDate);
        }catch (Exception ex){
            ex.printStackTrace();
            log("sendMsgListener", "发送消息异常，"+ex.getMessage());
        }
    };

    private ActionListener clearLogListener = e -> {
        logTextArea.setText(null);
        log("clearLogListener", "清空日志面板成功");
    };

    private ActionListener clearMessagePanelListener = e -> {
        messageTextArea.setText(null);
        log("clearResponseDataListener", "清空消息面板成功");
    };
    /*************************************************************** event end ******************************************************************/

    /**
     * 日志输出
     * */
    private void log(String methodName, String msg){
        logTextArea.append(now()+" "+WebSocketKit.class.getName()+"-"+methodName+" ["+Thread.currentThread().getName()+"] "+" "+msg+"\n");
    }

    /**
     * 响应数据输出
     * */
    private void responseDataLog(String msg){
        messageTextArea.append("【响应数据】 "+msg+"\n");
    }

    /**
     * 请求数据输出
     * */
    private void requestDataLog(String msg){
        messageTextArea.append("【请求数据】 "+msg+"\n");
    }

    private String now(){
        return DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss.SSS");
    }
}
