//Муравьев Федор 43501/3 IMAP4 сервер.
package Commands;

import DBHandler.DB;
import Server.IncomingServer;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SelectCommand extends IncomingServer {
    public String prefix;
    public String request;
    public DB assistant;
    public String unmarkedResponse;
    public String markedResponse;
    public PrintWriter out;

    public String folder;
    public int existMessages;
    public int recentMessages;
    public int unSeenMessages;
    public int UserId;
    public int UIDVALIDITY;

    /**
     * Конструктор SelectCommand
     * @param host_port - порт на котором работает сервер
     */
    public SelectCommand (int host_port) {//default constructor
        super(host_port);
    }

    /**
     * Конструктор SelectCommand
     * @param prefix - префикс команды
     * @param request - строка запроса
     * @param assistant - объект для работы с бд
     * @param out - поток для ответа
     * @param UserId - ID пользователя
     * @param UIDVALIDITY - UID запроса
     */
    public SelectCommand (String prefix, String request, DB assistant, PrintWriter out, int UserId, int UIDVALIDITY) {
        this.prefix = prefix;
        this.request = request;
        this.assistant = assistant;
        this.out = out;
        this.UserId = UserId;
        this.UIDVALIDITY = UIDVALIDITY;
    }

    /**
     * Выполнение комманды
     * Увеличиваем UIDVALIDITY, работаем с БД
     * @see Server.IncomingServer#SendAndPrint(String, java.io.PrintWriter)
     */
    public void DoCommand () {
        folder = GetFirstParamFromRequest(request);
        UIDVALIDITY++; //geting new UIDVALIDITY

        markedResponse = MakeMarked();
        unmarkedResponse = MakeUnmarked();

        SendAndPrint(unmarkedResponse, out);
        SendAndPrint(markedResponse, out);

    }
    /**
     * Собираем строку ответа с префиксом
     * Получаем статистику по сообщением из БД
     * @return строка с ответом
     */
    public String MakeMarked () {
        String response = "";
        String userId = Integer.toString(UserId);
        ResultSet resultSet;
        resultSet = assistant.CountExistMessages(userId,folder);
        try {
            if (resultSet.next())
                existMessages = Integer.valueOf(resultSet.getString("COUNT"));
            else
            {
                System.err.println("DB error");
                response = prefix + " NO SELECT failed";
                return response;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        resultSet = assistant.CountRecentMessages(userId,folder);
        try {
            if (resultSet.next())
                recentMessages = Integer.valueOf(resultSet.getString("COUNT"));
            else
            {
                System.err.println("DB error");
                response = prefix + " NO SELECT failed";
                return response;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        resultSet = assistant.CountUnSeenMessages(userId,folder);
        try {
            if (resultSet.next())
                unSeenMessages = Integer.valueOf(resultSet.getString("COUNT"));
            else
            {
                System.err.println("DB error");
                response = prefix + " NO SELECT failed";
                return response;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        response = prefix + " OK [READ-WRITE] SELECT Completed";

        return response;
    }
    /**
     * Собираем строку ответа без префикса
     * @return строка с ответом
     */
    public String MakeUnmarked () {

        String firstUnseen = FirstUnseen();

        String response = "* " + existMessages + " EXISTS\n" +
                "* " + recentMessages +" RECENT\n" +
                "* OK [UNSEEN " + unSeenMessages + "] " + firstUnseen + "\n"+
                "* OK [UIDVALIDITY " + UIDVALIDITY + "] UIDs valid\n" +
                "* FLAGS (\\Answered \\Recent \\Deleted \\Seen \\Draft \\Flagged)\n" +
                "* OK [PERMANENTFLAGS (\\Deleted \\Seen \\*)] Limited";

        return  response;
    }

    /**
     * Получаем строку-ответ с первым непрочитанным сообщением
     * @return
     */
    public String FirstUnseen ()
    {
        String response = "";
        ResultSet resultSet = assistant.FirstUnseen(Integer.toString(UserId), folder);

        try {
            if (resultSet.next())
            {
                response = "Message " + resultSet.getString("MESSAGE_ID") + " is first unseen";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return response;
    }
}
