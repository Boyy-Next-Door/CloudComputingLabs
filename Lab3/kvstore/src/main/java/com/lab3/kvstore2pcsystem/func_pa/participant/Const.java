package com.lab3.kvstore2pcsystem.func_pa.participant;

import org.springframework.stereotype.Component;

@Component
public class Const {
    private static int port;

    //  每隔5秒调用一次心跳接口
    public static int HEART_BEAT_INTERVAL = 5 * 1000;

    //java -jar --xxx.config xxxx.jar

    public static int getPort() {
        return port;
    }

    //@Value("${server.port}")
    public static void setPort(int port) {
        Const.port = port;
    }

}
