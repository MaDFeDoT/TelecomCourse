package Server;

import Commands.*;
import DBHandler.DB;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Properties;

public class IncomingServer extends Thread
{
    public int host_port = 0;
    public BufferedReader in = null;
    public PrintWriter out = null;
    public ServerSocket servers = null;
    public Socket fromclient = null;
    public DB assistant;
    public int UserId;
    public int UIDVALIDITY = 0;
    public String folder;
    public static final String[] CAPABILITY = new String[]{"capability", "CAPABILITY"};
    public static final String[] LOGIN = new String[]{"login", "LOGIN"};
    public static final String[] SELECT = new String[]{"select", "SELECT"};
    public static final String[] SEARCH = new String[]{"search", "SEARCH"};
    public static final String[] FETCH = new String[]{"fetch", "FETCH"};
    public static final String[] NOOP = new String[]{"noop", "NOOP"};
    public static final String[] LOGOUT = new String[]{"logout", "LOGOUT"};
    public static final String[] LSUB = new String[]{"lsub", "LSUB"};
    public static final String[] LIST = new String[]{"list", "LIST"};
    public static final String[] CREATE = new String[]{"create", "CREATE"};
    public static final String[] SUBSCRIBE = new String[]{"subscribe", "SUBSCRIBE"};
    public static final String[] COPY = new String[]{"copy", "COPY"};
    public static final String[] STATUS = new String[]{"status", "STATUS"};
    public static final String[] STORE = new String[]{"store", "STORE"};
    public static final String[] FLAGS = new String[]{"Answered", "Recent", "Deleted", "Seen", "Draft", "Flagged"};

