package tst.tcp.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by mark on 3/30/16.
 */
public class Server {
    private ServerSocket server;
    private Socket client;
    private int port;
    private BufferedReader in;
    private PrintWriter out;
    private String line;

    public void listenSocket(int port) {
        this.port = port;

        try {
            server = new ServerSocket(port);
            System.out.println("Server started...");
        } catch (IOException e) {
            System.out.println("Could not open port " + port);
            System.exit(-1);
        }

        try {
            client = server.accept();
        } catch (IOException e) {
            System.out.println("Accept failed: " + port);
            System.exit(-1);
        }

        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Read failed");
            System.exit(-1);
        }

        while (true) {
            try {
                line = in.readLine();
                if (line != null) {
                    out.println("Received");
                    System.out.println("Client said: " + line);
                }
            } catch (IOException e) {
                System.out.println("Read failed");
                System.exit(-1);
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.listenSocket(4321);
    }
}
