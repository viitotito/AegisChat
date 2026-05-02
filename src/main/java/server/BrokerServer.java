package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BrokerServer {

    private final int port;
    private boolean isRunning;

    private ServerSocket serverSocket;
    private final BrokerWindow brokerWindow;

    public BrokerServer(int port, BrokerWindow brokerWindow) {
        this.port = port;
        this.brokerWindow = brokerWindow;
    }

    private void addLog(String message) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            brokerWindow.addLog(message);
        });
    }

    public void start() {
        new Thread(() -> {
            try {
                TopicManager topicManager = new TopicManager();

                this.serverSocket = new ServerSocket(port);
                this.isRunning = true;

                addLog("Servidor iniciado na porta: " + port);

                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();

                    addLog("Cliente conectado: " + clientSocket.getInetAddress());

                    ClientHandler clientHandler = new ClientHandler(clientSocket, topicManager);

                    new Thread(clientHandler).start();
                }

            } catch (IOException e) {
                System.out.println("");
            }
        }).start();
    }

    public void stop() {
        isRunning = false;
        try {
            serverSocket.close();
            addLog("Servidor parado");
        } catch (IOException e) {
            addLog("Erro ao parar o servidor: " + e.getMessage());
        }
    }

}
