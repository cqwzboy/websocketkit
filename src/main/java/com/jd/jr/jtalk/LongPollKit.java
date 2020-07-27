package com.jd.jr.jtalk;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Date;

/**
 * 长轮询工具
 *
 * @author fuqinqin3
 * @date 2020-05-25
 * */
public class LongPollKit {
    private CloseableHttpClient httpClient;

    private JTextField receiveMsgUrlTextField;                  // 收消息URL栏
    private JTextField sendMsgUrlTextField;                     // 发消息URL栏
    private JTextField connectionStatusTextField;               // 连接状态
    private JTextArea logTextArea;                              // 日志面板
    private JTextArea messageTextArea;                          // 信息面板
    private JTextArea requestTextArea;                          // 请求数据报文

    public JPanel buildLongPollKitPanel(){
        JPanel longPollPanel = new JPanel();
        longPollPanel.setLayout(new BorderLayout());
        longPollPanel.add(buildNorthTab(), BorderLayout.NORTH);
        longPollPanel.add(buildCenter(), BorderLayout.CENTER);
        longPollPanel.add(buildEast(), BorderLayout.EAST);
        longPollPanel.add(buildSouth(), BorderLayout.SOUTH);
        return longPollPanel;
    }

    /**
     * 构建top栏
     * */
    private JPanel buildNorthTab(){
        JPanel northPanel = new JPanel();
        GridLayout northLayout = new GridLayout(2, 1);
        northPanel.setLayout(northLayout);
        northPanel.setBorder(new BevelBorder(BevelBorder.RAISED));

        FlowLayout layout = new FlowLayout();
        layout.setAlignment(FlowLayout.LEFT);

        // 收消息
        JPanel receiveMsgPanel = new JPanel();
        receiveMsgPanel.setSize(JtalkImKit.WIDTH, JtalkImKit.HEIGHT/5);
        receiveMsgPanel.setLayout(layout);
        receiveMsgPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);

        JLabel receiveLabel = new JLabel("收消息URL：");
        receiveMsgPanel.add(receiveLabel);

        receiveMsgUrlTextField = new JTextField(50);
        receiveMsgUrlTextField.setEditable(true);
        receiveMsgPanel.add(receiveMsgUrlTextField);
        northPanel.add(receiveMsgPanel);

