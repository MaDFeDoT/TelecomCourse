package Server;
public class ServerControl
{
    /**
     * Точка входа в программу
     * @param args
     */
    public static void main(String[] args)
    {
        IncomingServer Incoming = new IncomingServer(8000);
        Incoming.run();
    }
}

