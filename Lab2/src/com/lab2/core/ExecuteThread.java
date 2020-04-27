package com.lab2.core;

import com.lab2.Exceptions.AliveConnectionClosedException;
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
        boolean isKeepAlive = true;
        //只对长连接自旋
        while (!socket.isClosed() && isKeepAlive) {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            HttpRequest req = null;
            HttpResponse res = new HttpResponse(out);

            try {
                //这里会抛很多种异常 分别catch
                req = new HttpRequest(in);
                //这里面会阻塞在inputStream.readLine()上读请求行 如果浏览器刷新会立刻关闭http连接 从而断开socket
                //而如果断开，readLine()会读到一个空串 于是在analyze()里面会去读请求头
                //这就是之前浏览器访问图片 一次成功 第二次刷新后台就抛一个空指针并且创建新的socket获取的原因
                //于是需要在analyze里进行修改  当请求行处读到一个空行 就说明没有正确读入请求行
                //这就是上述的长连接被客户端提前断开的情况

                //检查request是否指定长连接
                if(req.getHeader().containsKey("Connection")){
                    String connection = req.getHeader().get("Connection");
                    //指定长连接
                    if("KEEP-ALIVE".equals(connection.toUpperCase())){
                        res.getHeader().put("Connection","keep-alive");
                        isKeepAlive=true;
                    }else{
                        //短连接
                        res.getHeader().put("Connection","closed");
                        isKeepAlive=false;
                    }
                }else{
                    //没有指定长连接  默认短连接
                    isKeepAlive=false;
                }
            } catch (ParsingParameterException e) {
                //TODO 返回内部错误响应
                res.setStatus(HttpResponseStatusEnum.ERROR.getCode());
                res.write("请求头参数格式错误");
                return;
            } catch (MethodUnimplementedException e) {
                //请求方法未实现
                res.setStatus(HttpResponseStatusEnum.NOT_IMPLEMENTED.getCode());
                res.write(new File(ServerContext.getWebRoot() + "/" + ServerContext.getMethodNotImplemented()));
                return;
            }catch (AliveConnectionClosedException e) {
                //长连接被中断 不再响应客户端 直接结束当前线程
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
