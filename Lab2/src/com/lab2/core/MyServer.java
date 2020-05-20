package com.lab2.core;


import com.lab2.utils.LoggerUtil;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyServer {
    private static ExecutorService cachedThreadPool;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //读取命令行参数
        HashMap<String, String> params = parseParameter(args);
        //参数解析错误
        if(params==null){
			LoggerUtil.LOGGER.severe("\t\t"+"parameter error"+"\n");
        	System.exit(1);
		}
        String ip = "";		//这个参数不知道用在哪里 默认创建的socket 对应的就是本机ip 使用127.0.0.1可以访问到
        int port = 0;
        int numberThread = 0;

        //校验参数
        if (params.containsKey("ip")) {
            ip = params.get("ip");
        }
        if (params.containsKey("port")) {
            port = Integer.valueOf(params.get("port"));
        }
        if (params.containsKey("number-thread")) {
            numberThread = Integer.valueOf(params.get("number-thread"));
        }

        // 初始化线程池
		//如果main函数未指定number-thread参数 那就按照默认构造
        initThreadPool(numberThread);
        //加载ServerContext类
        Class.forName("com.lab2.core.ServerContext");
        // 创建服务器端Socket对象
		//如果main函数未指定port参数 那就按照serverContext中的默认配置
        ServerSocket ss = initSocket(port != 0 ? port : ServerContext.getPort());
        // 监听客户端连接 返回一个Socket对象
        while (true) {
            // 接收到一个TCP连接请求 丢给服务端业务线程处理
            Socket accept = ss.accept();
            LoggerUtil.LOGGER.info("\t\t"+accept.getInetAddress().getHostAddress() + " "
                    + accept.getPort() + "创建tcp连接"+"\n");
            // 给线程池分发一个新任务
            cachedThreadPool.execute(new ExecuteThread(accept));
        }
    }

    //解析main函数参数  参数错误返回null 参数为空返回大小为0的map
    private static HashMap<String, String> parseParameter(String[] args) {
		HashMap<String, String> params = new HashMap<>();
		//首先校验参数是否成对 即args大小是否为偶数
		if (args.length % 2 == 1) {
			return null;
		}

        //提取参数
        for (int i = 0; i < (args.length -1); i += 2) {
            if (!args[i].startsWith("--")) {
                //参数格式不正确
                return null;
            }
            params.put(args[i].substring(2), args[i + 1]);
        }

        //返回参数map
        return params;
    }

    // 初始化ServerSocket对象
    private static ServerSocket initSocket(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        LoggerUtil.LOGGER.info("\t\t"+"服务器开启: " + serverSocket.getInetAddress().getHostAddress() + ": " + serverSocket.getLocalPort()+"\n");
        return serverSocket;
    }

    // 初始化线程池
    private static void initThreadPool(int numberThread) {
    	if(numberThread>0){
			cachedThreadPool = Executors.newScheduledThreadPool(numberThread);
		}else{
    		//自适应大小线程池
			cachedThreadPool = Executors.newCachedThreadPool();
		}
    }
}


