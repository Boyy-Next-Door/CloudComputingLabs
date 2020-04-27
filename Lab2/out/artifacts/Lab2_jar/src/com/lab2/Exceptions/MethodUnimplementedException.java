package com.lab2.Exceptions;

//HTTP请求方式不支持异常
public class MethodUnimplementedException extends Exception{
    /*无参构造函数*/
    public MethodUnimplementedException(){
        super();
    }

    //用详细信息指定一个异常
    public MethodUnimplementedException(String message){
        super(message);
    }

    //用指定的详细信息和原因构造一个新的异常
    public MethodUnimplementedException(String message, Throwable cause){
        super(message,cause);
    }

    //用指定原因构造一个新的异常
    public MethodUnimplementedException(Throwable cause) {
        super(cause);
    }

}
