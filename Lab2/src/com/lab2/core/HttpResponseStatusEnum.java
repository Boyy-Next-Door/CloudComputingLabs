package com.lab2.core;

//响应状态码和状态信息
public enum HttpResponseStatusEnum {
    正常("OK", 200),
    没有找到资源("Not Found", 404),
    内部错误("Internal Server Error", 500);

    private String describe;

    private int code;

    HttpResponseStatusEnum(String describe, int code) {
        this.describe = describe;
        this.code = code;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}