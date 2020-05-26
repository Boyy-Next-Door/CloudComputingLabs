package com.lab3.kvstoreparticipant.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lab3.kvstoreparticipant.participant.Database;
import com.lab3.kvstoreparticipant.protocol.RespRequest;
import com.lab3.kvstoreparticipant.protocol.RespResponse;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.crypto.Data;
import java.util.Map;

@RestController
@RequestMapping("/kvstore")
public class KVcontroller {

    @PostMapping(value = "set")
    RespResponse doSET(@RequestBody JSONObject jsonObject) {
        System.out.println("访问set接口");
        RespRequest respRequest = jsonObject.toJavaObject(RespRequest.class);
        if(respRequest.getRequestType()!=RespRequest.SET){
            //错误的SET指令
            RespResponse respResponse = new RespResponse();
            respResponse.setResponseType(RespResponse.ERROR);
            return respResponse;
        }
        return Database.SET(jsonObject.toJavaObject(RespRequest.class));
    }

    @PostMapping(value = "get")
    RespResponse doGET(@RequestBody JSONObject jsonObject) {
        System.out.println("访问get接口");
        RespRequest respRequest = jsonObject.toJavaObject(RespRequest.class);
        if(respRequest.getRequestType()!=RespRequest.GET){
            //错误的GET指令
            RespResponse respResponse = new RespResponse();
            respResponse.setResponseType(RespResponse.ERROR);
            return respResponse;
        }
        RespResponse r=Database.GET(jsonObject.toJavaObject(RespRequest.class));
//        System.out.println("in KVC r is =="+r.getValues().get(0));
//        System.out.println(JSON.toJSONString(r));
        return r;
    }

    @PostMapping(value = "del")
    RespResponse doDEL(@RequestBody JSONObject jsonObject) {
        System.out.println("访问del接口");
        RespRequest respRequest = jsonObject.toJavaObject(RespRequest.class);
        if(respRequest.getRequestType()!=RespRequest.DEL){
            //错误的DEL指令
            RespResponse respResponse = new RespResponse();
            respResponse.setResponseType(RespResponse.ERROR);
            return respResponse;
        }
        return Database.DEL(jsonObject.toJavaObject(RespRequest.class));
    }

    @PostMapping(value = "rollback")
    RespResponse rollback(@RequestBody JSONObject jsonObject) {
        System.out.println("访问rollback接口");
        RespRequest respRequest = jsonObject.toJavaObject(RespRequest.class);
        if (respRequest.getRequestType() == RespRequest.SET) {
            //SET回滚
            return Database.SET_ROLLBACK(respRequest);
        } else if (respRequest.getRequestType() == RespRequest.DEL) {
            //DEL回滚
            return Database.DEL_ROLLBACK(respRequest);
        } else {
            //错误的回滚指令
            RespResponse respResponse = new RespResponse();
            respResponse.setResponseType(RespResponse.ERROR);
            return respResponse;
        }
    }

    @PostMapping(value = "commit")
    RespResponse commit(@RequestBody JSONObject jsonObject) {
        System.out.println("访问commit接口");
        RespRequest respRequest = jsonObject.toJavaObject(RespRequest.class);
        if (respRequest.getRequestType() == RespRequest.SET) {
            //SET提交
            return Database.SET_COMMIT(respRequest);
        } else if (respRequest.getRequestType() == RespRequest.DEL) {
            //DEL提交
            return Database.DEL_COMMIT(respRequest);
        } else {
            //错误的提交指令
            RespResponse respResponse = new RespResponse();
            respResponse.setResponseType(RespResponse.ERROR);
            return respResponse;
        }
    }
}
