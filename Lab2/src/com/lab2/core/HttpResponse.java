package com.lab2.core;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义Http响应类
 */
public class HttpResponse {
    private Map<Integer, String> Status;
    private Map<String, String> Header;
    private int status;
    private OutputStream out;

    public HttpResponse(OutputStream out) {
        this.out = out;

        Header = new LinkedHashMap<String, String>();		//之所以采用linkedHashMap是为了保证响应头各项字段的顺序不乱 按照HTTP协议的规范回写响应报文
        Status = new HashMap<Integer, String>();

        Status.put(HttpResponseStatusEnum.正常.getCode(), HttpResponseStatusEnum.正常.getDescribe());
        Status.put(HttpResponseStatusEnum.没有找到资源.getCode(), HttpResponseStatusEnum.没有找到资源.getDescribe());
        Status.put(HttpResponseStatusEnum.内部错误.getCode(), HttpResponseStatusEnum.内部错误.getDescribe());

        Header.put("Content-Type", "text/plain;charset=utf-8");
        Header.put("Date", new Date().toString());
        status = HttpResponseStatusEnum.正常.getCode();
    }

	/**
	 * 响应方法，发送字符串
	 */
	public void write(String str) {
		Header.put("Content-Length", Integer.toString(str.length()));
		PrintStream ps = new PrintStream(out);
		printHeader(ps);
		ps.println(str);
		ps.flush();
	}
	/**
	 * 打印头信息
	 */
	private void printHeader(PrintStream ps) {
		ps.println(ServerContext.getProtocol() + " " + status + " " + Status.get(status));
		for (Map.Entry<String, String> entry : Header.entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();
			ps.println(k + ":" + v);
		}
		ps.println("");
	}

}