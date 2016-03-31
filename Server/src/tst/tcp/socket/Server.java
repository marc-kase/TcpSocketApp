package tst.tcp.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {
    public static final String EXIT = "Exit";
    private List<Socket> sockets = new ArrayList<>();
    private ServerSocket server;
    private final int port;
    private int clientNo = 0;

    public Server(int port) {
        this.port = port;

        try {
            server = new ServerSocket(port);
            System.out.println("Server started...");
        } catch (IOException e) {
            System.out.println("Could not open port " + port);
            System.exit(-1);
        }
    }

    public void start() {
        while (true) {
            ClientHandler c = new ClientHandler(server, clientNo++);
            c.start();
        }
    }

    public void whoIsOnline() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Start server notificator");
                while (true) {
                    for (Socket s : sockets) {
                        if (s.isOutputShutdown() || s.isInputShutdown())
                            System.out.println(s.isOutputShutdown() + " " + s.isInputShutdown());
                    }
                }
            }
        });
        t.start();
    }

    public class ClientHandler extends Thread {
        private final ServerSocket server;
        private final int clientNo;
        private BufferedReader in = null;
        private PrintWriter out = null;
        private String line = null;
        private Socket ss = null;

        public ClientHandler(ServerSocket server, int clientNo) {
            this.server = server;
            this.clientNo = clientNo;

            try {
                ss = server.accept();
                ss.setSoTimeout(1);
                ss.setKeepAlive(false);

                System.out.println(ss.getRemoteSocketAddress());
                System.out.println(ss.getInetAddress());
            } catch (IOException e) {
                System.out.println("Accept failed: " + server.getInetAddress());
                System.exit(-1);
            }

            sockets.add(ss);
            System.out.println("\nConnected client #" + this.clientNo);
        }

        @Override
        public void run() {
            clientHandler();
        }

        private void clientHandler() {
            if (ss == null) {
                System.out.println("Accept failed: " + server.getInetAddress());
                System.exit(-1);
            }

            try {
                in = new BufferedReader(new InputStreamReader(ss.getInputStream()));
                out = new PrintWriter(ss.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("I/O failed");
                System.exit(-1);
            }

            String prevLine = null;
            while (true) {
                try {
                    line = in.readLine();
                    if (line != null) {
                        if (line.equals(EXIT)) {
                            out.println(EXIT);
                            System.out.println("Stop socket #" + clientNo);
                            break;
                        }

                        if (prevLine == null) {
                            prevLine = line;

                            out.println("Received");
                            System.out.println("Received a message");
                        }
                    } else if (prevLine != null) {
                        System.out.println("Stop socket #" + clientNo);
                        break;
                    }
                } catch (IOException e) {
                    System.out.println("Read failed");
                    System.exit(-1);
                }
            }

            try {
                out.close();
                in.close();
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        Server server = new Server(4322);
//        server.whoIsOnline();
        server.start();
    }
}
