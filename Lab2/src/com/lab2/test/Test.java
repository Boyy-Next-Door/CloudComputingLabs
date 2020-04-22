package com.lab2.test;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Test {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        System.out.println("啊啊AAA 啊");
//        Out out = new Out();
        Class.forName("com.lab2.test.Out");
    }

}

class Out{
    {
        System.out.println("我是out");
    }
}
