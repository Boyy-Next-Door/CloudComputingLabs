package com.lab3.kvstore2pcsystem.coordinator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Const {

    private static int port;

    public static int getPort() {
        return port;
    }

    @Value("${tcpServer.port}")
    public void setPort(int port) {
        Const.port = port;
    }
}
