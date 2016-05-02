package Commands;


import DBHandler.DB;
import Server.IncomingServer;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchCommand extends IncomingServer {

    public DB assistant;
    public int UserId;
    public String request;
    public String prefix;
    public PrintWriter out;
    public String unmarkedResponse;
    public String markedResponse;
    /**
     * Конструктор SearchCommand
     * @param host_port - порт на котором работает сервер
     */
    public SearchCommand (int host_port) {//default constructor
        super(host_port);
    }

    /**
     * Конструктор SearchCommand
     * @param prefix - префикс команды
     * @param request - строка запроса
     * @param assistant - объект для работы с бд
     * @param out - поток для ответа
     * @param UserId - ID пользователя
     */
    public SearchCommand (String prefix, String request, DB assistant, PrintWriter out, int UserId) {//no-args constructor
        this.prefix = prefix;
        this.request = request;
        this.assistant = assistant;
        this.out = out;
        this.UserId = UserId;
    }
    /**
     * Выполнение комманды
     * Отправляем запрос БД для поиска
     * @see Server.IncomingServer#SendAndPrint(String, java.io.PrintWriter)
     */
    public void DoCommand () {
        unmarkedResponse = MakeUnmarked();
        markedResponse = MakeMarked();

        SendAndPrint(unmarkedResponse, out);
        SendAndPrint(markedResponse, out);
    }

    /**
     * Собираем строку ответа без префикса
     * результат получаем от БД
     * @return строка с ответом
     */
    public String MakeUnmarked () {
        String response = "* SEARCH";
        String userId = Integer.toString(UserId);
        ResultSet resultSet = null;

        if (request.indexOf("DELETED")!=-1)
        {
            resultSet = assistant.CountFlageddMessages(userId, folder, "DELETED");
        }

        if (request.indexOf("SEEN")!=-1)
        {
            resultSet = assistant.CountFlageddMessages(userId, folder, "SEEN");
        }

        try {
            while (resultSet.next())
            {
                response +=  " " + resultSet.getString("MESSAGE_ID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

      return response;
    }
    /**
     * Собираем строку ответа с префиксом
     * @return строка с ответом
     */
    public String MakeMarked () {
        String response = prefix + " OK SEARCH completed";

       return response;
    }

}
