package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BrokerServer {

    private final int port;
    private final BrokerWindow window;

    private ServerSocket serverSocket;
    private boolean running;

    public BrokerServer(int port, BrokerWindow window) {
        this.port = port;
        this.window = window;
    }

    public void start() {
        try {
            TopicManager topicManager = new TopicManager();

            serverSocket = new ServerSocket(port);
            running = true;

            window.addLog("Broker iniciado na porta " + port);

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    window.addLog("Cliente conectado: " + socket.getInetAddress());

                    ClientHandler handler = new ClientHandler(socket, topicManager);
                    new Thread(handler).start();

                } catch (IOException e) {
                    if (running) {
                        window.addLog("Erro: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            window.addLog("Erro ao iniciar broker: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            window.addLog("Broker encerrado.");

        } catch (IOException e) {
            window.addLog("Erro ao encerrar broker: " + e.getMessage());
        }
    }
}
