package com.lab2.Exceptions;

//长连接http中断异常
public class AliveConnectionClosedException extends Exception{
    /*无参构造函数*/
    public AliveConnectionClosedException(){
        super();
    }

    //用详细信息指定一个异常
    public AliveConnectionClosedException(String message){
        super(message);
    }

    //用指定的详细信息和原因构造一个新的异常
    public AliveConnectionClosedException(String message, Throwable cause){
        super(message,cause);
    }

    //用指定原因构造一个新的异常
    public AliveConnectionClosedException(Throwable cause) {
        super(cause);
    }

}
