import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestHalfClosedState {

    public static class TestServer {

        public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(10);
        private ServerSocket serverSocket;
        private int numberOfMessages;
        private static final int MIN_BYTES = 30;
        private final int maxBytes;
        private final int maxSleepTime;
        public static Random rand = new Random();
        private static final int MIN_SLEEP_TIME = 10;
        public static final byte[] THOUSAND_BYTES = new byte[1024];

        static {
            for (int i = 0; i < THOUSAND_BYTES.length; i++) {
                THOUSAND_BYTES[i] = 0;
            }
        }

        public TestServer(int numberOfMessages, int maxSleepTime, int maxBytes) {
            this.maxBytes = maxBytes;
            this.numberOfMessages = numberOfMessages;
            this.maxSleepTime = maxSleepTime;
        }

        public void start(int port) throws IOException {
            if (serverSocket == null || serverSocket.isClosed()) {
                serverSocket = new ServerSocket(port);
                System.out.println("Listening on " + port);
                EXECUTOR_SERVICE.execute(() -> {

                    try {
                        while (true) {
                            final Socket s = serverSocket.accept();

                            System.out.println("Got connection from " + s.getInetAddress());
                            String client = readName(s.getInputStream());
                            System.out.println("Name: " + client);
                            final int[] totalBytes = {0};

                            EXECUTOR_SERVICE.execute(() -> {

                                try {
                                    for (int i = 0; i < numberOfMessages; i++) {

//                                        if (!s.isOutputShutdown()) {

                                        s.getOutputStream().write(i);
                                        int size = rand.nextInt(maxBytes);
                                        if (size < MIN_BYTES) size = MIN_BYTES;
                                        totalBytes[0] += size;
                                        System.out.printf("[Server] Sending mes %d to %s, sent bytes: %d\n", i, client, totalBytes[0]);

                                        byte[] bytes = new byte[size];
                                        for (int k = 0; k < size; k++) {
                                            bytes[k] = 0;
                                        }

                                        s.getOutputStream().write(bytes);
//                                        s.getOutputStream().write(THOUSAND_BYTES);
//                                        } else {
//                                            System.out.printf("Output shutdown. mes %d\n", i);
//                                        }
                                        int sleep = rand.nextInt(maxSleepTime);
                                        sleep(sleep > MIN_SLEEP_TIME ? sleep : MIN_SLEEP_TIME);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });

                            System.out.println("We here");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        public static String readName(InputStream in) throws IOException {
            int len = in.read();
            byte[] bytes = new byte[len];
            in.read(bytes);
            return new String(bytes);
        }

        private static void sleep(int i) {
            try {
                Thread.sleep(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void stop() throws IOException {
            serverSocket.close();
        }
    }

    public static class TestClient {

        private final String name;
        private final String host;
        private final int port;
        private Socket socket;
        private int shutdownOuptutOnMesNo;

        public TestClient(String host, int port, String name, int shutdownOuptutOnMesNo) {
            this.host = host;
            this.port = port;
            this.name = name;

            this.shutdownOuptutOnMesNo = shutdownOuptutOnMesNo;
        }

        public void connect() throws IOException {
            System.out.printf("Connecting [%s], random num: %d\n", name, shutdownOuptutOnMesNo);
            socket = new Socket(host, port);
            final byte[] bytes = new byte[1024];
            Executors.newFixedThreadPool(1).execute(() -> {
                try {
                    byte[] nameBytes = name.getBytes();
                    socket.getOutputStream().write(nameBytes.length);
                    socket.getOutputStream().write(nameBytes);
                    while (true) {
                        InputStream in = socket.getInputStream();

                        int i = in.read();
                        int len = in.read(bytes, 0, bytes.length);
                        System.out.printf("[%s] Got mes %d, %d bytes\n", name, i, len);

                        if (i == -1) {
                            return;
                        }

                        if (i == shutdownOuptutOnMesNo) {
                            socket.shutdownInput();
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }


    public static void main(String[] args) throws IOException {

        if (args.length > 0) {
            int port = 1567;
            try {
                int mesCnt = Integer.parseInt(args[0]);
                int clientCnt = Integer.parseInt(args[1]);
                int maxSleep = Integer.parseInt(args[2]);
                int maxBytes = Integer.parseInt(args[3]);

                TestServer server = new TestServer(mesCnt, maxSleep, maxBytes);
                server.start(port);

                for (int c = 0; c < clientCnt; c++) {
                    TestClient client = new TestClient("localhost", port, "Client #" + c, 10);
                    client.connect();
                }

            } catch (NumberFormatException ne) {
                ne.printStackTrace();
            }
        } else {
            System.out.println("Need args dude, [messCnt],[clientCnt],[maxSleepTime],[maxBytes]");
        }

    }
}