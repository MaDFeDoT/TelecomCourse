package Commands;

import DBHandler.DB;
import Server.IncomingServer;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ListCommand extends IncomingServer {
    public String prefix;
    public String request;
    public DB assistant;
    public PrintWriter out;
    public String unmarkedResponse;
    public String markedResponse;
    public int UserId;
    String firstParam;
    String secondParam;

    /**
     * Конструктор ListCommand
     * @param host_port
     */
    public ListCommand (int host_port) {//default constructor
        super(host_port);
    }
    //we doing nothing, but can update something

    /**
     * Конструктор ListCommand
     * @param prefix - префикс команды
     * @param request - строка запроса
     * @param out - поток для ответа
     * @param assistant - объект для работы с бд
     * @param UserId - ID пользователя
     */
    public ListCommand (String prefix, String request, PrintWriter out, DB assistant, int UserId) {//no-args constructor
        this.prefix = prefix;
        this.out = out;
        this.request = request;
        this.assistant = assistant;
        this.UserId = UserId;
    }
    /**
     * Выполнение комманды
     * Вначале извлекаем из запроса необходимые поля и далее обращаемся к дб для получения списка сообщений.
     * Полученный результат отправляем клиенту
     * @see Server.IncomingServer#SendAndPrint(String, java.io.PrintWriter)
     */
    public void DoCommand () {

        firstParam =  GetFirstParamFromRequest(request);
        secondParam = GetSecondParamFromRequest(request);

        Boolean exists = FolderExist();

        markedResponse = MakeMarked(exists);
        if (exists)
        {
            unmarkedResponse = MakeUnmarked();
            SendAndPrint(unmarkedResponse, out);
        }

        SendAndPrint(markedResponse, out);
    }

    /**
     * Проверяем, существует ли указанная папка
     * @return - существует или нет
     */
    public Boolean FolderExist ()
    {
        Boolean exists = null;
        ResultSet resultSet = assistant.SearchFolder(Integer.toString(UserId),secondParam);
        try {
            if (resultSet.next())
            {
                exists = true;
            }
            else
            {
                exists = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  exists;
    }

    /**
     * Собираем строку ответа без префикса
     * @return
     */
    public String MakeUnmarked () {
        String response = "";
        ResultSet resultSet = null;
            if (firstParam.equals(""))
            {
                resultSet = assistant.GetDeepFolser(Integer.toString(UserId),secondParam);
                try {
                    if (resultSet.next())
                    {
                        while (resultSet.next())
                        {
                            //NEED TO COMPLETE!!
                        }
                    }
                    else
                    {
                        response ="* LIST (\\NoInferiors) \"|\" " + secondParam;
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        return response;
    }

    /**
     * Собираем строку ответа с префиксом
     * @param exists - существует ли указанная папка
     * @return - строка с ответом
     */
    public String MakeMarked (Boolean exists) {
        String response = "";

        if (exists)
        {
            response = prefix + " OK LIST completed";
        }
        else
        {
            response = prefix + " BAD LIST can't find this mailbox";
        }

        return response;
    }

}
