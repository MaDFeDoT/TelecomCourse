package smtpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException{
                
        System.out.println("SMTP-клиент\n");
        BufferedReader inu;
        inu  = new BufferedReader(new InputStreamReader(System.in));
        String fuser=null, log=null, passwd=null, from=null, to=null, subject=null;
        SMTPClient smtp = new SMTPClient();
        
        while(true){
            System.out.println(">Введите домен SMTP-сервера:");
            //if ((fuser = inu.readLine())!=null){
                //smtp.Connect(fuser);
                smtp.Connect("0.0.0.0");
            //}

            System.out.println(">Введите ваш домен:");
            //if ((fuser = inu.readLine())!=null){
                //smtp.Helo(fuser);
                smtp.Helo("localhost");

            System.out.println(">Введите ваш login:");
            if ((fuser = inu.readLine())!=null){
                log=fuser;
                System.out.println(">Введите ваш password:");
                if ((fuser = inu.readLine())!=null){
                    passwd=fuser;
                    smtp.Auth(log, passwd);
                }
            }

            System.out.println(">Введите адрес отправителя:");
            if ((fuser = inu.readLine())!=null){
                from=fuser;
                smtp.Mailfrom(fuser);
            }

            System.out.println(">Введите адрес получателя:");
            if ((fuser = inu.readLine())!=null){
                to=fuser;
                smtp.Rcptto(fuser);
            }

            System.out.println(">Введите тему письма:");
            if ((fuser = inu.readLine())!=null){
                subject=fuser;
                System.out.println(">Введите письмо:");
                if ((fuser = inu.readLine())!=null){
                smtp.Data(from, to, subject, fuser);
                }
            }
            System.out.println("****");
            smtp.Quit();
        }
        }     
    }