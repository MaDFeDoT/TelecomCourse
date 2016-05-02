package Commands;

import DBHandler.DB;
import Server.IncomingServer;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatusCommand extends IncomingServer {
    public String prefix;
    public PrintWriter out;
    public String request;
    public int UserId;
    public DB assistant;

    public String folder;
    public String userId;
    public String unmarkedResponse;
    public String markedResponse;

    /**
     * Конструктор StatusCommand
     * @param host_port
     */
    public StatusCommand (int host_port) {//default constructor
        super(host_port);
    }

    /**
     * Конструктор StatusCommand
     * @param prefix - префикс команды
     * @param request - строка запроса
     * @param assistant - объект для работы с бд
     * @param out - поток для ответа
     * @param UserId - ID пользователя
     */
    public StatusCommand (String prefix, PrintWriter out, String request, int UserId, DB assistant) {//no-args constructor
        this.prefix = prefix;
        this.out = out;
        this.request = request;
        this.UserId = UserId;
        this.assistant = assistant;
    }

    /**
     * Выполнение комманды
     * Работа со статусами сообщений с БД
     * @see Server.IncomingServer#SendAndPrint(String, java.io.PrintWriter)
     */
    public void DoCommand () {
        folder = GetFirstParamFromRequest(request);
        userId = Integer.toString(UserId);

        markedResponse = MakeMarked();
        unmarkedResponse = MakeUnmarked();

        SendAndPrint(unmarkedResponse, out);
        SendAndPrint(markedResponse, out);
    }
    /**
     * Собираем строку ответа без префикса
     * @return строка с ответом
     */
    public String MakeUnmarked () {
        String response = prefix + " OK STATUS completed";

        return response;
    }
    /**
     * Собираем строку ответа с префиксом
     * Обращаемся к БД что бы получить статусы писем в папке
     * @return строка с ответом
     */
    public String MakeMarked () {
        String response = "* STATUS " + folder + " (";

        if (request.contains("UIDNEXT"))
        {
            ResultSet resultSet = assistant.GetMessageUid();
            try {
                if (resultSet.next())
                {
                    String uid = resultSet.getString(1);
                    response += "UIDNEXT " + uid;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (request.contains("MESSAGES"))
        {
            ResultSet resultSet = assistant.CountExistMessages(userId, folder);
            try {
                if (resultSet.next())
                {
                    String exist = resultSet.getString("COUNT");
                    response += " MESSAGES " + exist;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (request.contains("UNSEEN"))
        {
            ResultSet resultSet = assistant.CountUnSeenMessages(userId, folder);
            try {
                if (resultSet.next())
                {
                    String unseen = resultSet.getString("COUNT");
                    response += " UNSEEN " + unseen;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (request.contains("RECENT"))
        {
            ResultSet resultSet = assistant.CountRecentMessages(userId, folder);
            try {
                if (resultSet.next())
                {
                    String recent = resultSet.getString("COUNT");
                    response += " RECENT " + recent;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        response += ")";

        return response;
    }

}
