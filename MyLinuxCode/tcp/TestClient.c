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

#define PORT    32180   //定义通信端口

#define FALSE   (-1)
#define TRUE    (0)

int creat_client(char *server_ip)
{
    int clientFd;
    struct sockaddr_in client_addr;
    
    clientFd = socket(AF_INET, SOCK_STREAM, 0);
    if(clientFd < 0){
        return FALSE;
    }
    
    memset(&client_addr, 0, sizeof(client_addr));
    client_addr.sin_family = AF_INET;
    client_addr.sin_addr.s_addr = inet_addr(server_ip);
    client_addr.sin_port = htons(PORT);
    
    int ret = connect(clientFd, (struct sockaddr *)&client_addr, sizeof(client_addr));
    if(ret < 0){
        close(clientFd);
        printf("connect error \n");
        return FALSE;
    }
    
    return clientFd;
}

int send_msg(int socket,char *msg)
{
    char buf[1024] = {0};
    int ret = -1;
    
    ret = send(socket, msg, sizeof(msg), 0);
    if(ret <= 0){
        return FALSE;
    }
    
    ret = read(socket, buf, 1024);
    if(ret > 0){
        printf("read : %s \n", buf);
        if(ret < 1024){
            buf[ret] = '\0';
        }
        if(strncmp(msg, "quit", 4) == 0)
        {
            return TRUE;
        }
    }
    
    return FALSE;
}

int get_send_msg(char *msg)
{
    #if 0 
    char sendbuf[24] = {0};
    sprintf(msg, "GET /vinput?keyvalue=22&functionkey=0&userCode=free_123456789012345670556&YNBModIP=%s:%d\r\n", server_ip, PORT);
    strcat(msg, "User-Agent: JavaJ2ME Personal Basis 1.0\r\n");
    sprintf(sendbuf, "Host: %s:%d\r\n", server_ip, PORT);
    strcat(msg, sendbuf);
    strcat(msg, "Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2\r\n");
    strcat(msg, "Connection: keep-alive\r\n\r\n");
    #else
    memset(msg, 0, strlen(msg));
    printf("get me cmd: ");
    scanf("%s", msg);
    #endif
    //printf("send buf : \n %s",msg);
    
    return TRUE;
}

int main(int argc, char* argv[])  
{  
	int sclient = -1;
	char sendData[256] = {0};
	char server_ip[50] = {0};
    
	if (argc < 2)
	{
		printf("plase get me Ip addr\n");
		return -1;
	}
	
	strcpy(server_ip, argv[1]);
    sclient = creat_client(server_ip);
    if(sclient < 0){
        return FALSE;
    }
	while(1){
        get_send_msg(sendData);
        int ret = send_msg(sclient, sendData);
        if(ret == TRUE)
        {
            break; 
        }
        usleep(1000);
        printf("@@@@@@@@@@@@@@@@ shit \n");
	}
	getchar();
    close(sclient);

    return TRUE;
}  
