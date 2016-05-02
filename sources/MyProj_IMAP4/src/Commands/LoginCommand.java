package Commands;


import DBHandler.DB;
import Server.IncomingServer;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginCommand extends IncomingServer {
    public DB assistant;
    public String prefix;
    public String request;
    public PrintWriter out;
    public int UserId;
    public String markedResponse;

    /**
     * Конструктор LoginCommand
     * @param host_port - порт на котором работает сервер
     */
    public LoginCommand (int host_port) {//default constructor
        super(host_port);
    }

    /**
     * Конструктор LoginCommand
     * @param prefix - префикс команды
     * @param request - строка запроса
     * @param assistant - объект для работы с бд
     * @param out - поток для ответа
     */
    public LoginCommand (String prefix, String request, DB assistant, PrintWriter out) {//no-args constructor
        this.prefix = prefix;
        this.request = request;
        this.assistant = assistant;
        this.out = out;
    }

    /**
     * Выполнение комманды
     * Проверка пользователя
     * @see Server.IncomingServer#SendAndPrint(String, java.io.PrintWriter)
     */
    public void DoCommand () {
        Boolean existUser = FindUser ();
        markedResponse = MakeMarked(existUser);

        SendAndPrint(markedResponse, out);
    }

    /**
     * Собираем строку ответа без префикса
     * @param existUser - существует ли пользователь
     * @return строка с ответом
     */
    public String MakeMarked (Boolean existUser) {
        String response = "";
        if (existUser)
        {
           response = prefix + " OK LOGIN completed";
        }
        else
        {
            response = prefix + " NO LOGIN failed";
        }

        return response;
    }

    /**
     * Ищем указанного в запросе пользователя
     * @return найден ли пользователь
     */
    public Boolean FindUser ()
    {
        Boolean user = null;
        String login = GetFirstParamFromRequest(request);
        String password = GetSecondParamFromRequest(request);

        ResultSet resultSet = assistant.FindUser(login,password);

        try {
            if (resultSet.next())
            {
                UserId = Integer.valueOf(resultSet.getString("USER_ID"));
                user = true;
            }
            else
            {
                user = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return user;
    }
}
