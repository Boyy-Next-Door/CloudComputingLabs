#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <fcntl.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <sys/wait.h>
#include <sys/stat.h>

#include <pthread.h>
#include <getopt.h>

#define MAX_CLIENT_COUNT 10
#define RECV_BUF_SIZE 512
#define SEND_BUF_SIZE 512
int port = 0;

enum RequestType{REQUEST_GET, REQUEST_POST, REQUEST_UNDEFINED};


void reuseAddr(int socketFd){
    int on = 1;
    int ret = setsockopt(socketFd,SOL_SOCKET,SO_REUSEADDR,&on,sizeof(on));
    if(ret==-1){
        fprintf(stderr, "Error : fail to setsockopt\n");
        exit(1);
    }
}


int startTcpServer(char *ipAddr, int portNum){
    
    int httpdSocket = socket(AF_INET,SOCK_STREAM,0);
    if(httpdSocket==-1){
        fprintf(stderr,"Error: can't create socket,errno is %d\n",errno);
        exit(1);
    }


    if(bind(httpdSocket,(const struct sockaddr*)&tcpServerSockAddr,    
        sizeof(tcpServerSockAddr))==-1){
        fprintf(stderr, "Error: can't bind port %d,errno is %d\n",portNum,errno);
        exit(1);
    }

    if(listen(httpdSocket,MAX_CLIENT_COUNT)==-1){         
        fprintf(stderr,"Error: can't listen port %d,error is %d\n",portNum,errno);
        exit(1);
    }

    return httpdSocket;
}


int getOneLineFromSocket(int socketFd,char* buf,int bufLength){
    int byteCount = 0;
    memset(buf, 0, bufLength);
    while(read(socketFd,&tmpChar,1) && byteCount<bufLength){
        if(tmpChar=='\r'){
            if(recv(socketFd,&tmpChar,1,MSG_PEEK)==-1){
                fprintf(stderr, "Error: fail to recv char after \\r\n");
                exit(1);
            }
          
            if(tmpChar=='\n' && byteCount<bufLength){
                read(socketFd,&tmpChar,1);
                buf[byteCount++] = '\n';
                break;
            }
            buf[byteCount++] = '\r';
        }else{
            buf[byteCount++] = tmpChar;
        }
    }
    return byteCount;
}


ssize_t socketSendMsg(int socketFd, const char* msg){
    return write(socketFd, msg, strlen(msg));
}


void responseP(int socketFd, char* contentType, char* namevalue, char* idvalue)
{
    char sendBuf[SEND_BUF_SIZE] = {0};

    sprintf(sendBuf,"HTTP/1.1 200 OK\r\n");
    socketSendMsg(socketFd, sendBuf);

    sprintf(sendBuf, "Server: %s\r\n", "BeiAnPaiLe's web Server");
    socketSendMsg(socketFd, sendBuf);
    contentType = "text/html";
    sprintf(sendBuf, "Content-type: %s\r\n", contentType);
    socketSendMsg(socketFd, sendBuf);
    sprintf(sendBuf, "Host: %s", "127.0.0.1:");
    char pp[10];
    sprintf(pp, "%d\r\n", port);
    strcat(sendBuf,pp);
    socketSendMsg(socketFd, sendBuf);
    socketSendMsg(socketFd, "\r\n");

}


