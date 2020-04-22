package com.lab2.core;

import com.lab2.Exceptions.MethodNotSupportException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServletManager {
    private static Map<String, String> GetMethod;
    private static Map<String, String> PostMethod;

    static {
        loadServlets();
    }

    private static void loadServlets() {
        GetMethod = new HashMap<>();
        PostMethod = new HashMap<String, String>();
        try {
            SAXReader reader = new SAXReader();
            File file = new File("src/com/lab2/config/web.xml");
            Document doc = reader.read(file);
            Element root = doc.getRootElement();    //这是server对象

            Element servlets = root.element("servlets");
            List<Element> list = servlets.elements("servlet");
            for (Element e : list) {
                Element servletUrl = e.element("servlet-url");
                String urlStr = servletUrl.getText();
                if (urlStr.startsWith("/")) {
                    urlStr = urlStr.substring(1);
                }
                if ("POST".equals(servletUrl.attributeValue("method").toUpperCase())) {
                    PostMethod.put(urlStr, e.elementText("servlet-class"));
                } else {
                    GetMethod.put(urlStr, e.elementText("servlet-class"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doAction(HttpRequest req, HttpResponse res, String methodurl, boolean isPost) throws IOException, MethodNotSupportException {
        //消除url最开始的/
        if (methodurl.startsWith("/")) {
            methodurl = methodurl.substring(1);
        }
        try {
            String target = null;
            if (isPost) {
                //获取对应POST请求的url的servlet-class类名
                target = PostMethod.get(methodurl);
            } else {
                //获取对应GET请求的url的servlet-class类名
                target = GetMethod.get(methodurl);
            }
            if(target==null){
                throw new MethodNotSupportException();
            }
            Class<? extends Object> cls = Class.forName(target);
            Object obj = cls.newInstance();
            //获取servlet中的方法列表
            Method[] methods = cls.getDeclaredMethods();
            //根据请求类型 确定反射执行的方法
            String invokeMethod=isPost?"doPost":"doGet";
            for (Method method : methods) {
                //这里规定bridge为servlet执行的入口方法
                if (invokeMethod.equals(method.getName())) {
                    method.invoke(obj, req, res);
                    break;
                }
            }
        } catch (Exception e) {
            if(e instanceof  MethodNotSupportException){
                throw new MethodNotSupportException();
            }else{
                e.printStackTrace();
                res.write(e.toString());
            }
        }
        //返回响应
        res.write("ok");
    }

    //根据url判断是否为servlet请求 暂时约定servlet以.do结尾
    public static boolean isAction(String uri, boolean isPost) {
        return uri.endsWith(".do");
    }
}
