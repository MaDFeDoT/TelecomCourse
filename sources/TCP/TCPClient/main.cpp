#define _WINSOCK_DEPRECATED_NO_WARNINGS
#include <time.h>
#include<stdio.h>
#include<winsock2.h>
#include<WS2tcpip.h>
#include<iostream>
#include<string>
using namespace std;
#pragma comment(lib,"ws2_32.lib")
#pragma comment (lib, "Mswsock.lib")
#pragma comment (lib, "AdvApi32.lib")

DWORD WINAPI threadHandler(LPVOID);
bool have_connect = false;
int main(int argc, char *argv[])
{	
	WSADATA wsa;
	SOCKET s;
	struct sockaddr_in server;

	if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0)
	{
		printf("Failed. Error Code : %d\n", WSAGetLastError());
		return 1;
	}
	while (1)
	{
		bool start=true;
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
		if ((s = socket(AF_INET, SOCK_STREAM, 0)) == INVALID_SOCKET)
		{
			printf("Could not create socket : %d\n", WSAGetLastError());
		}
		start = true;
		while (start)
		{
			/*cout << "Enter server IP adress: ";
			string ip_adress;
			cin >> ip_adress;
			cout << "Enter number of port: ";
			int port;
			cin >> port;
			server.sin_addr.s_addr = inet_addr(ip_adress.c_str());
			server.sin_family = AF_INET;
			server.sin_port = htons(port);*/

			server.sin_addr.s_addr = inet_addr("192.168.56.102");
			server.sin_family = AF_INET;
			server.sin_port = htons(5001);
			//Connect to remote server
			if (connect(s, (struct sockaddr *)&server, sizeof(server)) < 0)
			{
				puts("Connect error\n");
			}
			else
				start = false;
		}
		bool flag_croupier = false;
		bool flag = true;
		string mes_to_serv;
		char user_a;
		int sleep_time=100;

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
		if (send(s, mes_to_serv.c_str(), mes_to_serv.length(), 0) < 0)
		{
			puts("Send failed");
			return 1;
		}
		char server_reply[2000];
		int recv_size;
		if ((recv_size = recv(s, server_reply, 2000, 0)) == SOCKET_ERROR)
		{
			puts("recv failed");
		}
		server_reply[recv_size] = '\0';
		cout << "Server:  " << server_reply << "\n";
		std::string buffer(server_reply);
		if (buffer.find("We already have a croupier.") != string::npos)
			flag_croupier = false;

		have_connect = true;
		HANDLE t;
		t = CreateThread(NULL, 0, threadHandler, (LPVOID)s, 0, NULL);

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
							if (send(s, mes_to_serv.c_str(), mes_to_serv.length(), 0) < 0)
							{
								puts("Send failed\n");
							};
							have_connect = false;
							Sleep(sleep_time);
							break;
				case '2':	mes_to_serv = "2";
							if (send(s, mes_to_serv.c_str(), mes_to_serv.length(), 0) < 0)
							{
								puts("Send failed\n");
							};
							Sleep(sleep_time);
							break;
				case '3':	mes_to_serv = "4";
							if (send(s, mes_to_serv.c_str(), mes_to_serv.length(), 0) < 0)
							{
								puts("Send failed\n");
							};
							Sleep(sleep_time);
							break;
				case '4':	mes_to_serv = "5";
							if (send(s, mes_to_serv.c_str(), mes_to_serv.length(), 0) < 0)
							{
								puts("Send failed\n");
							};
							Sleep(sleep_time);
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
				switch (user_a)
				{
				case '1':	mes_to_serv = "1";
							if (send(s, mes_to_serv.c_str(), mes_to_serv.length(), 0) < 0)
							{
								puts("Send failed\n");
							};
							have_connect = false;
							Sleep(sleep_time);
							break;
				case '2':	mes_to_serv = "2";
							if (send(s, mes_to_serv.c_str(), mes_to_serv.length(), 0) < 0)
							{
								puts("Send failed\n");
							};
							Sleep(sleep_time);
							break;
				case '3':	while (true_rate)
							{
								cout << "Choose the type of rate:\n";
								cout << "	e - even\n	o - odd\n	n - number\n";
								cin >> rate_type;
								switch (rate_type)
								{
								case 'e': mes_to_serv = "3e"; true_rate = 0; break;
								case 'o': mes_to_serv = "3o"; true_rate = 0; break;
								case 'n': mes_to_serv = "3n"; true_rate = 0; break;
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
							if (send(s, mes_to_serv.c_str(), mes_to_serv.length(), 0) < 0)
							{
								puts("Send failed\n");
							};
							Sleep(sleep_time);
							break;
				default: cout << "That's not a choice.\n";
				}
			}
		}
	}
}
DWORD WINAPI threadHandler(LPVOID param){
	char server_reply[2000];
	int recv_size;
	SOCKET s = (SOCKET)param;
	while (have_connect)
	{
		if ((recv_size = recv(s, server_reply, 2000, 0)) == SOCKET_ERROR)
		{
			puts("recv failed");
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