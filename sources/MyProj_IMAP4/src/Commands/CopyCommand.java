package Commands;


import DBHandler.DB;
import Server.IncomingServer;

import java.io.PrintWriter;

public class CopyCommand extends IncomingServer {

    public String prefix;
    public String request;
    public DB assistant;
    public String unmarkedResponse;
    public String markedResponse;
    public PrintWriter out;
    /**
     * Конструктор CopyCommand
     * @param host_port - порт на котором работает сервер
     */
    public CopyCommand (int host_port) {//default constructor
        super(host_port);
    }

    /**
     * Конструктор CopyCommand
     * @param prefix - префикс команды
     * @param request - строка запроса
     * @param assistant - объект для работы с бд
     * @param out - поток для ответа
     */
    public CopyCommand (String prefix, String request, DB assistant, PrintWriter out) {//no-args constructor
        this.prefix = prefix;
        this.request = request;
        this.assistant = assistant;
        this.out = out;
    }

    /**
     * Выполнение комманды
     * Вначале извлекаем из запроса необходимые поля и далее обращаемся к дб для копирования сообщения.
     * Полученный результат отправляем клиенту
     * @see Server.IncomingServer#SendAndPrint(String, java.io.PrintWriter)
     */
    public void DoCommand () {
        String UID = GetUID();
        String folder = GetFirstParamFromRequest(request);
        int result = CopyMessage(UID, folder);

        unmarkedResponse = MakeUnmarked(UID, folder);
        markedResponse = MakeMarked(result);

        SendAndPrint(unmarkedResponse, out);
        SendAndPrint(markedResponse, out);

    }

    /**
     * Получаем из строки запроса UID сообщения
     * @return UID
     */
    public String GetUID () {
        String UID ="";
        int startSymbol = request.indexOf('Y') + 2;
        int endSymbol = request.indexOf('\"')-2;

        System.out.println("start = " + startSymbol + " end = " + endSymbol);
        for (int i = startSymbol; i<= endSymbol; i++)
        {
            UID += request.charAt(i);
        }
        System.out.println("UID = " + UID);
        return UID;
    }

    /**
     * Отбращаемся к БД с запросом копирования сообщения в указаную папку
     * @param uid - UID сообщения для копирования
     * @param folder - папка назначения
     * @return 1 - если успешно
     */
    int CopyMessage (String uid, String folder)
    {
        int result = assistant.CopyMessage(uid, folder);

        return result;
    }

    /**
     * Собираем строку ответа без префикса
     * @param UID - UID сообщения для копирования
     * @param folder - папка назначения
     * @return строка с ответом
     */
    public String MakeUnmarked (String UID, String folder) {
        String response = prefix + " COPY " + UID + " \"" + folder + "\"";

        return response;
    }

    /**
     * Собираем строку ответа с префиксом
     * @param result - результат запроса к БД
     * @return строка с ответом
     */
    public String MakeMarked (int result) {
        String response = "";

        if (result == 1)
        {
            response = prefix + " OK COPY completed";
        }
        else
        {
            response = prefix + " NO [TRYCREATE] Folder doesn't exist";
        }

        return response;
    }
}
