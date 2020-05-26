package com.lab3.kvstore2pcsystem;

import com.lab3.kvstore2pcsystem.coordinator.CheckNodeAliveRunner;
import com.lab3.kvstore2pcsystem.coordinator.Const;
import com.lab3.kvstore2pcsystem.coordinator.CoordinatorServer;
import com.lab3.kvstore2pcsystem.coordinator.NodeManager;
import com.lab3.kvstore2pcsystem.utils.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import java.lang.Object.joptsimple.OptionParser;
import javax.xml.soap.Node;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@SpringBootApplication
public class Kvstore2pcsystemApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(Kvstore2pcsystemApplication.class, args);
        //System.out.println(args[0]);
        Config config=new Config();
        //设置CO的port
        config.Config_intepret(args[1]);
        Const.setPort(config.getCoPORT());
       // System.out.println(config.getCoPORT());

        //设置参与者MAP
        System.out.println("size is"+config.paIP.size());
        for(int i=0;i<config.paIP.size();++i){
            Participant p=new Participant();
            p.setIp(config.paIP.get(i));
            p.setPort(config.paPORT.get(i));
            System.out.println("ip+port is=="+p.getIp()+":"+p.getPort());
            NodeManager.participants.put(p,new Date());
        }

        //开启协作者tcp客户端
        new CoordinatorServer().run();
        //开启数据节点活跃检测线程
        new CheckNodeAliveRunner().run();
    }

}