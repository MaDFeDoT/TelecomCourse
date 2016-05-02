package Commands;

import DBHandler.DB;
import Server.IncomingServer;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;


public class FetchCommand extends IncomingServer {

    public String prefix;
    public String request;
    public DB assistant;
    PrintWriter out;
    public String folder;
    public int UserId;

    /**
     * Конструктор FetchCommand
     * @param host_port - порт на котором работает сервер
     */
    public FetchCommand (int host_port) {//default constructor
        super(host_port);
    }

    /**
     * Конструктор FetchCommand
     * @param prefix - префикс команды
     * @param request - строка запроса
     * @param assistant - объект для работы с бд
     * @param out - поток для ответа
     * @param folder - рабочая папка
     * @param UserId - ID пользователя
     */
    public FetchCommand (String prefix, String request, DB assistant,  PrintWriter out,  String folder, int UserId) {//no-args constructor
        this.prefix = prefix;
        this.request = request;
        this.assistant = assistant;
        this.out = out;
        this.folder = folder;
        this.UserId = UserId;
    }

    /**
     * Выполнение команды
     * Разные клиенты присылают разного вида запросы. Логика обработки запросов описана в комментариях
     * @see Server.IncomingServer#SendAndPrint(String, java.io.PrintWriter)
     */
    public void DoCommand()  {
        String startUID = "";
        String endUID = ""; //see next comment
        Boolean sequence = false; // if we need to fetch something like 1:5 or 1:*
        Boolean allUID = false;// if we need to fetch :*
        Boolean uidIsVector = false;  // if we need to fetch 2,3,4

        Vector<String> vecUID = new Vector <String>();

        int separator = request.indexOf(':');
        if (separator > 0) //we have sequence
        {
            sequence = true;
            separator --;
            while (request.charAt(separator) != ' ')
            {
                startUID = request.charAt(separator) + startUID;
                separator --;
            }

            separator = request.indexOf(':')+ 1;

            if (request.charAt(separator) == '*')//if all UIDs
            {
                allUID = true;
            }
            else
            {
                while (request.charAt(separator) != ' ')
                {
                    endUID = request.charAt(separator) + endUID;
                    separator ++;
                }
            }
        }
        else //we need only 1 or a few
        {
            //if 1
            separator = request.indexOf (',');
            if (separator <= 0 ) //if only one
            {
                separator = request.indexOf ('H') + 2; // proplems with low case

                while (request.charAt(separator) != ' ')
                {
                    startUID += request.charAt(separator) ;
                    separator ++;
                }
            }
            else  //if a few
            {
                uidIsVector = true;
                String tempUID = "";
                separator --;
                while (request.charAt(separator) != ' ')
                {
                    tempUID = request.charAt(separator) + tempUID;
                    separator --;
                }
                vecUID.add(tempUID);
                separator = request.indexOf(',')+1;
                tempUID = "";
                while (request.charAt(separator) != ' ')
                {
                    if (request.charAt(separator) !=',')
                    {
                        tempUID  = request.charAt(separator) + tempUID;
                        separator ++;
                    }
                    else
                    {
                        vecUID.add(tempUID);
                        separator ++;
                        tempUID = "";
                    }
                }
                vecUID.add(tempUID);
            }
        }

        if (sequence)
        {
            int startuid = Integer.valueOf(startUID);
            String userId = Integer.toString(UserId);
            ResultSet resultSet = assistant.CountExistMessages(userId,folder);
            int count = 0;
            try {
                if (resultSet.next())
                {
                    count = Integer.valueOf(resultSet.getString("COUNT"));
                }
                else
                {
                    System.out.println("ERRROR");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


            if (allUID)
            {
                if (count <= 0)
                {
                    SendAndPrint(prefix + " No FETCH no one message", out);
                    return;
                }

                for ( int i = startuid ; i<= count; i++)
                {
                    try {
                        GenAndSendFetchResponse(request,Integer.toString(i),sequence);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                int enduid = Integer.valueOf(endUID);
                int finishUID;
                if (enduid <= count)
                {
                    finishUID = enduid;
                }
                else
                {
                    SendAndPrint(prefix + " No FETCH in folder only " + count + " messages", out);
                    return;
                }
                for ( int i = startuid ; i<= finishUID; i++)
                {
                    try {
                        GenAndSendFetchResponse(request,Integer.toString(i),sequence);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        else
        {
            if (uidIsVector) //if 2,3,4
            {
                for (int i = 0; i< vecUID.size(); i++)
                {
                    try {
                        GenAndSendFetchResponse(request,vecUID.get(i),sequence);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            else // if 1
            {
                try {
                    GenAndSendFetchResponse(request,startUID,sequence);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

       SendAndPrint(prefix + " OK FETCH completed", out);
    }

    /**
     * Генерирование ответа на Fetch запрос
     * @param request - строка запроса
     * @param UID
     * @param sequence - если сообщений несколько
     * @throws SQLException - при ошибке работы с БД
     */
    public void GenAndSendFetchResponse (String request, String UID, Boolean sequence) throws SQLException {

        Boolean  needbracket = true;
        int separator = 10; //this number is roughly
        //open message
        MimeMessage message = OpenMessage (UID);

        String response = "* " + UID + " FETCH (";
        if (sequence)
        {
            response += "UID " + UID + " ";
        }

        //If UID command in Fetch
        if (request.indexOf("UID", separator)!=-1)
            response += "UID " + UID + " ";

        //If RFC822.SIZE command in Fetch
        if (request.indexOf("RFC822.SIZE", separator)!=-1)
            try {
                response += "RFC822.SIZE " + message.getSize() + " ";
            } catch (MessagingException e) {
                e.printStackTrace();
            }

        //If INTERNALDATE command in Fetch
        //INTERNALDATE = senddate but in real = recivedate
        if (request.indexOf("INTERNALDATE", separator)!=-1)
            try {
                response += "INTERNALDATE \"" + message.getSentDate() + "\" ";
            } catch (MessagingException e) {
                e.printStackTrace();
            }

        //If FLAGS command in Fetch
        if (request.contains("FLAGS"))
        {
            ResultSet resultSet = assistant.GetMessageFlags(Integer.toString(UserId), folder, UID);
            String space = "";
            if (resultSet.next())
            {
                response += "FLAGS (";
                if ((Integer.parseInt(resultSet.getString("SEEN"))) == 1)
                {
                    response += "\\Seen";
                    space = " ";
                }
                if ((Integer.parseInt(resultSet.getString("ANSWERED"))) == 1)
                {
                    response += space + "\\Answered";
                    space = " ";
                }
                if ((Integer.parseInt(resultSet.getString("DELETED"))) == 1)
                {
                    response += space + "\\Deleted";
                    space = " ";
                }
                if ((Integer.parseInt(resultSet.getString("DRAFT"))) == 1)
                {
                    response += space + "\\Draft";
                    space = " ";
                }
                if ((Integer.parseInt(resultSet.getString("Recent"))) == 1)
                {
                    response += space + "\\Recent";
                    space = " ";
                }
                response += ") ";
            }
        }

        //If ENVELOPE command in Fetch
        if (request.indexOf("ENVELOPE", separator)!=-1)
        {
           // EnvelopeCommand env = new EnvelopeCommand(message);
            String envelope = null;
            try {
                envelope = getEnvelop(message);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            response += envelope + " ";
        }

        //If ENVELOPE command in Fetch
        if (request.indexOf("BODYSTRUCTURE", separator)!=-1)
        {
            //BodyStructureCommand bod = new BodyStructureCommand(message);
            String bodyStructure = null;
            try {
                bodyStructure = GetBodyStructure(message);
                bodyStructure.toUpperCase();
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            response += bodyStructure;
        }

        if (request.indexOf("BODY.PEEK[HEADER.FIELDS", separator)!=-1 || request.indexOf("BODY.PEEK[]", separator)!=-1)
        {
            needbracket = false;
            String emlFile = "D:\\mail\\" + UID + ".eml";
            String strLine = null;
            String line = "";
            try{
                // Open the file that is the first
                // command line parameter
                FileInputStream fstream = new FileInputStream(emlFile);
                // Get the object of DataInputStream
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                //Read File Line By Line

                while ((strLine = br.readLine()) != null)   {
                    line += strLine + " ";
                    // Print the content on the console
                }
                //Close the input stream
                in.close();
            }catch (Exception e){//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

            response += "BODY.PEEK[] {" + line.length() + "} \r\n";

            try {
                InternetAddress[] addrFrom = (InternetAddress[]) message.getFrom();
                if (addrFrom !=null)
                {
                    response += "From: ";
                    String space = "";
                    for (int i = 0; i<  addrFrom.length; i++)
                    {
                        response +=  space + addrFrom[i].getPersonal() + " <" + addrFrom[i].getAddress() + ">";
                        space = ", ";
                    }
                    response += "\r\n";
                }

                InternetAddress[] adrrTo = (InternetAddress[]) message.getRecipients(MimeMessage.RecipientType.TO);
                if (adrrTo!=null)
                {
                    response += "To: ";
                    String space = "";
                    for (int i = 0; i<  adrrTo.length; i++)
                    {
                        //ПОТОМ ИСПРАВИТЬ!
                        //response +=  space + adrrTo[i].getPersonal() + " " + adrrTo[i].getAddress();
                        response +=  adrrTo[i].getAddress();
                        space = ", ";
                    }
                    response += "\r\n";
                }

                InternetAddress[] adrrCc = (InternetAddress[]) message.getRecipients(MimeMessage.RecipientType.CC);
                if (adrrCc!=null)
                {
                    response += "Cc: ";
                    String space = "";
                    for (int i = 0; i<  adrrCc.length; i++)
                    {
                        response +=  space + adrrCc[i].getPersonal() + " " + adrrCc[i].getAddress();
                        space = ", ";
                    }
                    response += "\r\n";
                }

                InternetAddress[] adrrBcc = (InternetAddress[]) message.getRecipients(MimeMessage.RecipientType.BCC);
                if (adrrBcc!=null)
                {
                    response += "Bcc: ";
                    String space = "";
                    for (int i = 0; i<  adrrBcc.length; i++)
                    {
                        response +=  space + adrrBcc[i].getPersonal() + " " + adrrBcc[i].getAddress();
                        space = ", ";
                    }
                    response += "\r\n";
                }

                response += "Subject: " + message.getSubject() + "\r\n";

                response += "Date: " + message.getSentDate() + "\r\n";

                response += "Message-ID: " + message.getMessageID() + "\r\n";

                response += "Content-Type: " + message.getContentType() + "\r\n";

            } catch (MessagingException e) {
                e.printStackTrace();
            }
    }


        if (request.indexOf("BODY[]", separator)!=-1)
        {
            needbracket = false;
            String emlFile = "D:\\mail\\" + UID + ".eml";
            String strLine = null;
            String line = "";
            try{
                // Open the file that is the first
                // command line parameter
                FileInputStream fstream = new FileInputStream(emlFile);
                // Get the object of DataInputStream
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                //Read File Line By Line

                while ((strLine = br.readLine()) != null)   {
                    line += strLine + "\n";
                    // Print the content on the console
                }
                //Close the input stream
                in.close();
            }catch (Exception e){//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

            response += "BODY[]{" + line.length() + "}\n"  +line + "\n";


        }

        response += ")";

        SendAndPrint(response, out);

    }

    /**
     * Полученния envelop-контента сообщения
     * @param message - сообщения
     * @return - контент
     * @throws MessagingException - ошибка в сообщении
     */
    public String getEnvelop (MimeMessage message) throws MessagingException
    {
        String envelope ="ENVELOPE (\"" + message.getSentDate() + "\" " + //date
                "\"" + message.getSubject() + "\" "; //subject

        //from
        InternetAddress[] adrFrom = (InternetAddress[]) message.getFrom();
        String from = parsAdressMass(adrFrom);
        envelope += from + " " ;

        //sender
        envelope += from + " " ;

        //reply-to
        InternetAddress[] adrReplyTo = (InternetAddress[]) message.getReplyTo();
        String replyTo = parsAdressMass(adrReplyTo);
        envelope += replyTo + " ";

        //to
        InternetAddress[] adrrTo = (InternetAddress[]) message.getRecipients(MimeMessage.RecipientType.TO);
        String to = parsAdressMass(adrrTo);
        envelope += to + " ";

        //cc
        InternetAddress[] adrrCc = (InternetAddress[]) message.getRecipients(MimeMessage.RecipientType.CC);
        String cc = parsAdressMass(adrrCc);
        if (cc!=null)
            envelope += cc ;
        else
            envelope += "NIL";

        //bcc
        InternetAddress[] adrrBcc = (InternetAddress[]) message.getRecipients(MimeMessage.RecipientType.BCC);
        String bcc = parsAdressMass(adrrBcc);
        if (bcc!=null)
            envelope +=  " " + bcc ;
        else
            envelope += " NIL";

        //in-reply-to??
        envelope += " NIL";

        envelope += " \"" + message.getMessageID() + "\")";
        return  envelope;
    }

    /**
     * Парсер адреса из InternetAddress в String
     * @param adr - адрес InternetAddress
     * @return - адрес String
     */
    public String parsAdress (InternetAddress adr )
    {
        String line = "";
        String person;
        String adress;
        String name;
        String host;

        try {
            person = adr.getPersonal();
            if (person == null)
                person = "NILL";
            else
                person = "\"" + person + "\"";
        } catch (NullPointerException e) {
            person = "NILL";
        }

        try {
            adress = adr.getAddress();
            adress = "\"" + adress + "\"";
        } catch (NullPointerException e) {
            adress = "NILL";
        }

        try {
            name = adress.substring(0,adress.indexOf('@'));
            name =  name + "\"";
        } catch (StringIndexOutOfBoundsException e) {
            name = "NILL";
        }

        try {
            host = adress.substring(adress.indexOf('@')+1,adress.length());
            host = "\"" + host;
        } catch (StringIndexOutOfBoundsException e) {
            host = "NILL";
        }

        line = "(" + person + " NILL " + name + " " + host + ")";

        return  line;
    }

    /**
     * Парсер массива адресов из InternetAddress в String
     * @param adr - массив адресов InternetAddress
     * @return - адреса String
     */
    public String parsAdressMass (InternetAddress[] adr )
    {
        String adressLine = "(";
        String from ="";
        try {
            if (adr.length >=0)
            {
                String line;
                for (int i = 0; i<adr.length;i++)
                {
                    from = parsAdress (adr[i]);
                    adressLine += from;
                }
            }
            else
                adressLine += "(NILL NILL NILL NILL)";
            adressLine += ")";
        } catch (NullPointerException e) //if empty field
        {
            return null;
        }

        return  adressLine;
    }

    /**
     * Получения тела сообщения
     * @param message - сообщение
     * @return - тело сообщения
     * @throws MessagingException - ошибка в письме
     * @throws IOException - ошибка работы с файлом
     */
    public String GetBodyStructure (MimeMessage message) throws MessagingException, IOException
    {
        String contentType = message.getContentType();
        String ans = "BODY (\"";
        int nextIndex = 0;

        //get params
        for (int i = 0; i< contentType.length(); i++)
        {
            if (contentType.charAt(i) != '/')
            {
                if (contentType.charAt(i) ==';')
                {
                    i+= 2;
                    ans +="\"";
                    nextIndex = i;
                    break;
                }

                else
                    ans += contentType.charAt(i);
            }
            else
                ans +="\" \"";
        }

        ans+= " (\"";

        for (int i = nextIndex; i<contentType.length(); i++)
        {
            if (contentType.charAt(i) == '=')
                ans +="\" \"";
            else
                ans += contentType.charAt(i);
        }

        ans +="\") NIL NIL \"7BIT\" "; //7 BIT??

        int octetSize = (message.getSize() + 7)/8;
        //counting lines
        String mes = message.getContent().toString();
        int linesAmount = 0;
        for (int i=0; i<mes.length(); i++)
        {
            if (mes.indexOf('\n',i) >0)
            {
                i = mes.indexOf('\n',i);
                linesAmount ++;
            }
        }
        ans += octetSize + " " + linesAmount + ")";

        return ans;
    }
}
