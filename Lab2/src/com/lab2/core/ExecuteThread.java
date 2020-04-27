package com.lab2.core;

import com.lab2.Exceptions.MethodNotSupportException;
import com.lab2.Exceptions.MethodUnimplementedException;
import com.lab2.Exceptions.ParsingParameterException;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    private void execute() throws IOException, ClassNotFoundException {
        int count = 0;
        while (!socket.isClosed()) {
//            System.out.println("count:" + count);
            count++;
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            HttpRequest req = null;
            HttpResponse res = new HttpResponse(out);

            try {
                req = new HttpRequest(in);
                //检查request是否指定长连接
                if(req.getParameter().containsKey("Connection")){
                    String connection = req.getParameter().get("Connection");

                }
            } catch (ParsingParameterException e) {
                //TODO 返回内部错误响应
                res.setStatus(HttpResponseStatusEnum.ERROR.getCode());
                res.write("method not supported.");
            } catch (MethodUnimplementedException e) {
                //请求方法未实现
                res.setStatus(HttpResponseStatusEnum.NOT_IMPLEMENTED.getCode());
                res.write(new File(ServerContext.getWebRoot() + "/" + ServerContext.getMethodNotImplemented()));
                return;
            }

            //POST请求
            boolean isPost = "POST".equals(req.getMethod());

            //把消息分为静态内容和动态内容
            //--动态内容--
            //是否为用户数据处理接口(动态内容)
            if (ServletManager.isAction(req.getUri(), isPost)) {
                try {
                    ServletManager.doAction(req, res, req.getUri(), isPost);
                } catch (MethodNotSupportException e) {
                    //返回响应
                    res.setStatus(HttpResponseStatusEnum.ERROR.getCode());
                    res.write("method not supported.");
                }
                return;
            }

            //--静态内容--
            //获取请求类型
            String type = req.getUri().substring(req.getUri().lastIndexOf(".") + 1);
            //设置响应头属性
            res.addHeaderAttribute("Content-Type", ServerContext.getTypes().get(type));
            //获取静态文件
            File file = new File(ServerContext.getWebRoot() + req.getUri());

            if (!file.exists()) {
                //404 请求内容找不到
                res.setStatus(404);
                file = new File(ServerContext.getWebRoot() + "/" + ServerContext.getNotFoundPage());
            } else {
                res.setStatus(200);
            }
            //响应静态内容
            res.write(file);
        }
    }


    @Override
    public void run() {
        try {
            execute();
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
