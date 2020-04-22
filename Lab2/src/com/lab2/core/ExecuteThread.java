package com.lab2.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	private void execute() throws IOException, InterruptedException {
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();
		HttpRequset req = new HttpRequset(in);

		boolean isPost = "POST".equals(req.getMethod());
		HttpResponse res = new HttpResponse(out);

		//把消息分为静态内容和动态内容

​
		//--动态内容--
		//是否为用户数据处理接口(动态内容)
		if (ActionListen.isAction(req.getUri(), isPost)) {
			ActionListen.doAction(req, res, req.getUri(), isPost);
			return;
		}
		//--静态内容--
		//获取请求类型
		String type = req.getUri().substring(req.getUri().lastIndexOf(".") + 1);
		//设置响应头
		res.setHeader("Content-Type", ServerContext.getType(type));
		//获取静态文件
		File file = new File(ServerContext.WebRoot + req.getUri());

		if (!file.exists()) {
			//404 请求内容找不到
			res.setStatus(404);
			file = new File(ServerContext.WebRoot + "/" + ServerContext.NotFoundPage);
		} else {
			res.setStatus(200);
		}
		//响应静态内容
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		byte[] bys = new byte[(int) file.length()];
		bis.read(bys);
		res.write(bys);
		bis.close();
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
