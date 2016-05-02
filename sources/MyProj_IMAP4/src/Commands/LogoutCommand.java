package Commands;

import Server.IncomingServer;

import java.io.PrintWriter;

public class LogoutCommand extends IncomingServer {
    public String prefix;
    public PrintWriter out;
    public String unmarkedResponse;
    public String markedResponse;

    /**
     * Конструктор LogoutCommand
     * @param host_port - порт на котором работает сервер
     */
    public LogoutCommand (int host_port) {//default constructor
        super(host_port);
    }

    /**
     * Конструктор LogoutCommand
     * @param prefix - префикс команды
     * @param out - поток для ответа
     */
    public LogoutCommand (String prefix, PrintWriter out) {
        this.prefix = prefix;
        this.out = out;
    }

    /**
     * Выполнение команды
     * Выход пользователя.
     */
    public void DoCommand () {
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
        String response = "* BYE IMAP4rev1 server terminating connection";

        return response;
    }

    /**
     * Собираем строку ответа с префиксом
     * @return строка с ответом
     */
    public String MakeMarked () {
        String response = prefix + " LOGOUT completed";

        return response;
    }

}
