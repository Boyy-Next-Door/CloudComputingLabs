package com.lab2.test;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Test {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        System.out.println("啊啊AAA 啊");
//        Out out = new Out();
//        Class.forName("com.lab2.test.Out");

        ArrayList<String> list = new ArrayList<>();
//        for(int i=0;i<5;i++){
//            list.add(String.valueOf(i));
//            System.out.println(list.toString());
//        }
        String str ="啊啊啊";
        System.out.println(str.length());

        System.out.println(str.getBytes(Charset.forName("utf8")).length);
    }

}

class Out{
    {
        System.out.println("我是out");
    }
}
