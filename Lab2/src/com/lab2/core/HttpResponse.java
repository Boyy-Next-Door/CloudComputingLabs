package com.lab2.core;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

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

        Status.put(HttpResponseStatusEnum.OK.getCode(), HttpResponseStatusEnum.OK.getDescribe());
        Status.put(HttpResponseStatusEnum.NOT_FOUND.getCode(), HttpResponseStatusEnum.NOT_FOUND.getDescribe());
        Status.put(HttpResponseStatusEnum.ERROR.getCode(), HttpResponseStatusEnum.ERROR.getDescribe());

        Header.put("Content-Type", "text/plain;charset=utf-8");
        Header.put("Date", new Date().toString());
        Header.put("Server","oneFlower&threeGrass' web server");
        status = HttpResponseStatusEnum.OK.getCode();
    }

	/**
	 * 响应方法，发送字符串
	 */
	public void write(File file) throws IOException {
		Header.put("Content-Length", String.valueOf(file.length()));
		PrintStream ps = new PrintStream(out);
		//先回写响应头
		printHeader(ps);
		//封装数据源
		BufferedInputStream bis = new BufferedInputStream(
				new FileInputStream(file));

		//一次复制一个字节数组
		byte[] by = new byte[1024];
		int len = 0;
		while((len = bis.read(by)) != -1) {
			out.write(Arrays.copyOf(by,len));
		}

		out.flush();
		//释放资源
		bis.close();
		out.close();
	}

	/**
	 * 响应方法，发送字符串
	 */
	public void write(String message) {
		Header.put("Content-Length", message!=null?String.valueOf(message.getBytes(Charset.forName("utf8")).length):"0");
		PrintStream ps = new PrintStream(out);
		//回写响应头
		printHeader(ps);
		//回写message
		ps.println(message);
		ps.close();
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

	public Map<Integer, String> getStatus() {
		return Status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public void setStatus(Map<Integer, String> status) {
		Status = status;
	}

	public Map<String, String> getHeader() {
		return Header;
	}

	public void setHeader(Map<String, String> header) {
		Header = header;
	}

	public void addHeaderAttribute(String k,String v){
		Header.put(k,v);
	}
}