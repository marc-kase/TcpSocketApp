package tst.tcp.socket;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static final String EXIT = "Exit";
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void listenSocket(int port) {
        try {
            socket = new Socket("localhost", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            System.out.println("Unknown host, I/O error");
            System.exit(-1);
            e.printStackTrace();
        }
    }

    public void sendMssage(String mes) {
        System.out.println("Send message");
        out.println(mes);
    }

    public void receiveMessage() {
        long i = 0;
        boolean isConnected = true;

        while (isConnected) {
            try {
                String line = in.readLine();
                System.out.println(line);

                if (line != null) {
                    if (line.equals(EXIT)) {
                        out.close();
                        in.close();
                        socket.close();
                        isConnected = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Client reset");
    }

    public static String getLargeMessage() {
        long size = 10;
        StringBuilder sb = new StringBuilder();
        for (long i = 0; i < size; i++) {
            sb.append(i).append("\n");
        }
        return sb.toString();
    }

    public static void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String mes = Client.getLargeMessage();

        Client client = new Client();
        client.listenSocket(4322);
        client.sendMssage(mes);
        client.sendMssage(EXIT);
        client.receiveMessage();
    }
}
