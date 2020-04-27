package com.lab2.core;

//响应状态码和状态信息
public enum HttpResponseStatusEnum {
    OK("OK", 200),
    NOT_FOUND("Not Found", 404),
    ERROR("Internal Server Error", 500),
    NOT_IMPLEMENTED("Not Implemented",501);

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