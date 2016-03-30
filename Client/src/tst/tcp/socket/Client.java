package tst.tcp.socket;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void listenSocket() {
        try {
            socket = new Socket("localhost", 4321);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Unknown host, I/O error");
            System.exit(-1);
            e.printStackTrace();
        }
    }

    public void sendMssage(String mes) {
        System.out.println("Try to print message: " + mes);
        out.println(mes);
    }

    public void receiveMessage() {
        while (true) {
            try {
                String line = in.readLine();
                System.out.println("Server said: " + line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.listenSocket();
        client.sendMssage("Hello world");
        client.receiveMessage();
    }
}