    /**
     * Конструктор IncomingServer
     * @param host_port - порт, на котором развернут сервер
     */
    public IncomingServer(int host_port)
    {
        this.host_port = host_port;
        //make new assistant for work with base
        try {
            assistant = new DB();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Конструктор IncomingServer по-умолчанию
     */
    public IncomingServer() {
    }

    /**
     *Запуск сервера
     * Создает соединение, запускает процесс обмена сообщениями между клиентом и сервером
     */
    public void run()
    {

        System.out.println("Server at " + host_port + " running");
        //creating server socket
        try {
            servers = new ServerSocket(host_port);
        } catch (IOException e) {
            System.err.println("Couldn't listen to port "+host_port);
            System.exit(-1);
        }
        // connecting client
        try {
            System.out.println("Waiting for a client...");
            fromclient = servers.accept();
            System.out.println("Client connected");
        } catch (IOException e) {
            System.err.println("Can't accept");
            System.exit(-1);
        }
        //Buffer for echo
        try {
            in  = new BufferedReader(new
                    InputStreamReader(fromclient.getInputStream()));
            out = new PrintWriter(fromclient.getOutputStream(),true);
        } catch (IOException e) {
            System.err.println("Couldn't make buffer");
            System.exit(-1);
        }

        //chating
        out.println("* OK, IMAP4 service is ready");

        int i = 0;
        while (i==0)
            try {
                WorkWithClient();
            } catch (SQLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

    }

    /**
     * Прием сообщения от клиента и передача его методу анализа сообщений
     * @throws SQLException - ошибки при анализе
     */
    public void WorkWithClient() throws SQLException {
        String client_message = null;
        try {
            client_message = in.readLine();
        } catch (IOException e) {
            System.err.println("Couldn't read client message");
            System.exit(-1);
        }
        if (client_message!=null)
        {
            System.out.println("Clent says: " + client_message);
            AnalyzeMessage(client_message);
        }
    }

    /**
     * закрытие соединения с клиентом
     */
    public void CloseServer()
    {
        //closing all thigs
        out.close();
        try {
            in.close();
        } catch (IOException e) {
            System.err.println("Couldn't close BufferedReader");
            System.exit(-1);
        }
        try {
            fromclient.close();
        } catch (IOException e) {
            System.err.println("Couldn't close ClientSocket");
            System.exit(-1);
        }
        try {
            servers.close();
        } catch (IOException e) {
            System.err.println("Couldn't close ServerSocket");
            System.exit(-1);
        }
    }

    /**
     * Анализ входящих сообщений
     * Ищем во входящей строке ключевое слово, если оно находится, запускаем соответсвующий метод
     * @param client_message - сообщение от клиента
     * @throws SQLException - ошибки БД
     */
    public void AnalyzeMessage(String client_message) throws SQLException {
        String prefix = client_message.substring(0,client_message.indexOf(" "));
         //checking words
         if (client_message.contains(CAPABILITY[0]) || client_message.contains(CAPABILITY[1]) )
         {
             CapabilityCommand capability = new CapabilityCommand(prefix, out);
             capability.DoCommand();
         }

         if (client_message.contains(LOGIN[0]) || client_message.contains(LOGIN[1]) )
         {
             LoginCommand login = new LoginCommand(prefix, client_message, assistant, out);
             login.DoCommand();
             UserId = login.UserId;
         }

        if (client_message.contains(SELECT[0]) || client_message.contains(SELECT[1]) )
        {
            SelectCommand select = new SelectCommand(prefix, client_message, assistant, out, UserId, UIDVALIDITY);
            select.DoCommand();
            folder = select.folder;
            UIDVALIDITY = select.UIDVALIDITY;
        }

        if (client_message.contains(SEARCH[0]) || client_message.contains(SEARCH[1]) )
        {
            SearchCommand search = new SearchCommand(prefix, client_message, assistant, out, UserId);
            search.DoCommand();
        }

        if (client_message.contains(FETCH[0]) || client_message.contains(FETCH[1]) )
        {
            FetchCommand fetch = new FetchCommand(prefix, client_message, assistant, out, folder, UserId);
            fetch.DoCommand();
        }

        if (client_message.contains(NOOP[0]) || client_message.contains(NOOP[1]) )
        {
            NoopCommand noop = new NoopCommand(prefix, out);
            noop.DoCommand();
        }

        if (client_message.contains(LOGOUT[0]) || client_message.contains(LOGOUT[1]) )
        {
            LogoutCommand logout = new LogoutCommand(prefix, out);
            logout.DoCommand();
        }

        if (client_message.contains(LSUB[0]) || client_message.contains(LSUB[1]) )
        {
            LsubCommand lsub = new LsubCommand(prefix, out);
            lsub.DoCommand();
        }

        if (client_message.contains(LIST[0]) || client_message.contains(LIST[1]) )
        {
            ListCommand list = new ListCommand(prefix, client_message, out, assistant, UserId);
            list.DoCommand();
        }

        if (client_message.contains(CREATE[0]) || client_message.contains(CREATE[1]) )
        {
            CreateCommand create = new CreateCommand(prefix, client_message, UserId, out, assistant);
            create.DoCommand();
        }

        if (client_message.contains(SUBSCRIBE[0]) || client_message.contains(SUBSCRIBE[1]) )
        {
            SubscribeCommand subscribe = new SubscribeCommand(prefix, out);
            subscribe.DoCommand();
        }

        if (client_message.contains(COPY[0]) || client_message.contains(COPY[1]) )
        {
            CopyCommand copy = new CopyCommand(prefix, client_message, assistant, out);
            copy.DoCommand();
        }

        if (client_message.contains(STATUS[0]) || client_message.contains(STATUS[1]) )
        {
            StatusCommand status = new StatusCommand(prefix, out, client_message, UserId, assistant);
            status.DoCommand();
        }


        if (client_message.contains(STORE[0]) || client_message.contains(STORE[1]) )
        {
            StoreCommand store = new StoreCommand(prefix, client_message, assistant, out);
            store.DoCommand();
        }

     }

    /**
     * Извлечения первого параметра из сообщения клиента
     * @param request - сообщение клиента
     * @return первый параметр
     */
    public String GetFirstParamFromRequest(String request)
    {
        return request.substring( (request.indexOf("\"")+1),request.indexOf("\"",request.indexOf("\"")+1));
    }
    /**
     * Извлечения второго параметра из сообщения клиента
     * @param request - сообщение клиента
     * @return второй параметр
     */
    public String GetSecondParamFromRequest(String request)
    {
        int indexSecondseparator  = request.indexOf("\"",request.indexOf("\"")+1);
        return request.substring(indexSecondseparator + 3,request.length()-1) ;
    }

    /**
     * Открытие сообщения с диска
     * @param UID - UID сообщения
     * @return открытое сообщение
     */
    public MimeMessage OpenMessage (String UID)
    {
        //Connect *.eml file
        String emlFile = "C:\\IMAP4\\mail\\" + UID + ".eml";
        Properties props = System.getProperties();
        Session mailSession = Session.getDefaultInstance(props, null);
        InputStream source = null;
        try {
            source = new FileInputStream(emlFile);
        } catch (FileNotFoundException e) {
            System.err.println("Can't find this way ");
            e.printStackTrace();
        }
        MimeMessage message = null;
        try {
            message = new MimeMessage(mailSession, source);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return message;
    }

    /**
     * Отправка сообщения клиенту и вывод его в консоль
     * @param repsonse - ответ для клиента
     * @param out - поток для общения с клиентом
     */
    public void SendAndPrint (String repsonse, PrintWriter out)
    {//just for typing less
        out.println (repsonse);
        System.out.println ("IncomingServer says: " + repsonse);
    }

 }

