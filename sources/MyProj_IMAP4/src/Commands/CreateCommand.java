package Commands;

import DBHandler.DB;
import Server.IncomingServer;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;


public class CreateCommand extends IncomingServer {
    public String prefix;
    public String request;
    public int UserId;
    public PrintWriter out;
    public DB assistant;

    public String unmarkedResponse;
    public String markedResponse;
    public String userId;
    public String folder;
    /**
     * Конструктор CreateCommand
     * @param host_port - порт на котором работает сервер
     */
    public CreateCommand (int host_port) {//default constructor
        super(host_port);
    }

    /**
     * Конструктор CreateCommand
     * @param prefix - префикс команды
     * @param request - строка запроса
     * @param UserId - ID пользователя
     * @param out - поток для ответа
     * @param assistant - объект для работы с бд
     */
    public CreateCommand (String prefix, String request, int UserId, PrintWriter out, DB assistant) {//no-args constructor
        this.prefix = prefix;
        this.out = out;
        this.request = request;
        this.UserId = UserId;
        this.assistant = assistant;
    }

    /**
     * Выполнение комманды
     * Вначале извлекаем из запроса необходимые поля и далее обращаемся к дб для создания папки.
     * @see Server.IncomingServer#SendAndPrint(String, java.io.PrintWriter)
     */
    public void DoCommand () {
        userId = Integer.toString(UserId);
        folder = GetFirstParamFromRequest(request);

        Boolean create = CheckFolder();

        markedResponse = MakeMarked(create);

        SendAndPrint(markedResponse, out);
    }

    /**
     * Собираем строку ответа без префикса
     * @param create - true - если возможно создать папку
     *                 false - если такая папка уже есть
     * @return строка с ответом
     */
    public String MakeMarked (Boolean create) {
        String response = "";
        if (create)
        {
            int result = assistant.AddFolderToUser(Integer.toString(UserId),folder);

            if (result == 1)
            {
                response = prefix + " OK CREATE completed";
            }
            else
            {
                response = prefix + " NO CREATE error";
            }
        }
        else
        {
            response =  prefix + " NO CREATE folder already exists";
        }

        return response;
    }

    /**
     * Проверка наличия папки
     * @return - true - если возможно создать папку
     *           false - если такая папка уже есть
     */
    public Boolean CheckFolder ()
    {
        Boolean create = null;
        ResultSet resultSet = assistant.SearchFolder(userId,folder);

        try {
            if (resultSet.next())   // if this folder already exists
            {
                create = false;
            }
            else
            {
                create = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return create;
    }
}
