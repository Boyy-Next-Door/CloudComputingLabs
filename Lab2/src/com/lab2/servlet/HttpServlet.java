package com.lab2.servlet;


import com.lab2.core.HttpRequest;
import com.lab2.core.HttpResponse;

import java.io.IOException;
 
public abstract class HttpServlet {
 
	public void doGet(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
		this.service(httpRequest, httpResponse);
	}
	
	public void doPost(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
		this.service(httpRequest, httpResponse);
	}
	
	public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
		if("GET".equalsIgnoreCase(httpRequest.getMethod())) {
			doGet(httpRequest, httpResponse);
		}else {
			doPost(httpRequest, httpResponse);
		}
	}
}