void* responseBrowserRequest(void* ptr){
    int browserSocket = *(int*)ptr;
    char c;
    char recvBuf[RECV_BUF_SIZE+1] = {0};
    int contentLength = 0;
    enum RequestType requestType = REQUEST_UNDEFINED;
    
 
    #define FILE_PATH_LENGTH 128
    char requestFilePath[FILE_PATH_LENGTH] = {0};

    
    #define QUERY_STRING_LENGTH 128
    char PostString[QUERY_STRING_LENGTH] = {0};

    char name[5] = {0};
    char namevalue[QUERY_STRING_LENGTH] = {0};
    char id[3] = {0};
    char idvalue[QUERY_STRING_LENGTH] = {0};

   
    while(getOneLineFromSocket(browserSocket,recvBuf,RECV_BUF_SIZE)){   
        // printf("%s",recvBuf);
        if(strcmp(recvBuf, "\n")==0){
            break;
        }

        if(requestType==REQUEST_UNDEFINED){       
            int pFileName = 0;
            int pQueryString = 0;
            int pRecvBuf = 0;

            if(strncmp(recvBuf,"GET",3)==0){     
                
                requestType = REQUEST_GET;
                pRecvBuf = 4;
                
            }else if(strncmp(recvBuf,"POST",4)==0){
                
                requestType = REQUEST_POST;
                pRecvBuf = 5;
            }

           
            if(pRecvBuf){      
                requestFilePath[pFileName++] = '.';
                while(pFileName<FILE_PATH_LENGTH && recvBuf[pRecvBuf]
                    && recvBuf[pRecvBuf]!=' ' && recvBuf[pRecvBuf]!='?'){
                    requestFilePath[pFileName++] = recvBuf[pRecvBuf++];
                }

                if(pFileName<FILE_PATH_LENGTH && recvBuf[pRecvBuf]=='?'){     
                    ++pRecvBuf;
                    while(pQueryString<QUERY_STRING_LENGTH && 
                        recvBuf[pRecvBuf] && recvBuf[pRecvBuf]!=' '){
                        PostString[pQueryString++] = recvBuf[pRecvBuf++];
                    }
                }
            }
        }else if(requestType==REQUEST_GET){

        }else if(requestType==REQUEST_POST){
            if(strncmp(recvBuf, "Content-Length:", 15)==0){         
                contentLength = atoi(recvBuf+15);
            }
        }
    }

   
    if(requestType==REQUEST_POST && contentLength){
        if(contentLength > QUERY_STRING_LENGTH){
            fprintf(stderr, "Query string buffer is smaller than content length\n");
            contentLength = QUERY_STRING_LENGTH;
        }
        read(browserSocket, PostString, contentLength);
    }

    int i=0, t=0, k=0;
    for(i=0;i<4;i++) {name[i]=PostString[t]; t++;}
    if(strcmp(name,"Name")==0){
        i=0; t++;
        while(PostString[t]!='&'){namevalue[i]=PostString[t]; t++; i++;}
        t++;
        for(i=0;i<2;i++) {id[i]=PostString[t]; t++;}
        if(strcmp(id,"ID")==0){
            k=1; t++;
            for(i=0;i<contentLength;i++) {idvalue[i]=PostString[t]; t++;}
        }
    }
    
    
    struct stat fileInfo;
    stat(requestFilePath,&fileInfo);
    if(S_ISDIR(fileInfo.st_mode)){
    
        if(strcmp(requestFilePath,"./Post_show")!=0) k=0;
        char tt[11]="/index.html";
        strcat(requestFilePath,tt);
    }


    switch(requestType){
        case REQUEST_GET:
                responseStaticFile(browserSocket,200,requestFilePath,NULL);
            break;
        case REQUEST_POST:
            {
                if(contentLength==0){
                    responseStaticFile(browserSocket,404,"./404.html","text/html");
                    break;
                }
                else if(k==0){
                    responseStaticFile(browserSocket,404,"./404.html","text/html");
                }
                else{
                    responseP(browserSocket,"text/html",namevalue,idvalue);
                }
            }
            break;
        case REQUEST_UNDEFINED:
            {
                responseStaticFile(browserSocket, 501, "./501.html","text/html");
            }
            break;
        default:
            break;
    }
    
    close(browserSocket);
    return NULL;
}

int main(int argc,char* argv[]){
 
    char *string = "a:b:c:d";
    static struct option long_options[] =
    {  
        {"ip", required_argument, NULL, '1'},
        {"port", required_argument, NULL, '1'},
        {"number-thread", required_argument, NULL,'1'},
        {NULL,  0,   NULL, 0},
    }; 
   
    if(argc < 7){
        fprintf(stderr,"USAGE: %s --ip IPaddr --port portNum --number-thread threadNum\n",argv[0]);
        exit(1);
    }
    int portNum = atoi(argv[4]);
    port = portNum;
    char *ipAddr=argv[2];

    
    if((portNum!=80)&&(portNum<1024 || portNum>65535)){
        fprintf(stderr,"Error");
        exit(1);
    }

    int httpdSocket = startTcpServer(ipAddr, portNum);

    

    return 0;
}
