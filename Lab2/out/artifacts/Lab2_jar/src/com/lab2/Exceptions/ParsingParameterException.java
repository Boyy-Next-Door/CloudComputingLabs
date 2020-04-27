package com.lab2.Exceptions;

//HTTP参数解析错误异常
public class ParsingParameterException extends Exception{
    /*无参构造函数*/
    public ParsingParameterException(){
        super();
    }

    //用详细信息指定一个异常
    public ParsingParameterException(String message){
        super(message);
    }

    //用指定的详细信息和原因构造一个新的异常
    public ParsingParameterException(String message, Throwable cause){
        super(message,cause);
    }

    //用指定原因构造一个新的异常
    public ParsingParameterException(Throwable cause) {
        super(cause);
    }

}
