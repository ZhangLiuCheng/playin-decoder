package com.tech.playinsdk.connect;

import com.tech.playinsdk.util.PILog;
import com.tech.playinsdk.util.Tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * 建立连接.
 */
public abstract class PlaySocket extends Thread {

    public abstract void onOpen();
    public abstract void onMessage(String msg);
    public abstract void onMessage(byte[] buf);
    public abstract void onError(Exception ex);

    private String ip;
    private int port;
    private DataProcess dataProcess;

    private Socket socket;
    private InputStream istream;
    private OutputStream ostream;

    private Thread mWriteThread;

    public PlaySocket(String ip, int port) {
        this.ip = ip;
        this.port = port;
        dataProcess = new DataProcess();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void connect() {
        start();
    }

    public void disConnect() {
        PILog.v("断开连接");
        interrupt();
        try {
            if (null != socket) {
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Tool.closeStream(istream, ostream);
    }

    public void sendText(String text) {
        dataProcess.sendText(text);
    }

    /**
     * 发送触摸事件.
     * @param control
     */
    public void sendControl(String control) {
        dataProcess.sendControl(control);
    }

    /**
     * @param packType  1 控制  2 二进制流
     * @param streamType  0 触摸  1 H264  2 音频  6 android
     * @param control
     */
    public void sendStream(byte packType, byte streamType, String control) {
        dataProcess.sendStream(packType, streamType, control);
    }

    @Override
    public void run() {
        PILog.v("开始连接socket ip: " + ip + " port:" + port);
        try {
            socket = new Socket(ip, port);
            socket.setSoTimeout(0);
            socket.setReceiveBufferSize(100 * 1024);
            socket.setSendBufferSize(1024);
            socket.setTcpNoDelay(true);
            PILog.v("socket 连接成功");
            onOpen();
            istream = socket.getInputStream();
            ostream = socket.getOutputStream();
            // 启动发送数据线程
            mWriteThread = new WriteThread();
            mWriteThread.start();
            // 读取数据流(循环阻塞)
            readData();
        } catch (SocketException ex) {
            PILog.e("连接或读取异常： " + ex);
            onError(ex);
        } catch (IOException ex) {
            onError(ex);
        }
    }

    private void readData() {
        try {
            dataProcess.receiveData(istream, new DataProcess.ReceiveCallback() {
                @Override
                public void message(String message) {
                    onMessage(message);
                }

                @Override
                public void buffer(byte[] buf) {
                    onMessage(buf);
                }
            });
        } catch (IOException ex) {
            onError(ex);
        }
    }

    public class WriteThread extends Thread {
        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    byte[] sendBuf = dataProcess.getSendQueue().take();
                    ostream.write(sendBuf);
                    ostream.flush();
                }
            } catch (InterruptedException e) {
                // 主动断开连接，不做任何处理
            } catch (IOException e) {
                PILog.e("发送异常： " + e);
                onError(e);
            }
        }
    }
}
