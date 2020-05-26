package com.lab3.kvstoreparticipant.participant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.lang.Object.*;
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
