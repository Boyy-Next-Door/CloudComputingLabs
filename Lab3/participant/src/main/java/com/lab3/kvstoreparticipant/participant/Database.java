package com.lab3.kvstoreparticipant.participant;

import com.lab3.kvstoreparticipant.protocol.RespRequest;
import com.lab3.kvstoreparticipant.protocol.RespResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class Database {
    public static Participant participant=new Participant();

    public static RespResponse SET(RespRequest respRequest){
        //先打印日志
        //日志记录values
        String string = respRequest.getValues().toString();
        participant.log(respRequest.getTransactionNo()+":\tSET <"+respRequest.getKeys().get(0)+","+string+">");

        //写入缓冲区
        participant.getBuffer().put(respRequest.getTransactionNo(),respRequest);

        //封装response
        RespResponse respResponse = new RespResponse();
        //设置响应状态
        respResponse.setResponseType(RespResponse.SET_OK);
        //记录所查询的key
        respResponse.setKeys(respRequest.getKeys());
        //设置试图set的value
        respResponse.setValues(respRequest.getValues());

        return respResponse;
    }

    public static RespResponse SET_COMMIT(RespRequest respRequest){
        //先打印日志
        //日志记录提交
        participant.log(respRequest.getTransactionNo()+":\tSET committed");

        //将缓冲区内容修改到数据库 并清除缓冲区该条记录
        RespRequest remove = participant.getBuffer().remove(respRequest.getTransactionNo());

        participant.getMap().put(remove.getKeys().get(0),respRequest);

        //封装response
        RespResponse respResponse = new RespResponse();
        //设置响应状态
        respResponse.setResponseType(RespResponse.SET_COMMIT_DONE);
        //记录SET的key
        respResponse.setKeys(remove.getKeys());
        //设置set的value
        respResponse.setValues(remove.getValues());

        return respResponse;
    }

    public static RespResponse SET_ROLLBACK(RespRequest respRequest){
        //先打印日志
        //日志记录提交
        participant.log(respRequest.getTransactionNo()+":\tSET rollback");

        //将缓冲区内容移除
        RespRequest remove = participant.getBuffer().remove(respRequest.getTransactionNo());

        //封装response
        RespResponse respResponse = new RespResponse();
        //设置响应状态
        respResponse.setResponseType(RespResponse.SET_ROLLBACK_DONE);
        //记录原本要SET的key
        respResponse.setKeys(remove.getKeys());
        //设置原本要set的value
        respResponse.setValues(remove.getValues());

        return respResponse;
    }

    public static RespResponse GET(RespRequest respRequest){
        //GET不需要日志
        RespRequest record = participant.getMap().get(respRequest.getKeys().get(0));
        //封装response
        RespResponse respResponse = new RespResponse();
        //设置响应状态
        respResponse.setResponseType(RespResponse.GET_OK);
        //记录所查询的key
        respResponse.setKeys(respRequest.getKeys());

        //没有找到指定key
        if(record==null){
            //values是一个空的集合
            //System.out.println("in Database GET value is null");
            respResponse.setValues(new ArrayList<String>());
        }else{
           //找到了key对应的value
            respResponse.setValues(record.getValues());
//            System.out.println("in Database GET2 is not null=="+respResponse.getValues().get(0));
        }

        return respResponse;
    }

    public static RespResponse DEL(RespRequest respRequest){
        //先打印日志
        //日志记录values
        participant.log(respRequest.getTransactionNo()+":\tDEL "+respRequest.getKeys().toString());
        //写入缓冲区
        participant.getBuffer().put(respRequest.getTransactionNo(),respRequest);
        //封装response
        RespResponse respResponse = new RespResponse();
        //设置响应状态
        respResponse.setResponseType(RespResponse.DEL_OK);
        //记录试图删除的keys
        respResponse.setKeys(respRequest.getKeys());

        return respResponse;
    }


    public static RespResponse DEL_COMMIT(RespRequest respRequest){
        //先打印日志
        //日志记录提交
        participant.log(respRequest.getTransactionNo()+":\tDEL committed");

        //将缓冲区内容修改到数据库 并清除缓冲区该条记录
        RespRequest remove = participant.getBuffer().remove(respRequest.getTransactionNo());

        //循环删除所有key 并且记录有效删除数量
        int delCount=0;
        for(String key:remove.getKeys()){
            RespRequest remove1 = participant.getMap().remove(key);
            if(remove1!=null){
                //如果key存在 成功移除了
                delCount++;
            }
        }

        //封装response
        RespResponse respResponse = new RespResponse();
        //设置响应状态
        respResponse.setResponseType(RespResponse.DEL_COMMIT_DONE);
        //记录DEL的key
        respResponse.setKeys(remove.getKeys());
        //设置成功DEL的数量
        respResponse.setKeysRemoved(delCount);
        return respResponse;
    }

    public static RespResponse DEL_ROLLBACK(RespRequest respRequest){
        //先打印日志
        //日志记录提交
        String string = respRequest.getValues().toString();
        participant.log(respRequest.getTransactionNo()+":\tDEL rollback");

        //将缓冲区内容移除
        RespRequest remove = participant.getBuffer().remove(respRequest.getTransactionNo());

        //封装response
        RespResponse respResponse = new RespResponse();
        //设置响应状态
        respResponse.setResponseType(RespResponse.DEL_ROLLBACK_DONE);
        //记录原本要DEL的key
        respResponse.setKeys(remove.getKeys());
        //设置原本要DEL的value
        respResponse.setValues(remove.getValues());

        return respResponse;
    }
}