        // 发消息
        JPanel sendMsgPanel = new JPanel();
        sendMsgPanel.setSize(JtalkImKit.WIDTH, JtalkImKit.HEIGHT/5);
        sendMsgPanel.setLayout(layout);
        sendMsgPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);

        JLabel sendLabel = new JLabel("发消息URL：");
        sendMsgPanel.add(sendLabel);

        sendMsgUrlTextField = new JTextField(50);
        sendMsgUrlTextField.setEditable(true);
        sendMsgPanel.add(sendMsgUrlTextField);

        JButton connect = new JButton("连接");
        connect.addActionListener(connectListener);
        JButton disconnect = new JButton("断开连接");
        disconnect.addActionListener(disconnectListener);
        sendMsgPanel.add(connect);
        sendMsgPanel.add(disconnect);

        connectionStatusTextField = new JTextField(8);
        connectionStatusTextField.setEditable(false);
        sendMsgPanel.add(connectionStatusTextField);
        northPanel.add(sendMsgPanel);

        return northPanel;
    }

    private JPanel buildCenter(){
        JPanel centerPanel = new JPanel();
        BorderLayout layout = new BorderLayout();   // 样式
        centerPanel.setLayout(layout);
        centerPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);

        // request area
        JPanel requestPanel = new JPanel();
        requestTextArea = new JTextArea(32, 38);
        requestPanel.add(new JScrollPane(requestTextArea));
        requestPanel.setBorder(new TitledBorder("请求报文"));
        requestPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);
        centerPanel.add(requestPanel, BorderLayout.WEST);

        // sendButton
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
        centerPanel.add(buttonPanel, BorderLayout.EAST);

        return centerPanel;
    }

    private JPanel buildEast(){
        JPanel eastPanel = new JPanel();
        eastPanel.setBorder(new TitledBorder("信息面板"));
        GridLayout layout = new GridLayout(1, 1);
        eastPanel.setLayout(layout);
        eastPanel.setBackground(CommonConst.Color.BACKGROUND_COLOR);

        messageTextArea = new JTextArea(30, 70);
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

    /**
     * 日志输出
     * */
    private void log(String methodName, String msg){
        logTextArea.append(DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss.SSS")
                +" "+WebSocketKit.class.getName()+"-"+methodName+" ["+Thread.currentThread().getName()+"] "+" "+msg+"\n");
    }

    /**
     * 建立连接
     * */
    private void conn(){
        httpClient = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(receiveMsgUrlTextField.getText());
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            log("conn", "响应状态为:" + httpResponse.getStatusLine());
            if (httpEntity != null) {
                log("conn", "响应内容长度为:" + httpEntity.getContentLength());
                log("conn", "响应内容为:" + EntityUtils.toString(httpEntity));
                responseDataLog(EntityUtils.toString(httpEntity));
            }
            if(httpClient != null){
                conn();
            }
        } catch (IOException e) {
            e.printStackTrace();
            log("conn", "长轮询获取消息异常:" + e.getMessage());
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
                if(httpResponse != null){
                    httpResponse.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                log("conn", "长轮询获取消息异常:" + e.getMessage());
            }
        }
    }

    /**************************************************************** 事件开始 **********************************************************************/
    public ActionListener connectListener = e -> {
        if(httpClient!=null){
            log("connectListener", "长轮询连接已建立，不可重复创建...");
            return;
        }

        // url
        String url = receiveMsgUrlTextField.getText();
        if(StringUtils.isBlank(url)){
            log("connectListener", "接收消息URL为空...");
            return;
        }

        // 建立连接
        new Thread(()->{
            conn();
        },"LongPollConnThread").start();
        while (true){
            if(httpClient != null){
                connectionStatusTextField.setText("连接激活状态");
                connectionStatusTextField.setBackground(CommonConst.Color.CONN_ACTIVE);
                log("connectListener", "建立连接成功...");
                messageTextArea.append("["+DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss.SSS")+"]\t建立连接成功...\n");
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                log("connectListener", ex.getMessage());
                break;
            }
        }
    };
    public ActionListener disconnectListener = e -> {
        if(httpClient == null){
            log("disconnectListener", "尚未建立连接...");
            return;
        }
        try {
            httpClient.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            log("disconnectListener", "关闭连接失败...");
            return;
        }
        httpClient = null;
        connectionStatusTextField.setText("连接已关闭");
        connectionStatusTextField.setBackground(CommonConst.Color.CONN_CLOSE);
        log("disconnectListener", "关闭连接成功...");
        messageTextArea.append("["+DateUtil.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss.SSS")+"]\t连接已关闭\n");
    };
    private ActionListener sendMsgListener = e -> {
        try{
            String sendMsgUrl = sendMsgUrlTextField.getText();
            if(StringUtils.isBlank(sendMsgUrl)){
                log("sendMsgListener", "发送消息URL为空...");
                return;
            }

            String requestDate = requestTextArea.getText();
            if(StringUtils.isBlank(requestDate)){
                log("sendMsgListener", "请求报文为空...");
                return;
            }
            requestDataLog(requestDate);

            // 发送消息
            HttpPost httpPost = new HttpPost(sendMsgUrl);
            httpPost.setHeader("Content-Type", "application/json;charset=utf8");
            StringEntity stringEntity = new StringEntity(requestDate, "UTF-8");
            httpPost.setEntity(stringEntity);

            CloseableHttpResponse httpResponse = null;
            try {
                httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                log("sendMsgListener", "响应状态为:" + httpResponse.getStatusLine());
                if (httpEntity != null) {
                    log("sendMsgListener", "响应内容长度为:" + httpEntity.getContentLength());
                    log("sendMsgListener", "响应内容为:" + EntityUtils.toString(httpEntity));
                    responseDataLog(EntityUtils.toString(httpEntity));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                log("sendMsgListener", "长轮询获取消息异常:" + ex.getMessage());
            } finally {
                try {
                    if(httpResponse != null){
                        httpResponse.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    log("sendMsgListener", "长轮询获取消息异常:" + ex.getMessage());
                }
            }
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
    /**************************************************************** 事件结束 **********************************************************************/

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

    /**
     * 发送消息
     * */
    private void sendMsg(){

    }
}
