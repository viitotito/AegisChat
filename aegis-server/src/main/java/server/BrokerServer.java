package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BrokerServer {

    private final int port;
    private final BrokerWindow window;

    private ServerSocket serverSocket;
    private boolean running;

    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public BrokerServer(int port, BrokerWindow window) {
        this.port = port;
        this.window = window;
    }

    public boolean start() {
        try {
            TopicManager topicManager = new TopicManager();

            serverSocket = new ServerSocket(port);
            running = true;

            window.addLog("Broker iniciado na porta " + port);

            new Thread(() -> {
                while (running) {
                    try {
                        Socket socket = serverSocket.accept();

                        ClientHandler handler = new ClientHandler(socket, topicManager, this);
                        clients.add(handler);
                        new Thread(handler).start();

                    } catch (IOException e) {
                        if (running) {
                            window.addLog("Erro: " + e.getMessage());
                        }
                    }
                }
            }).start();

            return true;

        } catch (IOException e) {
            window.addLog("Erro ao iniciar broker: " + e.getMessage());
            return false;
        }
    }

    public void stop() {
        running = false;

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            for (ClientHandler client : clients) {
                client.close();
            }

            clients.clear();

            window.addLog("Broker encerrado.");

        } catch (IOException e) {
            window.addLog("Erro ao encerrar broker: " + e.getMessage());
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public void log(String message) {
        window.addLog(message);
    }
}
