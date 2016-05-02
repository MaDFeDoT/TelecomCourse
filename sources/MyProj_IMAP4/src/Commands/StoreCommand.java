package Commands;

import DBHandler.DB;
import Server.IncomingServer;

import java.io.PrintWriter;
import java.util.Vector;

public class StoreCommand extends IncomingServer {

    public String prefix;
    public String request;
    public DB assistant;
    public String unmarkedResponse;
    public String markedResponse;
    public Boolean plus;
    public PrintWriter out;
    /**
     * Конструктор StoreCommand
     * @param host_port - порт на котором работает сервер
     */
    public StoreCommand (int host_port) {//default constructor
        super(host_port);
    }
    /**
     * Конструктор StoreCommand
     * @param prefix - префикс команды
     * @param request - строка запроса
     * @param assistant - объект для работы с бд
     * @param out - поток для ответа
     */
    public StoreCommand (String prefix, String request, DB assistant, PrintWriter out) {//no-args constructor
        this.prefix = prefix;
        this.request = request;
        this.assistant = assistant;
        this.out = out;
    }
    /**
     * Выполнение комманды
     * Сохранение письма в БД
     * @see Server.IncomingServer#SendAndPrint(String, java.io.PrintWriter)
     */
    public void DoCommand () {
        GetPluse ();
        String UID = GetUID();
        Vector <String> flags = GetFlags();


        unmarkedResponse = MakeUnmarked(UID, flags);
        markedResponse = MakeMarked(UID, flags);

        SendAndPrint(unmarkedResponse, out);
        SendAndPrint(markedResponse, out);


    }

    /**
     * Проверка наличия флага "+" в запросе
     */
    public void GetPluse () {
        if (request.contains("+"))
        {
            plus = true;
        }
        else
        {
            plus = false;
        }
    }

    /**
     * Получение UID письма
     * @return UID
     */
    public String GetUID () {
        String UID ="";
        int startSymbol = request.indexOf('E') + 2;
        int endSymbol;

        if (plus)
        {
            endSymbol = request.indexOf('+') -2;
        }
        else
        {
            endSymbol = request.indexOf('-') -2;
        }

        for (int i = startSymbol; i<= endSymbol; i++)
        {
            UID += request.charAt(i);
        }

        return UID;
    }

    /**
     * Получения флагов письма
     * @return флаги
     */
    public Vector<String> GetFlags () {
        Vector <String> flags = new Vector<String>();

        for (int i = 0; i < FLAGS.length; i++)
        {
             if (request.contains(FLAGS[i]))
             {
                 flags.add(FLAGS[i]);
             }
        }

        return flags;
    }

    /**
     * Добавляем флаги сообщения в БД
     * @param UID - UID письма
     * @param flag - флаги
     * @return - 1 - если сохранение успешно
     *           2 - если произошла ошибка
     */
    public int ChangeFlag (String UID, String flag){
        String uppercaseFlag = flag.toUpperCase();
        int result;
        if (plus)
        {
            result = assistant.AddFlagToMessage(UID,uppercaseFlag);
        }
        else
        {
            result = assistant.RemoveFlagFromMessage(UID, uppercaseFlag);
        }

        return result;
    }
    /**
     * Собираем строку ответа без префикса
     * @param UID - UID сообщения для копирования
     * @param flags - флайги сообщения
     * @return строка с ответом
     */
    public String MakeUnmarked (String UID, Vector <String> flags) {
        String response = ("* STORE " + UID + " FLAGS (");
        for (int i = 0; i< flags.size(); i++)
        {
            response += "\\" + flags.get(i) + " ";
        }
        response = response.substring(0,response.length()-1);
        response += ")";

        return response;
    }
    /**
     * Собираем строку ответа с префиксом
     * @param UID - UID сообщения для копирования
     * @param flags - флайги сообщения
     * @return строка с ответом
     */
    public String MakeMarked (String UID, Vector <String> flags) {
        String response = "";

        for (int i = 0; i< flags.size(); i++)
        {
            int result = ChangeFlag(UID, flags.get(i));
            if (result == 1)
            {
                response = prefix + " OK STORE completed";
            }
            else
            {
                response = prefix + " NO STORE can't complete";
            }
        }
        return response;
    }
}

