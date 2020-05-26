package com.lab3.kvstoreparticipant;

import com.lab3.kvstoreparticipant.participant.Const;
import com.lab3.kvstoreparticipant.participant.Database;
import com.lab3.kvstoreparticipant.participant.HeartBeatRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KvstoreParticipantApplication {

    public static void main(String[] args) {
        SpringApplication.run(KvstoreParticipantApplication.class, args);

        //初始化Database的信息
        Database.participant.setCo_addr("localhost");
        Database.participant.setCo_port("8080");
        Database.participant.setIp("localhost");
        Database.participant.setPort(String.valueOf(Const.getPort()));
        new HeartBeatRunner().run();
    }

}
