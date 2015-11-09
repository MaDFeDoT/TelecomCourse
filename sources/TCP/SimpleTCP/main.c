#include <stdio.h>
#include <stdlib.h>

#include <netdb.h>
#include <netinet/in.h>
#include <unistd.h>
#include <fcntl.h>

#include <string.h>
#include <pthread.h>

#include <time.h>

int Have_Croupier;
int ID;
int Flag_start_hoax;
int Flag_finish_hoax;
int Koll_Connect;
int Koll_Rate;

struct Connect_Info{
    int id_con;
    char* Name;
    void* i_sock;
    int flag_dis;
};
struct Connect_Info* All_Connects;

struct Rates_Info{
    int id_pl;
    char* Name;
    void* i_sock;
    char* type;
    int summa;
    int number;
};
struct Rates_Info* All_Rates;

void *Log_and_Work(void* socket); //обработчик клиентов
void *Server();

int main() {
   Have_Croupier=0;
   Flag_finish_hoax=0;
   ID=0;
   Koll_Connect=0;
   int sockfd, newsockfd, portno, clilen, flag_thr;
   struct sockaddr_in serv_addr, cli_addr;
   const int on = 1;
   /* First call to socket() function */
   sockfd = socket(AF_INET, SOCK_STREAM, 0);

   if (sockfd < 0) {
      perror("ERROR opening socket");
      exit(1);
   }

   if ( setsockopt( sockfd, SOL_SOCKET, SO_REUSEADDR, &on,
   sizeof( on ) ) )
   {
        perror("ERROR call setsockopt");
        exit(1);
   }
   /* Initialize socket structure */
   bzero((char *) &serv_addr, sizeof(serv_addr));
   //printf("Write port number:  ");
   //scanf("%d", &portno);
   portno = 5001;

   serv_addr.sin_family = AF_INET;
   serv_addr.sin_addr.s_addr = INADDR_ANY;
   serv_addr.sin_port = htons(portno);

   /* Now bind the host address using bind() call.*/
   if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
      perror("ERROR on binding");
      exit(1);
   }
   /*Настройка атрибутов потока*/
   pthread_attr_t threadAttr;
   pthread_attr_init(&threadAttr);
   pthread_attr_setdetachstate(&threadAttr, PTHREAD_CREATE_DETACHED);

   /* Now start listening for the clients, here process will
    * go in sleep mode and will wait for the incoming connection
   */

   pthread_t serv;
   flag_thr = pthread_create(&serv, &threadAttr, Server, NULL);
   if(flag_thr != 0)
   {
       perror( "Creating thread false");
       exit(1);
   }

   listen(sockfd,5);
   clilen = sizeof(cli_addr);
   while(1)
   {
        /* Accept actual connection from the client */
        newsockfd = accept(sockfd, (struct sockaddr *)&cli_addr, &clilen);

        if (newsockfd < 0) {
            perror("ERROR on accept");
            exit(1);
        }
        pthread_t thread;
        flag_thr = pthread_create(&thread, &threadAttr, Log_and_Work, (void*)newsockfd);
        if(flag_thr != 0)
        {
            perror( "Creating thread false");
            exit(1);
        }
   }
   return 0;
}

