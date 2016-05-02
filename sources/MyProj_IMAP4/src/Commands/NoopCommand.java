package Commands;

import Server.IncomingServer;

import java.io.PrintWriter;

public class NoopCommand extends IncomingServer {
    public String prefix;
    public PrintWriter out;
    public String unmarkedResponse;
    public String markedResponse;
    /**
     * Конструктор NoopCommand
     * @param host_port - порт на котором работает сервер
     */
    public NoopCommand (int host_port) {//default constructor
        super(host_port);
    }

    /**
     * Конструктор NoopCommand
     * @param prefix - префикс команды
     * @param out - поток для ответа
     */
    public NoopCommand (String prefix, PrintWriter out) {
        this.prefix = prefix;
        this.out = out;
    }

    /**
     * Выполнение команды
     * Отправляем ответ NOOP
     */
    public void DoCommand () {
        markedResponse = MakeMarked();

        SendAndPrint(markedResponse, out);
    }
    /**
     * Собираем строку ответа с префиксом
     * @return строка с ответом
     */
    public String MakeMarked () {
        String response = prefix + " OK NOOP completed";
        return response;
    }

}
