package com.lab3.kvstore2pcsystem.coordinator;

import com.lab3.kvstore2pcsystem.protocol.RespRequest;
import com.lab3.kvstore2pcsystem.protocol.RespResponse;
import com.lab3.kvstore2pcsystem.utils.RespParseUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CoordinatorServer implements Runnable {
    private static ExecutorService cachedThreadPool;

    //    public static void main(String[] args) throws IOException {
    public void runServer() {
        // 初始化线程池
        initThreadPool();
        try {
            // 创建服务器端Socket对象
            ServerSocket ss = initSocket(Const.getPort());
            // 监听客户端连接 返回一个Socket对象
            while (true) {
                // 接收到一个TCP连接请求 丢给服务端业务线程处理
                Socket accept = ss.accept();
                System.out.println(accept.getInetAddress().getHostAddress() + " " + accept.getPort() + "创建tcp连接");
                // 给线程池分发一个新任务
                cachedThreadPool.execute(new ExecuteThread(accept));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 初始化ServerSocket对象
    private static ServerSocket initSocket(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out
                .println("本机: " + serverSocket.getInetAddress().getHostAddress() + ": " + serverSocket.getLocalPort());
        return serverSocket;
    }

    // 初始化线程池
    private static void initThreadPool() {
        cachedThreadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        runServer();
    }
}

/* 线程池中的具体任务线程类 */
class ExecuteThread implements Runnable {
    private Socket socket;
    private String clientInfo;
    private String clientIp;
    private int clientPort;

    // 构造传入socket实例
    public ExecuteThread(Socket s) {
        this.socket = s;
        this.clientInfo = socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort();
        this.clientIp = socket.getInetAddress().getHostAddress();
        this.clientPort = socket.getPort();

    }

    private String getTimeStamp() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        return simpleDateFormat.format(date);
    }

    // 请求处理逻辑
    private void execute() throws IOException, InterruptedException {
        //自旋监听请求
        while (!socket.isClosed()) {
            // 解析请求
            String readData = readData();
            if ("soket is closed".equals(readData)) {
                //说明readData时已经发现socket断开了
                break;
            }
            RespRequest respRequest = RespParseUtil.parseRequest(readData);
            System.out.println("试图从tcp连接中读取一个请求报文，解析结果："+respRequest);
            // ip和端口号在客户端打包封装请求的时候获取不到 需要服务器端建立tcp连接后才能确定
            if (respRequest != null) {
                System.out.println("request: " + respRequest);
                // 根据request的具体类型 向controller分发任务
                switch (RespRequest.METHOD.enumOf(respRequest.getRequestType())) {
                    case SET:
                        //TODO 二阶段提交SET任务逻辑
                        break;
                    case GET:
                        //TODO 二阶段提交GET任务逻辑
                        break;
                    case DEL:
                        //TODO 二阶段提交DEL任务逻辑
                        break;
                    default: //说明RESP报文请求类型不支持或错误 不做任何响应
                }
            }
            //respRequest为null 说明从TCP的输入流读到的RESP报文不正确 不做任何响应
        }

        //socket已经关闭 用户关闭了客户端  还没有登陆的用户也会有socket连接 这样无法区分是否为已登录用户
        //而且socket断掉之后这里之前的循环一直阻塞 没办法正确跳出来  这个以后再想办
        //就采用退出接口的方法吧 （强制关闭还得得靠服务器端的socket连接状态监测）
//		ServerManager.getSM().userLogout(clientInfo);

    }

    private String readData() throws IOException, InterruptedException {
        // 封装数据源
        InputStream inputStream = socket.getInputStream();
        int count = 0;
        while (count == 0) {
            Thread.sleep(200);
            //检查socket是否断开
            if (socket.isClosed()) {
                return "soket is closed";
            }
            count = inputStream.available();
        }
        if (count != 0) {
            byte[] bt = new byte[count];
            int readCount = 0;
            while (readCount < count) {
                readCount += inputStream.read(bt, readCount, count - readCount);
            }
            return new String(bt, "UTF-8");
        }
        return "";
    }

    // 向客户端回写响应
    private void writeBack(RespResponse respResponse) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        String s = RespParseUtil.parseResponse(respResponse);
        System.out.println("response to " + clientInfo + ": " + s);
        PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
        pWriter.println(s);
        pWriter.flush();
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

