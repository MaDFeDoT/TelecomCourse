package Commands;

import Server.IncomingServer;

import java.io.PrintWriter;


public class SubscribeCommand extends IncomingServer {
    public String prefix;
    public PrintWriter out;
    public String unmarkedResponse;
    public String markedResponse;

    public SubscribeCommand (int host_port) {//default constructor
        super(host_port);
    }

    public SubscribeCommand (String prefix, PrintWriter out) {
        this.prefix = prefix;
        this.out = out;
    }

    public void DoCommand () {
        markedResponse = MakeMarked();
        SendAndPrint(markedResponse, out);
    }

    public String MakeMarked () {
        String response = prefix + " BAD SUBSCRIBE illegal command";
        return response;
    }
}
