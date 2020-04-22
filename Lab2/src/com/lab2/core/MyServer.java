package com.lab2.core;

import com.lab2.model.Const;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyServer {
	private static ExecutorService cachedThreadPool;

	public static void main(String[] args) throws IOException {
		// 初始化线程池
		initThreadPool();
		// 创建服务器端Socket对象
		ServerSocket ss = initSocket(Const.SERVER_PORT);
		// 监听客户端连接 返回一个Socket对象
		while (true) {
			// 接收到一个TCP连接请求 丢给服务端业务线程处理
			Socket accept = ss.accept();
			System.out.println(accept.getInetAddress().getHostAddress() + " " + accept.getPort() + "创建tcp连接");
			// 给线程池分发一个新任务
			cachedThreadPool.execute(new ExecuteThread(accept));
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
}

