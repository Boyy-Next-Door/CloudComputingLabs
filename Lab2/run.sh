#!/bin/sh

 
JAR_PATH=Lab2.jar

#    $1  ip
#    $2  port
#    $3  number

#参数个数不能小于3个
#if [ $# -lt 3 ]; then
#     exit 1
#fi
 
java -jar $JAR_PATH $1 $2 $3 $4 $5 $6

#echo "java -jar $JAR_PATH $1 $2 $3 $4 $5 $6";

exit $?
