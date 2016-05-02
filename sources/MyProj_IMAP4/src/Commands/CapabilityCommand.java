package Commands;


import Server.IncomingServer;

import java.io.PrintWriter;
public class CapabilityCommand extends IncomingServer {
    String prefix;
    PrintWriter out;
    public String unmarkedResponse;
    public String markedResponse;

    /**
     * Конструктор Capability
     * @param host_port - порт на котором работает сервер
     */
    public CapabilityCommand (int host_port) {
        super(host_port);
    }

    /**
     * Конструктор Capability
     * @param prefix - префикс команды
     * @param out - поток для ответа
     */
    public CapabilityCommand (String prefix, PrintWriter out) {
        this.prefix = prefix;
        this.out = out;
    }

    /**
     * Выполнение комманды
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
     * @return строка с ответом
     */
    public String MakeUnmarked () {
        String response  =   "* CAPABILITY IMAP4rev1";

        return response;
    }

    /**
     * Собираем строку ответа с префиксом
     * @return строка с ответом
     */
    public String MakeMarked () {
         String response = prefix + " OK CAPABILITY completed";

        return response;
    }
}
