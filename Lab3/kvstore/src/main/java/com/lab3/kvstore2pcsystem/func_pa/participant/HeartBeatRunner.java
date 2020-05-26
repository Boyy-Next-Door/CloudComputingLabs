package com.lab3.kvstore2pcsystem.func_pa.participant;


import com.lab3.kvstore2pcsystem.func_pa.utils.HttpClientUtils;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

//心跳线程 在springboot启动类中创建并执行 com.lab3.kvstoreparticipant.KvstoreParticipantApplication
public class HeartBeatRunner implements Runnable {
    @Override
    public void run() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {

                HashMap<String, String> stringStringHashMap = new HashMap<>();
                stringStringHashMap.put("ip", Database.participant.getIp());
                stringStringHashMap.put("port", Database.participant.getPort());

                String s = HttpClientUtils.doPost("http://" + Database.participant.getCo_addr() + ":" + "8080" + "/coordinator/heartbeat", stringStringHashMap);
                System.out.println("心跳结果:" + s);
            }
        }, 1000, Const.HEART_BEAT_INTERVAL);   //1秒后开始第一次心跳 之后每隔5秒一次
    }
}
