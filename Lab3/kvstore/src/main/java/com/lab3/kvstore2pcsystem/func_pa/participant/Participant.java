package com.lab3.kvstore2pcsystem.func_pa.participant;


import com.lab3.kvstore2pcsystem.func_pa.protocol.RespRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Stack;

public class Participant {
    private String co_addr; //协调者地址
    private String co_port; //协调者接口ip
    private String ip;//参与者IP
    private String port;//参与者端口
    private HashMap<String, RespRequest> map;        //内存数据库    <key，RespRequest>
    private HashMap<String, RespRequest> buffer;     //缓冲区    <transactionNo，RespRequest>
    private Stack<String> log;                       //日志

    public Participant() {
        this.map = new HashMap<>();
        this.buffer = new HashMap<>();
        this.log = new Stack<>();
    }

    public String getCo_port() {
        return co_port;
    }

    public void setCo_port(String co_port) {
        this.co_port = co_port;
    }

    public String getCo_addr() {
        return co_addr;
    }

    public void setCo_addr(String co_addr) {
        this.co_addr = co_addr;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public HashMap<String, RespRequest> getMap() {
        return map;
    }

    public void setMap(HashMap<String, RespRequest> map) {
        this.map = map;
    }

    public Stack<String> getLog() {
        return log;
    }

    public void setLog(Stack<String> log) {
        this.log = log;
    }

    public void log(String info) {
        this.getLog().push(new Date().toString() + "\t" + info);
    }

    public HashMap<String, RespRequest> getBuffer() {
        return buffer;
    }

    public void setBuffer(HashMap<String, RespRequest> buffer) {
        this.buffer = buffer;
    }


    @Override
    public String toString() {
        return "Participant{" +
                "co_addr='" + co_addr + '\'' +
                ", ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", map=" + map +
                ", buffer=" + buffer +
                ", log=" + log +
                '}';
    }

    /**
     * 解析协调者发给参与者的RESP消息，并调用各方法：添加KV、删除KV、返回KV、回滚（即删除栈顶job）
     *
     * @param msg
     * @return
     */
    private Job parse(String msg) {
        return null;
    }

    /**
     * 处理job，job内含任务信息（K V 还有method）
     *
     * @param job
     * @return 1 if succeed(prepared), 0 if fail(not prepared)
     */
    private int prepared(Job job) {
        return 0;
    }

    /**
     * 将参与者的状态发给协调者（PREPARED/NOT PREPARED/DONE）
     *
     * @param code
     * @return
     */
    private int send_p2c(String code) {
        return 0;
    }

    /**
     * 从协调者收到消息（任务/状态码），交给parse处理
     *
     * @return
     */
    private String recv_from_co() {
        return "";
    }

}
