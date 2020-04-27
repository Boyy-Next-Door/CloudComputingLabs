package com.lab2.core;

public interface BaseServlet {
	void doPost(HttpRequest req, HttpResponse res);
	void doGet(HttpRequest req, HttpResponse res);
}