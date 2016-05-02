package Commands;

import Server.IncomingServer;

import java.io.PrintWriter;


public class LsubCommand extends IncomingServer {
    public String prefix;
    public PrintWriter out;
    public String unmarkedResponse;
    public String markedResponse;
    /**
     * Конструктор LsubCommand
     * @param host_port - порт на котором работает сервер
     */
    public LsubCommand (int host_port) {//default constructor
        super(host_port);
    }

    /**
     * Конструктор LsubCommand
     * @param prefix - префикс команды
     * @param out - поток для ответа
     */
    public LsubCommand (String prefix, PrintWriter out) {
        this.prefix = prefix;
        this.out = out;
    }
    /**
     * Выполнение комманды
     * отправляем ответ о том, что наш сервер не поддерживает данную комманду
     * @see Server.IncomingServer#SendAndPrint(String, java.io.PrintWriter)
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
        String response = prefix + " BAD LSUB illegal command";
        return response;
    }
}
