#include <stdio.h>  
#include <stdlib.h>  
#include <string.h>
#include <strings.h>  
#include <unistd.h>  
#include <sys/types.h>  
#include <sys/socket.h> 
#include <memory.h>  
#include <arpa/inet.h>  
#include <netinet/in.h> 
#include <signal.h>
#include <pthread.h>

#define PORT    32180   //定义通信端口  
#define BACKLOG 5       //定义侦听队列长度  
#define buflen  1024  
#define OK_CLINET  "HTTP/1.1 200 OK\r\nDate: Sat, 31 Dec 2005 23:59:59 GMT\r\nContent-Type: text/html;charset=ISO-8859-1\r\nContent-Length: 49\r\n\r\n{\"errCode\":\"0\",\"errMsg\":\"授权码不存在\"}\r\n\r\n"

#define TRUE    (0)
#define FALSE   (-1)

int creat_socket()
{
    struct sockaddr_in server_addr;
    int ret = -1;
    
    int socketFd = socket(AF_INET, SOCK_STREAM, 0);
    if(socketFd < 0){
        printf("socket error \n");
        return FALSE;
    }
    
    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(PORT);
    server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    
    ret = bind(socketFd, (struct sockaddr *)&server_addr, sizeof(server_addr));
    if(ret < 0){
        printf("bind error prot : %d\n",PORT);
        close(socketFd);
        return FALSE;
    }
    
    ret = listen(socketFd, BACKLOG);
    if(ret < 0){
        printf("listen error port: %d\n",PORT);
        close(socketFd);
        return FALSE;
    }
    
    return socketFd;
}

int wait_client(int socket)
{
    struct sockaddr_in clinet_addr;
    int len = 0;
    
    memset(&clinet_addr, 0, sizeof(clinet_addr));
    len = sizeof(clinet_addr);
    
    int clinet_socket = accept(socket, (struct sockaddr *)&clinet_addr, &len);
    if(clinet_socket == -1){
        printf("accept error \n");
        return FALSE;
    }
    
    return clinet_socket;
}

int handl_client(int socket)
{
    char buf[buflen] = {0};
    char read_ok[11] = "accpet ok:";
    char send_buf[buflen] = {0};
    
    while(1){
        int ret = read(socket, buf, buflen);
        if(ret <= 0){
            break;
        }
        if(ret < 1024){
            buf[ret] = '\0';
        }
        
        printf("read : %s\n", buf);
        
        memset(send_buf, 0, sizeof(send_buf));
        strcat(send_buf, read_ok);
        strcat(send_buf, buf);
        send(socket, send_buf, sizeof(send_buf), 0);
        if(strncmp(buf, "quit", 4) == 0){
            printf("client quit !\n");
            break;
        }
    }
    
    close(socket);
    
    return TRUE;
}

int main(int argc,char *argv[])  
{  
  
    /*****************socket()***************/  
    int server_socket = creat_socket();
    if(server_socket < 0){
        printf("creat socket error \n");
    }
    printf("server : listen ok\n"); 

    for (;;)  
    {
        int client_socket = wait_client(server_socket);
        if(client_socket < 0){
            printf("wait client error \n");
            continue;
        }
        pthread_t id;
        pthread_create(&id, NULL, handl_client, (void *)client_socket);
        
       pthread_detach(id);
    }
    close(server_socket);
}  
