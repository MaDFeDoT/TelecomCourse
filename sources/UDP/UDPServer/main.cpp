#define _WINSOCK_DEPRECATED_NO_WARNINGS
#define _CRT_SECURE_NO_WARNINGS
#define snprintf(buf,len, format,...) _snprintf_s(buf, len,len, format, __VA_ARGS__)
#define bzero(p, size)     (void)memset((p), 0, (size))
#include <stdio.h>
#include <stdlib.h>
#include<winsock2.h>
#include<WS2tcpip.h>

#include <string.h>
#include <string>
#include <windows.h>
#include <process.h>

#include <time.h>

#pragma comment(lib,"ws2_32.lib")
#pragma comment (lib, "Mswsock.lib")
#pragma comment (lib, "AdvApi32.lib")
#include <iostream>;
using namespace std;
int Have_Croupier;
int ID;
int Flag_start_hoax;
int Flag_finish_hoax;
int Koll_Connect;
int Koll_Rate;
int portno;

struct Connect_Info{
	int id_con;
	char* Name;
	struct sockaddr_in client;
	int slen;
	SOCKET i_sock;
	int flag_dis;
};
struct Connect_Info* All_Connects;

struct Rates_Info{
	int id_pl;
	char* Name;
	SOCKET i_sock;
	char type;
	int summa;
	int number;
};
struct Rates_Info* All_Rates;

unsigned __stdcall Log_and_Work(void * arg); //обработчик клиентов
unsigned __stdcall Server(void * arg);
void Watch_Rate(SOCKET newsockfd, int my_id);
void Player(char *Name, SOCKET newsockfd, int my_id, struct sockaddr_in client, int slen);
void disconnect(SOCKET newsockfd, int my_id, char *Name);
void disconnect_serv(int my_id);
void Do_Rate(SOCKET newsockfd, int my_id, char *Name);
void Croupier(char *Name, SOCKET newsockfd, int my_i, struct sockaddr_in client, int slen);

