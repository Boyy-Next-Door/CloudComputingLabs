package com.lab3.kvstoreparticipant;

import com.lab3.kvstoreparticipant.participant.Const;
import com.lab3.kvstoreparticipant.participant.Database;
import com.lab3.kvstoreparticipant.participant.HeartBeatRunner;
import com.lab3.kvstoreparticipant.utils.Config;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

import javax.sound.sampled.Port;
import java.io.IOException;

@SpringBootApplication
public class KvstoreParticipantApplication implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    public static void main(String[] args) throws IOException {

        Config config = Config.getInstance();
        config.Config_intepret(args[1]);

        //开启springboot
        SpringApplication.run(KvstoreParticipantApplication.class, args);

        //初始化Database的信息
        Database.participant.setCo_addr(config.getCoIP());
        System.out.println("COIP is " + config.getCoIP());
        Database.participant.setCo_port(String.valueOf(config.getCoPORT()));
        System.out.println("CO PORT is==" + config.getCoPORT());
        Database.participant.setIp(config.getParIP());

        Database.participant.setPort(String.valueOf(config.getParPORT()));
        System.out.println("my PORT is==" + Database.participant.getPort());
        new HeartBeatRunner().run();
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        //TODO  把这里换成participant.conf 里的端口号
        factory.setPort(Config.getInstance().getParPORT());
    }

}
