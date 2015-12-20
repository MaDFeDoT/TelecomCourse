#define _WINSOCK_DEPRECATED_NO_WARNINGS
#define _CRT_SECURE_NO_WARNINGS
#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include<iostream>
#include<string>
#include <strings.h>
using namespace std;
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <netinet/in.h>
#include <unistd.h>
#include <fcntl.h>
#include <pthread.h>
void * Print_Server_Mes(void * arg);

bool have_connect = false;
struct sockaddr_in si_other;
int slen = sizeof(si_other);
int main(int argc, char *argv[])
{
    //struct sockaddr_in si_other;
    int s;
    //int slen = sizeof(si_other);
    char buf[255];
    char message[255];
    while (1)
    {
        bool start = true;
        while (start)
        {
            char choose;
            cout << "What do you want?:\n	1:connect\n	2:exit\n Your choose: ";
            cin >> choose;
            switch (choose)
            {
            case '1': start = false; break;
            case '2': return 0; break;
            default: cout << "That's not a choice.\n";
            }
        }
        //create socket
        if ((s = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) < 0)
        {
            printf("socket() failed");
            exit(EXIT_FAILURE);
        }

        /*cout << "Enter server IP adress: ";
        string ip_adress;
        cin >> ip_adress;
        cout << "Enter number of port: ";
        int port;
        cin >> port;
        server.sin_addr.s_addr = inet_addr(ip_adress.c_str());
        server.sin_family = AF_INET;
        server.sin_port = htons(port);*/
        //setup address structure
        bzero((char *)&si_other, sizeof(si_other));
        si_other.sin_family = AF_INET;
        si_other.sin_port = htons(5001);
        si_other.sin_addr.s_addr =inet_addr("192.168.56.101");
        bool flag_croupier = false;
        bool flag = true;
        string mes_to_serv;
        char user_a;
        int sleep_time = 1;

        while (flag)
        {
            cout << "Do you want to be a croupier? y/n\n";
            string answer;
            cin >> answer;
            if ((answer != "y") && (answer != "n"))
                cout << "Error. Write Y or N";
            if (answer == "y")
            {
                mes_to_serv = "1";
                flag = false;
                flag_croupier = true;
            }
            else if (answer == "n")
            {
                mes_to_serv = "0";
                flag = false;
            }
        }
        cout << "Enter your name: ";
        string name;
        cin >> name;
        mes_to_serv += name;
        mes_to_serv += '.';
        //say connect to server
        mes_to_serv += '.';
        //send the message
        if (sendto(s, "connect", 7, 0, (struct sockaddr *) &si_other, slen) <0)
        {
            printf("sendto() failed");
            exit(EXIT_FAILURE);
        }
        bzero(buf, 255);
        //try to receive some data, this is a blocking call
        if (recvfrom(s, buf, 255, 0, (struct sockaddr *) &si_other, (socklen_t*)&slen) <0)
        {
            printf("recvfrom() failed");
            exit(EXIT_FAILURE);
        }
        int new_port = atoi(buf);
        cout<<new_port<<endl;
        si_other.sin_port = htons(new_port);
        //send the message
        if (sendto(s, mes_to_serv.c_str(), mes_to_serv.length(), 0, (struct sockaddr *) &si_other, slen) <0)
        {
            printf("sendto() failed");
            exit(EXIT_FAILURE);
        }

        char server_reply[2000];
        int recv_size;
        if ((recv_size = recvfrom(s, server_reply, 2000, 0, (struct sockaddr *) &si_other, (socklen_t*) &slen)) <0)
        {
            puts("recv failed");
        }
        server_reply[recv_size] = '\0';
        cout << "Server:  " << server_reply << "\n";
        std::string buffer(server_reply);
        if (buffer.find("We already have a croupier.") != string::npos)
            flag_croupier = false;

        have_connect = true;

        int flag_thr;
        pthread_attr_t threadAttr;
        pthread_attr_init(&threadAttr);
        pthread_attr_setdetachstate(&threadAttr, PTHREAD_CREATE_DETACHED);
        pthread_t serv;
        flag_thr = pthread_create(&serv, &threadAttr, Print_Server_Mes, (void*)s);
        if(flag_thr != 0)
        {
            perror( "Creating thread false");
            exit(1);
        }

        if (flag_croupier == true)
        {
            while (have_connect)
            {
                cout << "You can:\n";
                cout << "	1: Disconnect\n";
                cout << "	2: Watch rates\n";
                cout << "	3: Start hoaxs\n";
                cout << "	4: Finish hoaxs\n";
                cin >> user_a;
                if (have_connect == false) break;
                switch (user_a)
                {
                case '1':	mes_to_serv = "1";
                    if (sendto(s, mes_to_serv.c_str(), mes_to_serv.length(), 0, (struct sockaddr *) &si_other, slen) < 0)
                    {
                        puts("Send failed\n");
                    };
                    have_connect = false;
                    sleep(sleep_time);
                    break;
                case '2':	mes_to_serv = "2";
                    if (sendto(s, mes_to_serv.c_str(), mes_to_serv.length(), 0, (struct sockaddr *) &si_other, slen) < 0)
                    {
                        puts("Send failed\n");
                    };
                    sleep(sleep_time);
                    break;
                case '3':	mes_to_serv = "4";
                    if (sendto(s, mes_to_serv.c_str(), mes_to_serv.length(), 0, (struct sockaddr *) &si_other, slen) < 0)
                    {
                        puts("Send failed\n");
                    };
                    sleep(sleep_time);
                    break;
                case '4':	mes_to_serv = "5";
                    if (sendto(s, mes_to_serv.c_str(), mes_to_serv.length(), 0, (struct sockaddr *) &si_other, slen) < 0)
                    {
                        puts("Send failed\n");
                    };
                    sleep(sleep_time);
                    break;
                default: cout << "That's not a choice.\n";
                }
            }
        }
        else
        {
            while (have_connect)
            {
                cout << "You can:\n";
                cout << "	1: Disconnect\n";
                cout << "	2: Watch rates\n";
                cout << "	3: Make rate\n";
                cin >> user_a;
                if (have_connect == false) break;
                int true_rate = 1;
                char rate_type;
                string type_r = "3";
                switch (user_a)
                {
                case '1':	mes_to_serv = "1";
                    if (sendto(s, mes_to_serv.c_str(), mes_to_serv.length(), 0, (struct sockaddr *) &si_other, slen) < 0)
                    {
                        puts("Send failed\n");
                    };
                    have_connect = false;
                    sleep(sleep_time);
                    break;
                case '2':	mes_to_serv = "2";
                    if (sendto(s, mes_to_serv.c_str(), mes_to_serv.length(), 0, (struct sockaddr *) &si_other, slen) < 0)
                    {
                        puts("Send failed\n");
                    };
                    sleep(sleep_time);
                    break;
                case '3':	while (true_rate)
                            {
                                cout << "Choose the type of rate:\n";
                                cout << "	e - even\n	o - odd\n	n - number\n";
                                cin >> rate_type;
                                switch (rate_type)
                                {
                                        case 'e': mes_to_serv = "e"; true_rate = 0; break;
                                        case 'o': mes_to_serv = "o"; true_rate = 0; break;
                                        case 'n': mes_to_serv = "n"; true_rate = 0; break;
                                        default: cout << "That's not a choice.\n";
                                }
                            }
                            cout << "Enter a cash:\n";
                            int cash;
                            cin >> cash;
                            mes_to_serv += to_string(cash) + ".";
                            if (rate_type == 'n')
                            {
                                cout << "Enter a number:\n";
                                int number;
                                cin >> number;
                                mes_to_serv += to_string(number) + ".";
                            }
                            if (sendto(s, type_r.c_str(), type_r.length(), 0, (struct sockaddr *) &si_other, slen) < 0)
                            {
                                puts("Send failed\n");
                            };
                            for (int i = 0; i < mes_to_serv.length(); i++)
                            {
                                type_r = mes_to_serv[i];
                                if (sendto(s, type_r.c_str(), type_r.length(), 0, (struct sockaddr *) &si_other, slen) < 0)
                                {
                                    puts("Send failed\n");
                                };
                            }
                            sleep(sleep_time);
                            break;
                default: cout << "That's not a choice.\n";
                }
            }
        }
    }
}

void *Print_Server_Mes(void * arg)
{
    char server_reply[2000];
    int recv_size;
    int s = (int)arg;
    while (have_connect)
    {
        if ((recv_size = recvfrom(s, server_reply, 2000, 0, (struct sockaddr *) &si_other, (socklen_t*) &slen)) <0)
        {
            puts("recv failed");
            string mes = server_reply;
            cout << "Server:  " << mes;
            return 0;
        }
        server_reply[recv_size] = '\0';
        string mes = server_reply;
        if (mes.find("You disconected") != string::npos)
        {
            cout << "Server:  " << mes;
            have_connect = false;
            return 0;
        }
        cout << "Server:  " << server_reply;
    }
    return 0;
}