void *Log_and_Work(void* newsockfd)
{
    char *Name;
    char buffer[255];
    int rc, flag, i,kol, my_id;
    ID=ID+1;
    my_id=ID;
    bzero(buffer,255);
    rc = read( newsockfd,buffer,255 ); //здесь крашиться
    if (rc < 0) {
       perror("ERROR reading from socket");
       exit(1);
    }
    flag=(int) buffer[0] - (int)'0';
    kol=0;
    while(buffer[kol]!='.')
    {
        kol=kol+1;
    }
    Name =(char*) malloc (sizeof(char) * kol);
    for(i=1; i<kol; i++)
    {
        Name[i-1]=buffer[i];
    }
    Name[kol-1]='\0';  //Вставляем нулевой символ окончания строки.
    if (Koll_Connect==0)
    {
        Koll_Connect=Koll_Connect+1;
        All_Connects=(struct Connect_Info*) malloc(sizeof(struct Connect_Info));
        All_Connects[0].id_con=my_id;
        All_Connects[0].i_sock=newsockfd;
        All_Connects[0].Name=Name;
        All_Connects[0].flag_dis=0;
    }
    else
    {
        Koll_Connect=Koll_Connect+1;
        All_Connects=(struct Connect_Info*) realloc(All_Connects, sizeof(struct Connect_Info)*Koll_Connect);
        All_Connects[Koll_Connect-1].id_con=my_id;
        All_Connects[Koll_Connect-1].i_sock=newsockfd;
        All_Connects[Koll_Connect-1].Name=Name;
        All_Connects[Koll_Connect-1].flag_dis=0;
    }
    if(flag!=0)
    {
        if(Have_Croupier==1)
        {
            write(newsockfd, "We already have a croupier. You will be a player.",49);
            Player(All_Connects[Koll_Connect-1].Name, All_Connects[Koll_Connect-1].i_sock, All_Connects[Koll_Connect-1].id_con);//Name, newsockfd, my_id
        }
        else
        {
            Have_Croupier=1;
            Croupier(All_Connects[Koll_Connect-1].Name, All_Connects[Koll_Connect-1].i_sock, All_Connects[Koll_Connect-1].id_con);//Name, newsockfd, my_id
        }
    }
    else
        Player(All_Connects[Koll_Connect-1].Name, All_Connects[Koll_Connect-1].i_sock, All_Connects[Koll_Connect-1].id_con);//Name, newsockfd, my_id
    free(Name);
    pthread_exit(NULL);
}

void Croupier(char *Name, void* newsockfd, int my_id)
{
   char c[1],buf[60];
   int i,rc,Roll_Result;
   printf("Connect croupier: Name: %s. ID: %d.\n",Name,my_id);
   fcntl(newsockfd, F_SETFL, O_NONBLOCK);
   while(1)
   {
       for(i=0; i<Koll_Connect; i++)
       {
           if (All_Connects[i].id_con==my_id)
           {
               if(All_Connects[i].flag_dis==1)
               {
                   Have_Croupier=0;
                   disconnect(newsockfd, my_id, Name);
               }
               break;
           }
       }
       rc=read(newsockfd, c,1);
       if(rc>0)
       {
            if(c[0]=='1')
            {
                Have_Croupier=0;
                disconnect(newsockfd, my_id, Name);
            }
            if(c[0]=='4')
            {
                if (Flag_start_hoax==0)
                {
                    Koll_Rate=0;
                    Flag_finish_hoax=0;
                    Flag_start_hoax=1;
                    printf("Hoax starts!\n");
                }
                else
                    write(newsockfd, "The hoax already starts\n",25);
            }
            if(c[0]=='5')
            {
                if (Flag_start_hoax==0)
                    write(newsockfd, "But first let the hoax starts\n",31);
                else
                {
                    Flag_start_hoax=0;
                    Flag_finish_hoax=1;
                    srand( time(0) );
                    Roll_Result=rand()%37;
                    printf("Hoax finish! RollNumber=%d\n",Roll_Result);
                    for(i=0; i<Koll_Rate; i++)
                    {
                        write(All_Rates[i].i_sock, "Hoax finish!\n\n",14);
                        snprintf(buf, 60, "Winning number is %d\n\n", Roll_Result);
                        write(All_Rates[i].i_sock, buf, strlen(buf));
                        if((All_Rates[i].type[0]=='n')&&(All_Rates[i].number==Roll_Result))
                        {
                            write(All_Rates[i].i_sock, "You win!\n",9);
                            snprintf(buf, 60, "Your prize is %d$\n\n", All_Rates[i].summa*35);
                            write(All_Rates[i].i_sock, buf, strlen(buf));
                        }
                        else
                            if((All_Rates[i].type[0]=='e')&&(Roll_Result%2==0))
                            {
                                write(All_Rates[i].i_sock, "You win!\n",9);
                                snprintf(buf, 60, "Your prize is %d$\n\n", All_Rates[i].summa);
                                write(All_Rates[i].i_sock, buf, strlen(buf));
                            }
                            else
                                if((All_Rates[i].type[0]=='o')&&(Roll_Result%2==1))
                                {
                                    write(All_Rates[i].i_sock, "You win!\n",9);
                                    snprintf(buf, 60, "Your prize is %d$\n\n", All_Rates[i].summa);
                                    write(All_Rates[i].i_sock, buf, strlen(buf));
                                }
                                else
                                    write(All_Rates[i].i_sock, "Sorry, you lose.\n\n",18);
                        free(All_Rates);
                    }
                }

            }
       }
   }
}

