package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import model.CertificateUtils;
import model.ConfigLoader;

public class BrokerServer {

    private final int port;
    private final BrokerWindow window;

    private final X509Certificate brokerCertificate;

    private ServerSocket serverSocket;
    private boolean running;

    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public BrokerServer(int port, BrokerWindow window) {

        this.port = port;
        this.window = window;

        try {
            brokerCertificate = CertificateUtils.loadCertificate(
                    ConfigLoader.getBrokerCertificatePath()
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Não foi possível carregar o certificado do broker.",
                    e
            );
        }
    }

    public X509Certificate getBrokerCertificate() {
        return brokerCertificate;
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

                        ClientHandler handler =
                                new ClientHandler(socket, topicManager, this);

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