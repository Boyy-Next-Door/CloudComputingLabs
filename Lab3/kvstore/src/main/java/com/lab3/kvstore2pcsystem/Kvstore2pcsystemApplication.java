package com.lab3.kvstore2pcsystem;

import com.lab3.kvstore2pcsystem.coordinator.CoordinatorServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Kvstore2pcsystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(Kvstore2pcsystemApplication.class, args);
        //开启协作者tcp客户端
        new CoordinatorServer().run();
    }

}