int main() {
	Have_Croupier = 0;
	Flag_finish_hoax = 0;
	ID = 0;
	Koll_Connect = 0;
	SOCKET s;
	struct sockaddr_in server, si_other;
	int slen, recv_len;
	char buf[255];
	//printf("Write port number:  ");
	//scanf("%d", &portno);
	portno = 5001;
	WSADATA wsa;

	slen = sizeof(si_other);

	//Initialise winsock
	if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0)
	{
		printf("Failed. Error Code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
	//Create a socket
	if ((s = socket(AF_INET, SOCK_DGRAM, 0)) == INVALID_SOCKET)
	{
		printf("Could not create socket : %d", WSAGetLastError());
	}
	//Prepare the sockaddr_in structure
	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	//server.sin_addr.s_addr = inet_addr("127.0.0.1");
	server.sin_port = htons(portno);
	//Bind
	if (bind(s, (struct sockaddr *)&server, sizeof(server)) == SOCKET_ERROR)
	{
		printf("Bind failed with error code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
	HANDLE serv;
	unsigned uThreadIDs;
	serv = (HANDLE)_beginthreadex(NULL, 0,&Server, NULL, 0, &uThreadIDs);
	while (1)
	{
		//clear the buffer by filling null, it might have previously received data
		memset(buf, '\0', 255);
		//try to receive some data, this is a blocking call
		if ((recv_len = recvfrom(s, buf, 255, 0, (struct sockaddr *) &si_other, &slen)) == SOCKET_ERROR)
		{
			printf("recvfrom() failed with error code : %d", WSAGetLastError());
			exit(EXIT_FAILURE);
		}
		//print details of the client/peer and the data received
		printf("Received packet from %s:%d\n", inet_ntoa(si_other.sin_addr), ntohs(si_other.sin_port));
		printf("Data: %s\n", buf);
		ID = ID + 1;
		string new_p = to_string(portno+ID);
		if (sendto(s, new_p.c_str(), new_p.length(), 0, (struct sockaddr*) &si_other, slen) == SOCKET_ERROR)
		{
			printf("sendto() failed with error code : %d", WSAGetLastError());
			exit(EXIT_FAILURE);
		}
		HANDLE thread;
		thread = (HANDLE)_beginthreadex(NULL, 0, &Log_and_Work, (void*)ID, 0, &uThreadIDs);
		CloseHandle(thread);
	}
	CloseHandle(serv);
	return 0;
}

unsigned __stdcall Log_and_Work(void * arg)
{
	char *Name;
	char buffer[255];
	int rc, flag, i, kol, my_id;
	my_id = (int)arg;

	SOCKET newsockfd;
	struct sockaddr_in new_serv, client;
	int slen = sizeof(client);
	if ((newsockfd = socket(AF_INET, SOCK_DGRAM, 0)) == INVALID_SOCKET)
	{
		printf("Could not create socket : %d", WSAGetLastError());
	}
	//Prepare the sockaddr_in structure
	new_serv.sin_family = AF_INET;
	new_serv.sin_addr.s_addr = INADDR_ANY;
	//new_serv.sin_addr.s_addr = inet_addr("127.0.0.1");
	new_serv.sin_port = htons(portno + my_id);
	//Bind
	if (bind(newsockfd, (struct sockaddr *)&new_serv, sizeof(new_serv)) == SOCKET_ERROR)
	{
		printf("Bind failed with error code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}

	bzero(buffer, 255);
	if ((rc = recvfrom(newsockfd, buffer, 255, 0, (struct sockaddr *) &client, &slen)) == SOCKET_ERROR)
	{
		printf("recvfrom() failed with error code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
	flag = (int)buffer[0] - (int)'0';
	kol = 0;
	while (buffer[kol] != '.')
	{
		kol = kol + 1;
	}
	Name = (char*)malloc(sizeof(char) * kol);
	for (i = 1; i<kol; i++)
	{
		Name[i - 1] = buffer[i];
	}
	Name[kol - 1] = '\0';  //Вставляем нулевой символ окончания строки.
	if (Koll_Connect == 0)
	{
		Koll_Connect = Koll_Connect + 1;
		All_Connects = (struct Connect_Info*) malloc(sizeof(struct Connect_Info));
		All_Connects[0].id_con = my_id;
		All_Connects[0].i_sock = newsockfd;
		All_Connects[0].Name = Name;
		All_Connects[0].flag_dis = 0;
		All_Connects[0].client = client;
		All_Connects[0].slen = slen;
	}
	else
	{
		Koll_Connect = Koll_Connect + 1;
		All_Connects = (struct Connect_Info*) realloc(All_Connects, sizeof(struct Connect_Info)*Koll_Connect);
		All_Connects[Koll_Connect - 1].id_con = my_id;
		All_Connects[Koll_Connect - 1].i_sock = newsockfd;
		All_Connects[Koll_Connect - 1].Name = Name;
		All_Connects[Koll_Connect - 1].flag_dis = 0;
		All_Connects[Koll_Connect - 1].client = client;
		All_Connects[Koll_Connect - 1].slen = slen;
	}
	if (flag != 0)
	{
		if (Have_Croupier == 1)
		{
			sendto(newsockfd, "We already have a croupier. You will be a player.", 49, 0, (struct sockaddr*) &client, slen);
			Player(All_Connects[Koll_Connect - 1].Name, All_Connects[Koll_Connect - 1].i_sock, All_Connects[Koll_Connect - 1].id_con, All_Connects[Koll_Connect - 1].client, All_Connects[Koll_Connect - 1].slen);//Name, newsockfd, my_id
		}
		else
		{
			sendto(newsockfd, "You are a croupier.", 20, 0, (struct sockaddr*) &client, slen);
			Have_Croupier = 1;
			Croupier(All_Connects[Koll_Connect - 1].Name, All_Connects[Koll_Connect - 1].i_sock, All_Connects[Koll_Connect - 1].id_con, All_Connects[Koll_Connect - 1].client, All_Connects[Koll_Connect - 1].slen);//Name, newsockfd, my_id
		}
	}
	else
		Player(All_Connects[Koll_Connect - 1].Name, All_Connects[Koll_Connect - 1].i_sock, All_Connects[Koll_Connect - 1].id_con, All_Connects[Koll_Connect - 1].client, All_Connects[Koll_Connect - 1].slen);//Name, newsockfd, my_id
	free(Name);
}

void Croupier(char *Name, SOCKET newsockfd, int my_id, struct sockaddr_in client, int slen)
{
	char c[1], buf[60];
	int i, rc, Roll_Result;
	printf("Connect croupier: Name: %s. ID: %d.\n", Name, my_id);
	while (1)
	{
		for (i = 0; i<Koll_Connect; i++)
		{
			if (All_Connects[i].id_con == my_id)
			{
				if (All_Connects[i].flag_dis == 1)
				{
					Have_Croupier = 0;
					disconnect(newsockfd, my_id, Name);
				}
				break;
			}
		}
		rc = recvfrom(newsockfd, c, 1, 0, (struct sockaddr *) &client, &slen);
		if (rc>0)
		{
			if (c[0] == '1')
			{
				Have_Croupier = 0;
				disconnect(newsockfd, my_id, Name);
			}
			if (c[0] == '4')
			{
				if (Flag_start_hoax == 0)
				{
					Koll_Rate = 0;
					Flag_finish_hoax = 0;
					Flag_start_hoax = 1;
					printf("Hoax starts!\n");
				}
				else
					sendto(newsockfd, "The hoax already starts\n", 25, 0, (struct sockaddr *) &client, slen);
			}
			if (c[0] == '5')
			{
				if (Flag_start_hoax == 0)
					sendto(newsockfd, "But first let the hoax starts\n", 31,0, (struct sockaddr *) &client, slen);
				else
				{
					Flag_start_hoax = 0;
					Flag_finish_hoax = 1;
					srand(time(0));
					Roll_Result = rand() % 37;
					printf("Hoax finish! RollNumber=%d\n", Roll_Result);
					for (i = 0; i<Koll_Rate; i++)
					{
						int id_c;
						for (int j = 0; j<Koll_Connect; j++)
						{
							if (All_Connects[j].id_con == All_Rates[i].id_pl)
							{
								id_c = j;
								break;
							}
						}

						sendto(All_Rates[i].i_sock, "Hoax finish!\n\n", 14, 0, (struct sockaddr *) &All_Connects[id_c].client, All_Connects[id_c].slen);
						snprintf(buf, 60, "Winning number is %d\n\n", Roll_Result);
						sendto(All_Rates[i].i_sock, buf, strlen(buf), 0, (struct sockaddr *) &All_Connects[id_c].client, All_Connects[id_c].slen);
						if ((All_Rates[i].type == 'n') && (All_Rates[i].number == Roll_Result))
						{
							sendto(All_Rates[i].i_sock, "You win!\n", 9, 0, (struct sockaddr *) &All_Connects[id_c].client, All_Connects[id_c].slen);
							snprintf(buf, 60, "Your prize is %d$\n\n", All_Rates[i].summa * 35);
							sendto(All_Rates[i].i_sock, buf, strlen(buf), 0, (struct sockaddr *) &All_Connects[id_c].client, All_Connects[id_c].slen);
						}
						else
							if ((All_Rates[i].type == 'e') && (Roll_Result % 2 == 0))
							{
								sendto(All_Rates[i].i_sock, "You win!\n", 9, 0, (struct sockaddr *) &All_Connects[id_c].client, All_Connects[id_c].slen);
								snprintf(buf, 60, "Your prize is %d$\n\n", All_Rates[i].summa);
								sendto(All_Rates[i].i_sock, buf, strlen(buf), 0, (struct sockaddr *) &All_Connects[id_c].client, All_Connects[id_c].slen);
							}
							else
								if ((All_Rates[i].type == 'o') && (Roll_Result % 2 == 1))
								{
									sendto(All_Rates[i].i_sock, "You win!\n", 9, 0, (struct sockaddr *) &All_Connects[id_c].client, All_Connects[id_c].slen);
									snprintf(buf, 60, "Your prize is %d$\n\n", All_Rates[i].summa);
									sendto(All_Rates[i].i_sock, buf, strlen(buf), 0, (struct sockaddr *) &All_Connects[id_c].client, All_Connects[id_c].slen);
								}
								else
									sendto(All_Rates[i].i_sock, "Sorry, you lose.\n\n", 18, 0, (struct sockaddr *) &All_Connects[id_c].client, All_Connects[id_c].slen);
					}
					if (Koll_Rate != 0)
						free(All_Rates);
				}

			}
		}
	}
}

void Player(char *Name, SOCKET newsockfd, int my_id, struct sockaddr_in client, int slen)
{
	int i, rc, Flag_Rate;
	char c[1], buf[255];
	unsigned long on = 1;
	ioctlsocket(newsockfd, FIONBIO, &on);
	printf("Connect player: Name: %s. ID: %d.\n", Name, my_id);
	while (1)
	{
		sendto(newsockfd, "Waiting the start of hoax\n", 26, 0, (struct sockaddr *) &client, slen);
		while (Flag_start_hoax == 0)
		{
			for (i = 0; i<Koll_Connect; i++)
			{
				if (All_Connects[i].id_con == my_id)
				{
					if (All_Connects[i].flag_dis == 1)
						disconnect(newsockfd, my_id, Name);
					break;
				}
			}

			rc = recvfrom(newsockfd, c, 1, 0, (struct sockaddr *) &client, &slen);
			if (rc>0)
			{
				if (c[0] == '1')
				{
					disconnect(newsockfd, my_id, Name);
				}
				if (c[0] == '2')
				{
					cout << "work";
					sendto(newsockfd, "The hoax didn't start yet\n", 27, 0, (struct sockaddr *) &client, slen);
				}
				if (c[0] == '3')
				{
					sendto(newsockfd, "The hoax didn't start yet\n", 27, 0, (struct sockaddr *) &client, slen);
					recvfrom(newsockfd, buf, 255, 0, (struct sockaddr *) &client, &slen);
					bool flag = true;
					do{
						if ((buf[0] == 'n') && flag)
						{
							do{
								recvfrom(newsockfd, buf, 255, 0, (struct sockaddr *) &client, &slen);
							} while (buf[0] != '.');
							flag = false;
						}
						recvfrom(newsockfd, buf, 255, 0, (struct sockaddr *) &client, &slen);
					} while (buf[0] != '.');
					recvfrom(newsockfd, buf, 255, 0, (struct sockaddr *) &client, &slen);
					bzero(buf, 255);
				}
			}
		}
		sendto(newsockfd, "Hoax starts!\n", 13, 0, (struct sockaddr *) &client, slen);
		Flag_Rate = 0;
		while (Flag_finish_hoax == 0)
		{
			for (i = 0; i<Koll_Connect; i++)
			{
				if (All_Connects[i].id_con == my_id)
				{
					if (All_Connects[i].flag_dis == 1)
						disconnect(newsockfd, my_id, Name);
					break;
				}
			}
			rc = recvfrom(newsockfd, c, 1, 0, (struct sockaddr *) &client, &slen);
			if (rc>0)
			{
				if (c[0] == '1')
				{
					disconnect(newsockfd, my_id, Name);
				}
				if (c[0] == '2')
				{
					Watch_Rate(newsockfd, my_id);
				}
				if (c[0] == '3')
				{
					if (Flag_Rate == 0)
					{
						Do_Rate(newsockfd, my_id, Name);
						Flag_Rate = 1;
					}
					else
					{
						sendto(newsockfd, "You already rate! Wait results.\n", 33, 0, (struct sockaddr *) &client, slen);
						recvfrom(newsockfd, buf, 255, 0, (struct sockaddr *) &client, &slen);
						bool flag = true;
						do{
							if ((buf[0] == 'n') && flag)
							{
								do{
									recvfrom(newsockfd, buf, 255, 0, (struct sockaddr *) &client, &slen);
								} while (buf[0] != '.');
								flag = false;
							}
							recvfrom(newsockfd, buf, 255, 0, (struct sockaddr *) &client, &slen);
						} while (buf[0] != '.');
						recvfrom(newsockfd, buf, 255, 0, (struct sockaddr *) &client, &slen);
						bzero(buf, 255);
					}
				}
			}
		}
	}
}
void disconnect(SOCKET newsockfd, int my_id, char *Name)
{
	int flag, i, KolDis;

	for (i = 0; i<Koll_Connect; i++)
	{
		if (All_Connects[i].id_con == my_id)
		{
			KolDis = i;
			break;
		}
	}
	sendto(newsockfd, "You disconected\n", 17, 0, (struct sockaddr *) &All_Connects[KolDis].client, All_Connects[KolDis].slen);
	if (Koll_Connect != 1)
	{
		for (i = KolDis; i<Koll_Connect - 1; i++)
		{
			All_Connects[i].id_con = All_Connects[i + 1].id_con;
			All_Connects[i].i_sock = All_Connects[i + 1].i_sock;
			All_Connects[i].Name = All_Connects[i + 1].Name;
			All_Connects[i].flag_dis = All_Connects[i + 1].flag_dis;
			All_Connects[i].client = All_Connects[i + 1].client;
			All_Connects[i].slen = All_Connects[i + 1].slen;
		}
		Koll_Connect = Koll_Connect - 1;
		All_Connects = (struct Connect_Info*) realloc(All_Connects, sizeof(struct Connect_Info)*Koll_Connect);
	}
	else
	{
		Koll_Connect = 0;
		free(All_Connects);
		All_Connects = NULL;
	}

	printf("Disconnected: Name: %s ID:%d\n", Name, my_id);
	flag = shutdown(newsockfd, SD_BOTH);
	if (flag < 0) {
		perror("ERROR shutdown socket");
		exit(1);
	}
	flag = closesocket(newsockfd);
	if (flag < 0) {
		perror("ERROR close socket");
		exit(1);
	}
	free(Name);
}
void Watch_Rate(SOCKET newsockfd, int my_id)
{
	int Id_c;
	for (int i = 0; i<Koll_Connect; i++)
	{
		if (All_Connects[i].id_con == my_id)
		{
			Id_c = i;
			break;
		}
	}
	char buf[500];//,message[105];
	int i;
	//snprintf(message, 105, "RATES\nType:\n  e-even\n  o-odd\n  n-number(after that write the number)\nName | ID | Type | SUMM\n\n");
	//write(newsockfd, message, strlen(message));
	for (i = 0; i<Koll_Rate; i++)
	{
		//write(newsockfd, All_Rates[i].Name,strlen(All_Rates[i].Name));
		//write(newsockfd, " ",1);
		//snprintf(buf, 60, "%d", All_Rates[i].id_pl);
		//write(newsockfd, buf, strlen(buf));
		//write(newsockfd, " ",1);
		//write(newsockfd, All_Rates[i].type,1);
		//write(newsockfd, " ",1);
		//string message="";
		if (All_Rates[i].type == 'n')
			snprintf(buf, 500, "%s %d %c %d %d$\n", All_Rates[i].Name, All_Rates[i].id_pl, All_Rates[i].type, All_Rates[i].number, All_Rates[i].summa);
		else
			snprintf(buf, 500, "%s %d %c %d$\n", All_Rates[i].Name, All_Rates[i].id_pl, All_Rates[i].type, All_Rates[i].summa);
		sendto(newsockfd, buf, strlen(buf), 0, (struct sockaddr *) &All_Connects[Id_c].client, All_Connects[Id_c].slen);
	}
}
void Do_Rate(SOCKET newsockfd, int my_id, char *Name)
{
	int Id_c;
	for (int i = 0; i<Koll_Connect; i++)
	{
		if (All_Connects[i].id_con == my_id)
		{
			Id_c = i;
			break;
		}
	}
	char type[2], c[1];
	int summa, n, number;
	recvfrom(newsockfd, type, 1, 0, (struct sockaddr *) &All_Connects[Id_c].client, &All_Connects[Id_c].slen);
	//cout << type << endl<<"*******"<<endl;
	Sleep(100);
	summa = 0;
	recvfrom(newsockfd, c, 1, 0, (struct sockaddr *) &All_Connects[Id_c].client, &All_Connects[Id_c].slen);
	//cout << c[0] << endl;
	Sleep(100);
	do{
		if ((c[0] == 'n') || (c[0] == 'e') || (c[0] == 'o'))
		{
			type[0] = c[0];
			bzero(c, 1);
			recvfrom(newsockfd, c, 1, 0, (struct sockaddr *) &All_Connects[Id_c].client, &All_Connects[Id_c].slen);
		}
		else
		{
			summa = summa * 10;
			n = (int)c[0] - (int)'0';
			summa = summa + n;
			recvfrom(newsockfd, c, 1, 0, (struct sockaddr *) &All_Connects[Id_c].client, &All_Connects[Id_c].slen);
			//cout << c << endl;
			Sleep(100);
		}
	} while (c[0] != '.');
	if (type[0] == 'n')
	{
		number = 0;
		recvfrom(newsockfd, c, 1, 0, (struct sockaddr *) &All_Connects[Id_c].client, &All_Connects[Id_c].slen);
		do{
			number = number * 10;
			n = (int)c[0] - (int)'0';
			number = number + n;
			recvfrom(newsockfd, c, 1, 0, (struct sockaddr *) &All_Connects[Id_c].client, &All_Connects[Id_c].slen);
		} while (c[0] != '.');
	}
	type[1] = '\0';
	if (type[0] == 'n')
		printf("New Rate from %s: Type %s Summ %d Number %d\n", Name, type, summa, number);
	else
		printf("New Rate from %s: Type %s Summ %d\n", Name, type, summa);
	Koll_Rate = Koll_Rate + 1;
	if (Koll_Rate == 1)
		All_Rates = (struct Rates_Info*) malloc(sizeof(struct Rates_Info));
	else
		All_Rates = (struct Rates_Info*) realloc(All_Rates, sizeof(struct Rates_Info)*Koll_Rate);
	All_Rates[Koll_Rate - 1].id_pl = my_id;
	All_Rates[Koll_Rate - 1].Name = Name;
	All_Rates[Koll_Rate - 1].i_sock = newsockfd;
	All_Rates[Koll_Rate - 1].type = type[0];
	All_Rates[Koll_Rate - 1].summa = summa;
	All_Rates[Koll_Rate - 1].number = number;
}
unsigned __stdcall Server(void * arg)
{
	int i, dis_id, n, flag;
	char buf[40];
	dis_id = 0;
	while (1)
	{
		scanf("%s", buf);
		flag = 0;
		if ((buf[0] == 'd') && (buf[9] == 't') && (buf[10] == '_'))
		{
			dis_id = 0;
			for (i = 11; buf[i] != '.'; i++)
			{
				dis_id = dis_id * 10;
				n = (int)buf[i] - (int)'0';
				if ((n<0) || (n>9))
				{
					flag = 1;
					break;
				}
				dis_id = dis_id + n;
			}
			if (flag == 1)
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
	int i, KolDis;
	KolDis = -1;
	for (i = 0; i<Koll_Connect; i++)
	{
		if (All_Connects[i].id_con == my_id)
		{
			KolDis = i;
			break;
		}
	}
	if (KolDis == -1)
	{
		printf("Error: Cant find client with this ID\n");
		return;
	}
	All_Connects[KolDis].flag_dis = 1;
}