package com.lab2.core;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.xml.transform.Source;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerContext {
    private static Map<String, String> Types;
    private static String Protocol;
    private static int Port;
    private static int MaxThread;
    private static String WebRoot;
    private static String NotFoundPage;

    static{
        //静态代码块进行初始化
        System.out.println("initializing ServerContext...");
        init();
    }

    //从xml中读入配置信息
    public static void init() {
        Types = new HashMap<String, String>();
        try {
            SAXReader reader = new SAXReader();
//            System.out.println(System.getProperty("user.dir"));
            Document doc = reader.read("src/com/lab2/config/web.xml");

            Element root = doc.getRootElement();

            Element service = root.element("service");
            Element connector = service.element("connector");
            Protocol = connector.attributeValue("protocol");
            Port = Integer.parseInt(connector.attributeValue("port"));
            MaxThread = Integer.parseInt(connector.attributeValue("max-thread"));
            WebRoot = service.elementText("webroot");
            NotFoundPage = service.elementText("not-found-page");

            @SuppressWarnings("unchecked")
            List<Element> typeMappings = root.element("type-mappings").elements("type-mapping");
            for (Element e : typeMappings) {
                Types.put(e.attributeValue("ext"), e.attributeValue("type"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> getTypes() {
        return Types;
    }

    public static void setTypes(Map<String, String> types) {
        Types = types;
    }

    public static String getProtocol() {
        return Protocol;
    }

    public static void setProtocol(String protocol) {
        Protocol = protocol;
    }

    public static int getPort() {
        return Port;
    }

    public static void setPort(int port) {
        Port = port;
    }

    public static int getMaxThread() {
        return MaxThread;
    }

    public static void setMaxThread(int maxThread) {
        MaxThread = maxThread;
    }

    public static String getWebRoot() {
        return WebRoot;
    }

    public static void setWebRoot(String webRoot) {
        WebRoot = webRoot;
    }

    public static String getNotFoundPage() {
        return NotFoundPage;
    }

    public static void setNotFoundPage(String notFoundPage) {
        NotFoundPage = notFoundPage;
    }

}
