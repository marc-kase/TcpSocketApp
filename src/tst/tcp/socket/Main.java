package tst.tcp.socket;

public class Main {

    public static void main(String[] args) {

        Server server = new Server(4322);
        server.start();

        Client client = new Client();
        client.listenSocket(4322);
        client.sendMssage("Hello world");
        client.receiveMessage();
    }
}
