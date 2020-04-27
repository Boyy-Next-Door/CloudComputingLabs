package com.lab2.web;

import com.lab2.core.*;
import com.lab2.utils.LoggerUtil;

import java.io.IOException;
import java.util.Map;

//这是一个Servlet实现类
public class PostShowServlet implements BaseServlet {
    @Override
    public void doPost(HttpRequest req, HttpResponse res)  {
        //获取参数
        Map<String, String> parameter = req.getParameter();
        //构造回写html文本
        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n" +
                "<title>POST method</title>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "</head>\n" +
                "<body>\n");
        for(Map.Entry entry:parameter.entrySet()){
//            sb.append(entry.getKey() + ": "+entry.getValue()+"<br>\n");
            sb.append(entry.getKey() + ": "+entry.getValue()+"\n");
        }
        sb.append("</body>\n</html>");

        res.write(sb.toString());
    }

    @Override
    public void doGet(HttpRequest req, HttpResponse res) {
        LoggerUtil.LOGGER.info("奥里给我是LoginServlet的get请求");
    }
}
