package com.lab3.kvstore2pcsystem.func_co.coordinator;

import com.lab3.kvstore2pcsystem.func_co.Participant;
import com.lab3.kvstore2pcsystem.func_co.protocol.RespRequest;
import com.lab3.kvstore2pcsystem.func_co.protocol.RespResponse;
import com.lab3.kvstore2pcsystem.Config;
import com.lab3.kvstore2pcsystem.func_co.utils.RespParseUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


public class CoordinatorServer implements Runnable {
    private static ExecutorService cachedThreadPool;

    //    public static void main(String[] args) throws IOException {
    public void runServer() {
        // 初始化线程池
        initThreadPool();
        try {
            // 创建服务器端Socket对象
            ServerSocket ss = initSocket(Config.getInstance().getCoPORT());
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
    private final Socket socket;
    private final String clientInfo;
    private final String clientIp;
    private final int clientPort;

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
            //System.out.println("试图从tcp连接中读取一个请求报文，解析结果：" + respRequest);
            // ip和端口号在客户端打包封装请求的时候获取不到 需要服务器端建立tcp连接后才能确定
            if (respRequest != null) {
                //System.out.println("request: " + respRequest);
                // 根据request的具体类型 向controller分发任务
                switch (RespRequest.METHOD.enumOf(respRequest.getRequestType())) {
                    case SET:
                        //二阶段提交SET任务逻辑
                        doSET(respRequest);
                        break;
                    case GET:
                        //TODO 二阶段提交GET任务逻辑
                        doGET(respRequest);
                        break;
                    case DEL:
                        //TODO 二阶段提交DEL任务逻辑
                        doDEL(respRequest);
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

    private void doDEL(RespRequest respRequest) {
        //要向所有存活的参与者发送指令
        ArrayList<Participant> participants = NodeManager.getAliveParticipantList();
        //数据节点死光了  直接返回ERROR
        if (participants.size() == 0) {
            writeBackError();
        }
        //这里应该要开若干条线程  并行地通知所有参与者并等待其返回结果
        ExecutorService executor = Executors.newCachedThreadPool();
        HashMap<Participant, FutureTask<RespResponse>> futures = new HashMap();
        for (Participant p : participants) {
            FutureTask<RespResponse> futureTask = new FutureTask<RespResponse>(
                    new RequestToPrepareRunner(p, respRequest, "DEL", ""));
            futures.put(p, futureTask);
            executor.submit(futureTask);
        }
        boolean abort = false;
        try {
            //检查所有参与者的返回情况
            for (Map.Entry<Participant, FutureTask<RespResponse>> entry : futures.entrySet()) {
                RespResponse respResponse = entry.getValue().get();
                // 检查prepare-request的响应结果
                if (respResponse == null || respResponse.getResponseType() != RespResponse.DEL_OK) {
                    //DEL的响应结果不是OK或者数据节点挂了 那么Abort这次任务
                    participants.remove(entry.getKey());
                    abort = true;
                    break;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        //发送commit指令
        if (abort) {
            //DEL指令回滚
            futures = new HashMap();
            for (Participant p : participants) {
                FutureTask<RespResponse> futureTask = new FutureTask<RespResponse>(
                        new RequestToPrepareRunner(p, respRequest, "ROLLBACK", ""));
                futures.put(p, futureTask);
                executor.submit(futureTask);
            }

            boolean isDone = false;
            RespResponse standardResponse = null; //从下面任取一个有效的response作为所有节点同一的返回内容
            try {
                //检查所有参与者的返回情况
                for (Map.Entry<Participant, FutureTask<RespResponse>> entry : futures.entrySet()) {
                    RespResponse respResponse = entry.getValue().get();
                    // 检查commit的响应结果
                    if (respResponse == null || respResponse.getResponseType() != RespResponse.DEL_ROLLBACK_DONE) {
                        //数据节点挂了 或者 响应状态不是SET_ROLLBACK_DONE（这种情况理论上来说不可能出现）直接无视
                        continue;
                    } else {
                        //响应结果为DEL_ROLLBACK_DONE
                        //只要有一个节点DONE了 说明最终结果是done 要做的只是等待所有响应结果都确定下来（要么done 要么挂掉）
                        isDone = true;
                        //取最后一个有效的respResponse
                        standardResponse = respResponse;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            if (isDone) {
                //SET回滚指令提交成功 给客户端返回RESP响应DEL失败的报文
                writeBack(standardResponse);
            } else {
                //子节点全都挂掉了直接返回ERROR
                writeBackError();
            }

        } else {
            //DEL指令提交
            futures = new HashMap();
            for (Participant p : participants) {
                FutureTask<RespResponse> futureTask = new FutureTask<RespResponse>(
                        new RequestToPrepareRunner(p, respRequest, "COMMIT", ""));
                futures.put(p, futureTask);
                executor.submit(futureTask);
            }

            boolean isDone = false;
            RespResponse standardResponse = null; //从下面任取一个有效的response作为所有节点同一的返回内容
            try {
                //检查所有参与者的返回情况
                for (Map.Entry<Participant, FutureTask<RespResponse>> entry : futures.entrySet()) {
                    RespResponse respResponse = entry.getValue().get();
                    // 检查commit的响应结果
                    if (respResponse == null || respResponse.getResponseType() != RespResponse.DEL_COMMIT_DONE) {
                        //数据节点挂了 或者 响应状态不是DEL_COMMIT_DONE（这种情况理论上来说不可能出现）直接无视
                        continue;
                    } else {
                        //响应结果为SET_COMMIT_DONE
                        //只要有一个节点DONE了 说明最终结果是done 要做的只是等待所有响应结果都确定下来（要么done 要么挂掉）
                        isDone = true;
                        //取最后一个有效的respResponse
                        standardResponse = respResponse;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            if (isDone) {
                //DEL commit指令执行成功 给客户端返回RESP响应SET成功的报文
                writeBack(standardResponse);
            } else {
                //子节点全都挂掉了直接返回ERROR
                writeBackError();
            }
        }
        //释放线程池资源
        executor.shutdown();
    }

    private void doGET(RespRequest respRequest) {

        ArrayList<Participant> participants;
        ExecutorService executor = Executors.newCachedThreadPool();
        //当list里还有节点存活时
        while ((participants = NodeManager.getAliveParticipantList()).size() > 0) {
            int min = Integer.MAX_VALUE;
            Participant min_load_p = new Participant();
            for (Participant p : participants) {
                if (p.CNT < min) {
                    min_load_p = p;
                    min = p.CNT;
                }
            }
            FutureTask<RespResponse> futureTask = new FutureTask<RespResponse>(
                    new RequestToPrepareRunner(min_load_p, respRequest, "GET", ""));
            executor.submit(futureTask);
            try {
                RespResponse respResponse = futureTask.get();
                //System.out.println("doGET!!!!" + respResponse.getValues().get(0));
                if (respResponse == null || respResponse.getResponseType() != RespResponse.GET_OK) {
                    //如果有问题，就换个节点，除非所有节点都挂了
                    if (NodeManager.getAliveParticipantList().size() == 0) {
                        break;
                    }
                    continue;
                }
                //发送成功信息
                //System.out.println("in CS doGET  response is "+respResponse.getValue().get(0));
                writeBack(respResponse);
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        writeBackError();
    }

    private void doSET(RespRequest respRequest) {
        //要向所有存活的参与者发送指令
        ArrayList<Participant> participants = NodeManager.getAliveParticipantList();
        //数据节点死光了  直接返回ERROR
        if (participants.size() == 0) {
            writeBackError();
        }
        //这里应该要开若干条线程  并行地通知所有参与者并等待其返回结果
        ExecutorService executor = Executors.newCachedThreadPool();
        HashMap<Participant, FutureTask<RespResponse>> futures = new HashMap();
        for (Participant p : participants) {
            FutureTask<RespResponse> futureTask = new FutureTask<RespResponse>(
                    new RequestToPrepareRunner(p, respRequest, "SET", ""));
            futures.put(p, futureTask);
            executor.submit(futureTask);
        }
        boolean abort = false;
        try {
            //检查所有参与者的返回情况
            for (Map.Entry<Participant, FutureTask<RespResponse>> entry : futures.entrySet()) {
                RespResponse respResponse = entry.getValue().get();
                // 检查prepare-request的响应结果
                if (respResponse == null || respResponse.getResponseType() != RespResponse.SET_OK) {
                    //SET的响应结果不是OK或者数据节点挂了 那么Abort这次任务
                    System.out.println("before: " + participants.size());
                    participants.remove(entry.getKey());
                    System.out.println("after: " + participants.size());

                    abort = true;
                }
            }

        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
            System.out.println("有参与者宕机 本次请求回滚");
        }

        //发送commit指令
        if (abort) {
            //SET指令回滚
            futures = new HashMap();
            for (Participant p : participants) {
                FutureTask<RespResponse> futureTask = new FutureTask<RespResponse>(
                        new RequestToPrepareRunner(p, respRequest, "ROLLBACK", ""));
                futures.put(p, futureTask);
                executor.submit(futureTask);
            }

            boolean isDone = false;
            RespResponse standardResponse = null; //从下面任取一个有效的response作为所有节点同一的返回内容
            try {
                //检查所有参与者的返回情况
                for (Map.Entry<Participant, FutureTask<RespResponse>> entry : futures.entrySet()) {
                    RespResponse respResponse = entry.getValue().get();
                    // 检查commit的响应结果
                    if (respResponse == null || respResponse.getResponseType() != RespResponse.SET_ROLLBACK_DONE) {
                        //数据节点挂了 或者 响应状态不是SET_ROLLBACK_DONE（这种情况理论上来说不可能出现）直接无视
                        continue;
                    } else {
                        //响应结果为SET_ROLLBACK_DONE
                        //只要有一个节点DONE了 说明最终结果是done 要做的只是等待所有响应结果都确定下来（要么done 要么挂掉）
                        isDone = true;
                        //取最后一个有效的respResponse
                        standardResponse = respResponse;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            if (isDone) {
                //SET回滚指令提交成功 给客户端返回RESP响应SET失败的报文
                writeBack(standardResponse);
            } else {
                //子节点全都挂掉了直接返回ERROR
                writeBackError();
            }

        } else {
            //SET指令提交
            futures = new HashMap();
            for (Participant p : participants) {
                FutureTask<RespResponse> futureTask = new FutureTask<RespResponse>(
                        new RequestToPrepareRunner(p, respRequest, "COMMIT", ""));
                futures.put(p, futureTask);
                executor.submit(futureTask);
            }

            boolean isDone = false;
            RespResponse standardResponse = null; //从下面任取一个有效的response作为所有节点同一的返回内容
            try {
                //检查所有参与者的返回情况
                for (Map.Entry<Participant, FutureTask<RespResponse>> entry : futures.entrySet()) {
                    RespResponse respResponse = entry.getValue().get();
                    // 检查commit的响应结果
                    if (respResponse == null || respResponse.getResponseType() != RespResponse.SET_COMMIT_DONE) {
                        //数据节点挂了 或者 响应状态不是SET_COMMIT_DONE（这种情况理论上来说不可能出现）直接无视
                        continue;
                    } else {
                        //响应结果为SET_COMMIT_DONE
                        //只要有一个节点DONE了 说明最终结果是done 要做的只是等待所有响应结果都确定下来（要么done 要么挂掉）
                        isDone = true;
                        //取最后一个有效的respResponse
                        standardResponse = respResponse;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            if (isDone) {
                //SET commit指令执行成功 给客户端返回RESP响应SET成功的报文
                writeBack(standardResponse);
            } else {
                //子节点全都挂掉了直接返回ERROR
                writeBackError();
            }
        }
        //释放线程池资源
        executor.shutdown();
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
            return new String(bt, StandardCharsets.UTF_8);
        }
        return "";
    }

    // 向客户端回写响应
    private void writeBack(RespResponse respResponse) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            String s = RespParseUtil.parseResponse(respResponse);
            System.out.println("response to " + clientInfo + ": " + s);
            PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
            pWriter.println(s);
            pWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 向客户端回写响应
    private void writeBackError() {
        try {
            OutputStream outputStream = socket.getOutputStream();
            String s = "-ERROR\r\n";
            System.out.println("response to " + clientInfo + ": " + s);
            PrintWriter pWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
            pWriter.println(s);
            pWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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