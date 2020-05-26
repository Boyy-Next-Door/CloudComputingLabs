package com.lab3.kvstore2pcsystem.func_co.coordinator;

import com.lab3.kvstore2pcsystem.func_co.Participant;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//线程安全List: CopyOnWriteArrayList
//线程安全Map:  ConcurrentHashMap


//参与者节点管理中心
@Component
public class NodeManager {
    public static ConcurrentHashMap<Participant, Date> participants = new ConcurrentHashMap<>();

    //获取当前存活的参与者列表
    public static ArrayList<Participant> getAliveParticipantList() {
        ArrayList<Participant> aliveParticipants = new ArrayList<Participant>();
        // System.out.println("in getAliveP  participants entrySet数目为"+participants.entrySet().size());
        for (Map.Entry<Participant, Date> entry : participants.entrySet()) {
            //数据节点存活
            if (entry != null && new Date().getTime() - entry.getValue().getTime() < Const.ALIVE_CHECK_INTERVAL) {
                aliveParticipants.add(entry.getKey());
            } else if (entry == null) {
                continue;
            }
            //数据节点心跳不及时  认为已经掉线 删除
            else {
                participants.remove(entry.getKey());
            }
        }
        return aliveParticipants;
    }

    //刷新数据节点的活跃时间
    public static int refreshAliveNode(String ip, String port) {
        for (Map.Entry<Participant, Date> entry : participants.entrySet()) {
            Participant key = entry.getKey();//System.out.println("in refresh"+key.getCo_addr());

            if (key.getIp().equals(ip) && key.getPort().equals(port)) {
                entry.setValue(new Date());
                return 1;
            }
        }
        //没有找到需要刷新的节点
        return 0;
    }

    //检查数据节点活跃性
    public static void checkAlive() {
        for (Map.Entry<Participant, Date> entry : participants.entrySet()) {
            //数据节点心跳不及时  认为已经掉线 删除
            if (new Date().getTime() - entry.getValue().getTime() >= Const.ALIVE_CHECK_INTERVAL) {
                participants.remove(entry.getKey());
            }
        }
    }


}
