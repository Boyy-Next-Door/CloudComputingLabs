package com.lab2.utils;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UTF8StringLengthUtil {
    public static int length(String str){
        str.getBytes(Charset.forName("utf8"));
//        for(int i=0;;i++){
//
//        }
//        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
//        Matcher m = p.matcher(str);
//        if (m.find()) {
//            return true;
//        }
        return 1;
    }
}
