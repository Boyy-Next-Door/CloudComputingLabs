package com.lab2.core;
 
import com.lab2.Exceptions.ParsingParameterException;
import com.lab2.utils.LoggerUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 	自定义Http请求类  通过socket的输入流读取http的请求报文内容并封装
 */
public class HttpRequest {
	private String uri;
	private String method;
	private String protocol;

	private Map<String,String> Header;
	private Map<String,String> QueryString;
	private Map<String,String> Form;
	private Map<String,String> Parameter;

	public HttpRequest(InputStream in) throws IOException, ParsingParameterException {
		//初始化参数集合
		Header=new HashMap<>();
		QueryString=new HashMap<>();
		Form=new HashMap<>();
		Parameter=new HashMap<>();
		//从tcp数据段读取HTTP请求的各项参数
		analysis(in);
	}

	private void analysis(InputStream in) throws IOException, ParsingParameterException {
		BufferedReader buffer = new BufferedReader(new InputStreamReader(in));

		//读取请求行
		String line = buffer.readLine();
		if (line != null && line.length() > 0) {
			//TODO 将url进行解码 这样可以保证GET请求中的中文参数无乱码
			String output = java.net.URLDecoder.decode(line, "UTF-8");
			LoggerUtil.info(output);
			//HTTP协议请求行 请求方法 【空格】 URI 【空格】 协议版本 【回车符\r】【换行符\n】
			String[] temp = output.split("\\s");
			this.method = temp[0];
			this.uri = temp[1];
			//uri中携带了参数 应该是GET、PUT之类的请求
			if (this.uri.indexOf("?") != -1) {
				//获取uri中的参数部分
				String str = this.uri.substring(this.uri.indexOf("?") + 1);
				//读取解析并存储参数
				parseParameter(str, false);
				//抛弃uri中参数部分 仅保留请求接口路径
				this.uri = this.uri.substring(0, this.uri.indexOf("?"));
			}
			//如果uri以/结尾 多数情况下属于未指定静态资源 默认返回index.html  这个也可以优化成类似JAVAEE中的web.xml配置默认资源
			if (this.uri.endsWith("/")) {
				this.uri += "index.html";
			}
			//记录请求协议版本
			this.protocol = temp[2];
		}

		//读取请求头  HTTP协议请求头格式:
		// 头部字段名1：值 【回车符\r】【换行符\n】
		// 头部字段名2：值 【回车符\r】【换行符\n】
		// 头部字段名3：值 【回车符\r】【换行符\n】
		//【回车符\r】【换行符\n】    这里有个空行 表示请求头结束
		// 这里开始是请求体
		/*
			使用buffer.readLine时读取到末尾并不会自动退出，会一直阻塞，因为浏览器端发送消息后或一直
			等待服务端的响应直到请求超时，所以在等待期间输入流会一直打开，而服务端使用buffer.readLine
			读取时，读取到最后时因为浏览器端输入流没关闭，所以会一直阻塞，造成了两端在相互等待的情况，
			形成死锁。所以在读取到空行时就break出while循环。这里保存的Content-Length的值是为了根据
			这个值的大小去读取post请求的内容。
		*/
		String l = null;
		int len = 0;
		//循环读取请求头
		while ((l = buffer.readLine()) != null) {
			if ("".equals(l)) {
				break;
			}
			if(l.indexOf(":")==-1){
				throw new ParsingParameterException("请求头参数格式异常");
			}
			String k = l.substring(0, l.indexOf(":")).trim();
			String v = l.substring(l.indexOf(":") + 1).trim();
			if(k.length()==0 || v.length()==0){
				throw new ParsingParameterException("请求头参数格式异常");
			}
			//像header区中存储请求头参数
			this.Header.put(k, v);
			//如果该行记录的content-length属性  解析成整数
			if (l.indexOf("Content-Length") != -1) {
				len = Integer.parseInt(l.substring(l.indexOf(":") + 1).trim());
			}
		}
		//如果是POST请求 需要按照请求体长度len读取请求体
		if (method != null && method.toUpperCase().equals("POST")) {
			char[] bys = new char[len];
			buffer.read(bys);
			String paraStr = new String(bys);
			parseParameter(paraStr, true);
		}
	}

	/**
	 * 对请求字符串解析成参数对象
	 * @param str
	 * @param isPost
	 */
	private void parseParameter(String str, boolean isPost) throws ParsingParameterException {
//		System.out.println(str);
		//为空串
		if(str==null||str.length()==0){
			return;
		}
		//将参数字符串按照&分割
		String[] arr = str.split("&");
		for (String s : arr) {
			//参数按照键值对形式传入  这里按照=分割
			String[] temp = s.split("=");		//这里可以考虑优化 忽略掉处于引号内的等号
			//判断键值对分割
			if(temp.length!=2){
				throw new ParsingParameterException("uri参数格式异常");
			}
			//如果是post方式请求，把参数放在form表单参数中
			if (isPost) {
				this.Form.put(temp[0], temp[1]);
			}
			//非post方式请求，参数放在queryString中
			else {
				//在queryString中存一份  该参域暂时不知道是什么含义
				this.QueryString.put(temp[0], temp[1]);
			}
			//最后在parameter中存一份
			this.Parameter.put(temp[0], temp[1]);
		}
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public Map<String, String> getHeader() {
		return Header;
	}

	public void setHeader(Map<String, String> header) {
		Header = header;
	}

	public Map<String, String> getQueryString() {
		return QueryString;
	}

	public void setQueryString(Map<String, String> queryString) {
		QueryString = queryString;
	}

	public Map<String, String> getForm() {
		return Form;
	}

	public void setForm(Map<String, String> form) {
		Form = form;
	}

	public Map<String, String> getParameter() {
		return Parameter;
	}

	public void setParameter(Map<String, String> parameter) {
		Parameter = parameter;
	}
}