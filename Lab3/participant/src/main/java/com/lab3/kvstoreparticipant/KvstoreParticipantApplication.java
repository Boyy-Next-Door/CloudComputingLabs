package com.lab3.kvstoreparticipant;

import com.lab3.kvstoreparticipant.participant.Const;
import com.lab3.kvstoreparticipant.participant.Database;
import com.lab3.kvstoreparticipant.participant.HeartBeatRunner;
import com.lab3.kvstoreparticipant.utils.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class KvstoreParticipantApplication {

    public static void main(String[] args) throws IOException {

        SpringApplication.run(KvstoreParticipantApplication.class, args);
        Config config=new Config();
        config.Config_intepret(args[0]);
        //初始化Database的信息
        Database.participant.setCo_addr(config.getCoIP());
        System.out.println("COIP is "+config.getCoIP());
        Database.participant.setCo_port(String.valueOf(config.getCoPORT()));
        System.out.println("CO PORT is "+config.getCoPORT());
        Database.participant.setIp(config.getParIP());
        System.out.println("my IP is "+config.getParIP());
        Database.participant.setPort(String.valueOf(config.getParPORT()));
        new HeartBeatRunner().run();
    }

}