void Player(char *Name, void* newsockfd, int my_id)
{
   int i,rc, Flag_Rate;
   char c[1],buf[255];
   fcntl(newsockfd, F_SETFL, O_NONBLOCK);
   printf("Connect player: Name: %s. ID: %d.\n",Name,my_id);
   while(1)
   {
        write(newsockfd, "Waiting the start of hoax\n",26);
        while(Flag_start_hoax==0)
        {
             for(i=0; i<Koll_Connect; i++)
             {
                 if (All_Connects[i].id_con==my_id)
                 {
                     if(All_Connects[i].flag_dis==1)
                         disconnect(newsockfd, my_id, Name);
                     break;
                 }
             }

             rc = read( newsockfd, c, 1);
             if((rc>0)&&(c[0]=='1'))
             {
                   disconnect(newsockfd, my_id, Name);
             }
        }
        write(newsockfd, "Hoax starts!\n",13);
        Flag_Rate=0;
        while(Flag_finish_hoax==0)
        {
             for(i=0; i<Koll_Connect; i++)
             {
                 if (All_Connects[i].id_con==my_id)
                 {
                     if(All_Connects[i].flag_dis==1)
                         disconnect(newsockfd, my_id, Name);
                     break;
                 }
             }
             rc = read( newsockfd, c, 1);
             if(rc>0)
             {
                   if (c[0]=='1')
                   {
                        disconnect(newsockfd, my_id, Name);
                   }
                   if(c[0]=='2')
                   {
                        Watch_Rate(newsockfd);
                   }
                   if(c[0]=='3')
                   {
                        if(Flag_Rate==0)
                        {
                            Do_Rate(newsockfd, my_id, Name);
                            Flag_Rate=1;
                        }
                        else
                        {
                            write(newsockfd, "You already rate! Wait results.\n",33);
                            read( newsockfd, buf, 255);
                            bzero(buf,255);
                        }
                   }
             }
         }
   }
}
void disconnect(void* newsockfd, int my_id, char *Name)
{
    int flag,i,KolDis;

    for(i=0; i<Koll_Connect; i++)
    {
        if (All_Connects[i].id_con==my_id)
        {
            KolDis=i;
            break;
        }
    }
    if(Koll_Connect!=1)
    {
        for(i=KolDis; i<Koll_Connect-1; i++)
        {
            All_Connects[i].id_con=All_Connects[i+1].id_con;
            All_Connects[i].i_sock=All_Connects[i+1].i_sock;
            All_Connects[i].Name=All_Connects[i+1].Name;
            All_Connects[i].flag_dis=All_Connects[i+1].flag_dis;
        }
        Koll_Connect=Koll_Connect-1;
        All_Connects=(struct Connect_Info*) realloc(All_Connects, sizeof(struct Connect_Info)*Koll_Connect);
    }
    else
    {
        Koll_Connect=0;
        free(All_Connects);
        All_Connects=NULL;
    }

    printf("Disconnected: Name: %s ID:%d\n",Name,my_id);
    write(newsockfd, "You disconected\n", 17);
    flag=shutdown(newsockfd, SHUT_RDWR);
    if (flag < 0) {
       perror("ERROR shutdown socket");
       exit(1);
    }
    flag=close(newsockfd);
    if (flag < 0) {
       perror("ERROR close socket");
       exit(1);
    }
    free(Name);
    pthread_exit(NULL);
}
void Watch_Rate(void* newsockfd)
{
    char buf[60],message[105];
    int i;
    snprintf(message, 105, "RATES\nType:\n  e-even\n  o-odd\n  n-number(after that write the number)\nName | ID | Type | SUMM\n\n");
    write(newsockfd, message, strlen(message));
    for(i=0; i<Koll_Rate; i++)
    {
        write(newsockfd, All_Rates[i].Name,strlen(All_Rates[i].Name));
        write(newsockfd, " ",1);
        snprintf(buf, 60, "%d", All_Rates[i].id_pl);
        write(newsockfd, buf, strlen(buf));
        write(newsockfd, " ",1);
        write(newsockfd, All_Rates[i].type,1);
        write(newsockfd, " ",1);
        if(All_Rates[i].type[0]=='n')
        {
            snprintf(buf, 60, "%d", All_Rates[i].number);
            write(newsockfd, buf, strlen(buf));
            write(newsockfd, " ",1);
        }
        snprintf(buf, 60, "%d$", All_Rates[i].summa);
        write(newsockfd, buf, strlen(buf));
        write(newsockfd, "\n",1);
    }
}
void Do_Rate(void* newsockfd, int my_id, char *Name)
{
    char type[1],c[1];
    int summa,n,number;
    read( newsockfd, type, 1);
    summa=0;
    read( newsockfd, c, 1);
    do{
        summa=summa*10;
        n=(int) c[0] - (int)'0';
        summa=summa+n;
        read( newsockfd, c, 1);
    }while(c[0]!='.');
    if(type[0]=='n')
    {
        number=0;
        read( newsockfd, c, 1);
        do{
            number=number*10;
            n=(int) c[0] - (int)'0';
            number=number+n;
            read( newsockfd, c, 1);
        }while(c[0]!='.');
    }
    if(type[0]=='n')
        printf("New Rate from %s: Type %s Summ %d Number %d\n",Name,type,summa,number);
    else
        printf("New Rate from %s: Type %s Summ %d\n",Name,type,summa);
    Koll_Rate=Koll_Rate+1;
    if (Koll_Rate==1)
             All_Rates=(struct Rates_Info*) malloc(sizeof(struct Rates_Info));
    else
             All_Rates=(struct Rates_Info*) realloc(All_Rates, sizeof(struct Rates_Info)*Koll_Rate);
    All_Rates[Koll_Rate-1].id_pl=my_id;
    All_Rates[Koll_Rate-1].Name=Name;
    All_Rates[Koll_Rate-1].i_sock=newsockfd;
    All_Rates[Koll_Rate-1].type=type;
    All_Rates[Koll_Rate-1].summa=summa;
    All_Rates[Koll_Rate-1].number=number;

}
void *Server()
{
    int i,dis_id,n,flag;
    char buf[40];
    dis_id=0;
    while(1)
    {
        scanf("%s",buf);
        flag=0;
        if((buf[0]=='d')&&(buf[9]=='t')&&(buf[10]=='_'))
        {
            dis_id=0;
            for(i=11; buf[i]!='.'; i++)
            {
                dis_id=dis_id*10;
                n=(int) buf[i] - (int)'0';
                if((n<0)||(n>9))
                {
                    flag=1;
                    break;
                }
                dis_id=dis_id+n;
            }
            if(flag==1)
            {
                printf("Error: write the comand correctly: disconnect_id.\n");
            }
            else
            {
                disconnect_serv(dis_id);
            }
        }
    }
}

void disconnect_serv(int my_id)
{
    int i,KolDis;
    KolDis=-1;
    for(i=0; i<Koll_Connect; i++)
    {
        if (All_Connects[i].id_con==my_id)
        {
            KolDis=i;
            break;
        }
    }
    if(KolDis==-1)
    {
        printf("Error: Cant find client with this ID\n");
        return;
    }
    All_Connects[KolDis].flag_dis=1;
}
