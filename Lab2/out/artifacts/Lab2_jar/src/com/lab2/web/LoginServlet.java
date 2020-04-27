package com.lab2.web;

import com.lab2.core.BaseServlet;
import com.lab2.core.HttpRequest;
import com.lab2.core.HttpResponse;
import com.lab2.utils.LoggerUtil;

//这是一个Servlet实现类
public class LoginServlet implements BaseServlet {
    @Override
    public void doPost(HttpRequest req, HttpResponse res) {
        LoggerUtil.LOGGER.info("哈哈哈哈哈哈哈我是LoginServlet的post请求\n");
    }

    @Override
    public void doGet(HttpRequest req, HttpResponse res) {
        LoggerUtil.LOGGER.info("奥里给我是LoginServlet的get请求");
    }
}
