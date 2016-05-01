package smtpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import java.net.*;

public class SMTPClient {

    private Socket clientsocket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    
    // Соединение с SMTP-сервером
    public void Connect(String host) throws UnknownHostException, IOException{
            
            String reply = null;
            try{
                
                clientsocket = new Socket(host, 25);
               
                in  = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
                out = new PrintWriter(clientsocket.getOutputStream(),true);
                reply = Reply();
                System.out.println(reply);
                
                if(reply==null){
                    System.out.println("Error: Connect!");
                    System.exit(-1);
                }    
                if (reply.substring(0, 3).equals("220")){
                    System.out.println("Connect\n");
                }
                else System.out.println("Error Connect");
            }catch(IOException ex) {
                System.out.println("Error Connect");
                System.exit(-1);
             }

    }
        
    // Передача серверу информации о домене пользователя
    public void Helo(String domainUser){
        String reply;
        try{
            reply=SendCommand("HELO " + domainUser);
            System.out.println(reply);
            if(reply.substring(0, 3).equals("250")){
                System.out.println("HELO\n");
            }
            else System.exit(-1);
        }catch(IOException ex) {
            System.out.println("Error  HELO\n");
            System.exit(-1);
        }
    }
    
    // Аутентификация
    public void Auth(String login, String password) {
        String reply, cod;
        try{
            reply=SendCommand("AUTH LOGIN");
            System.out.println(reply);
            if(reply.substring(0, 3).equals("334")){
                //cod="boytsev.andrey@rambler.ru";
                cod= new BASE64Encoder().encode(login.getBytes());
                reply=SendCommand(cod);
                System.out.println(reply);
                if(!reply.substring(0, 3).equals("334")){
                    System.exit(-1);
                }
                //cod="i1r9i9n4a";
                cod= new BASE64Encoder().encode(password.getBytes());
                reply=SendCommand(cod);
                System.out.println(reply);
                if(!reply.substring(0, 3).equals("235")){
                    System.exit(-1);
                }
            }
            else System.exit(-1);
        }catch(IOException ex) {
            System.out.println("Error\n");
            System.exit(-1);
        }
    }
    
    // Передача серверу адреса отправителя письма
    public void Mailfrom(String mailclient){
        String reply;
        try{
            reply=SendCommand("MAIL FROM: " + mailclient);
            System.out.println(reply);
            if(reply.substring(0, 3).equals("250")){
                System.out.println("MAIL FROM\n");
            }
            else{
                System.out.println("Error  MAIL FROM\n");
            }
        }catch(IOException ex) {
            System.out.println("Error\n");
            System.exit(-1);
        }
    }
    
    // Передача серверу адреса получателя письма
    public void Rcptto(String maildest){
        String reply;
        try{
            reply=SendCommand("RCPT TO: " + maildest);
            System.out.println(reply);
            if(reply.substring(0, 3).equals("250")){
                System.out.println("RCPT TO\n");
            }
            else{
                System.out.println("Error  RCPT TO\n");
            }
        }catch(IOException ex) {
            System.out.println("Error  RCPT TO\n");
            System.exit(-1);
        }
    }
    
    // Передача серверу тела письма
    public void Data(String from, String to, String subject, String dat){
        String reply;
        String msg;
        try{
            msg="From: "+from+"\nTo: "+to+"\nSubject: "+subject+"\nContent-Type: text/plain; charset=ISO-8859-1\nContent-Transfer-Encoding: base64\n"+dat+"\n\n.";
            //System.out.println(msg);
            reply=SendCommand("DATA ");
            System.out.println(reply);
            if(reply.substring(0, 3).equals("354")){
                reply=SendCommand(msg);
                if(reply.substring(0, 3).equals("250")){
                    System.out.println(reply);
                }
            }
            else{
                System.out.println("Error  DATA\n");
            }
        }catch(IOException ex) {
            System.out.println("Error\n");
            System.exit(-1);
        }
    }
    
    // Завершение сеанса связи
    public void Quit() {
        String reply;
        try{
            reply=SendCommand("QUIT");
            System.out.println(reply);
            if(reply.substring(0, 3).equals("221")){
                System.out.println("QUIT\n");
                in.close();
                out.close();
                clientsocket.close();
            }
            else{
                System.out.println("Error  QUIT");
            }
        }catch(IOException ex) {
            System.out.println("Error");
            System.exit(-1);
        }
    }
    
    //Чтение ответа от SMTP-сервера
    public String Reply() {
        String reply = null;
        try {
            reply = in.readLine();
        } catch (IOException e) {
           System.out.println("System Error: read!!");
           System.exit(-1);
        } finally {
            return reply;
        }
    }
    
    //Посылка команды ПОП3-серверу
    public String SendCommand(String command) throws IOException {
        out.println(command);
        return Reply(); // ответ сервера на команду  
    }
   


    
}
