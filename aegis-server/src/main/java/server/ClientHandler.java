package server;

import model.ConfigLoader;
import model.Message;

import java.io.*;
import java.net.Socket;
import java.security.cert.X509Certificate;
import model.CertificateUtils;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final BrokerServer server;

    private final TopicManager topicManager;

    private BufferedReader input;
    private PrintWriter output;

    private String clientName;

    public ClientHandler(Socket socket, TopicManager topicManager, BrokerServer server) {
        this.socket = socket;
        this.topicManager = topicManager;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            output.println(new Message("REQUEST_CERT", "", "BROKER", ""));
            String line = input.readLine();
            if (line == null) {
                throw new Exception("Conexão encerrada antes da autenticação");
            }

            Message authMessage = Message.fromString(line);
            if (!"CERT".equals(authMessage.getType())) {
                send(new Message("ERROR", "AUTH", "BROKER", "Certificado não recebido."));
                server.removeClient(this);
                socket.close();
                return;
            }

            X509Certificate clientCert = CertificateUtils.decodeCertificate(authMessage.getContent());
            X509Certificate serverCert = CertificateUtils.loadCertificate(ConfigLoader.getServerCertPath());

            if (!CertificateUtils.verifyCertificate(clientCert, serverCert)) {
                send(new Message("ERROR", "AUTH", "BROKER", "Falha na autenticação do certificado."));
                server.removeClient(this);
                socket.close();
                return;
            }

            String certificateName = CertificateUtils.getCommonName(clientCert);
            output.println(new Message("REQUEST_NAME", "", "BROKER", ""));

            line = input.readLine();
            if (line == null) {
                throw new Exception("Conexão encerrada antes de enviar o nome");
            }

            authMessage = Message.fromString(line);
            if (!"NAME".equals(authMessage.getType()) || authMessage.getSender().isEmpty()) {
                send(new Message("ERROR", "AUTH", "BROKER", "Nome não informado."));
                server.removeClient(this);
                socket.close();
                return;
            }

            clientName = authMessage.getSender();
            if (!clientName.equals(certificateName)) {
                send(new Message("ERROR", "AUTH", "BROKER", "O nome fornecido não corresponde ao certificado."));
                server.removeClient(this);
                socket.close();
                return;
            }

            synchronized (topicManager) {
                if (!topicManager.registerUser(clientName, this)) {
                    send(new Message("ERROR", "", "BROKER", "Nome já está em uso."));
                    server.log("Tentativa de conexão falhou: nome duplicado -> " + clientName);
                    server.removeClient(this);
                    socket.close();
                    return;
                }
            }

            send(new Message("INFO", "", "BROKER", "Conectado como " + clientName));
            server.log("Cliente conectado: " + clientName);
            topicManager.deliverPendingMessages(clientName);

            String lineInput;
            while ((lineInput = input.readLine()) != null) {
                Message msg = Message.fromString(lineInput);

                switch (msg.getType()) {
                    case "CREATE":
                        if (topicManager.create(msg.getTopic(), clientName, this)) {
                            send(new Message("SUBSCRIBED", msg.getTopic(), "BROKER",
                                    "Tópico criado e inscrição realizada."));
                        } else {
                            send(new Message("ERROR", msg.getTopic(), "BROKER",
                                    "Tópico já existe."));
                        }
                        break;

                    case "SUBSCRIBE":
                        if (topicManager.subscribe(msg.getTopic(), clientName)) {
                            send(new Message("SUBSCRIBED", msg.getTopic(), "BROKER",
                                    "Inscrito no tópico."));
                        } else {
                            send(new Message("ERROR", msg.getTopic(), "BROKER",
                                    "Tópico inexistente."));
                        }
                        break;

                    case "UNSUBSCRIBE":
                        topicManager.unsubscribe(msg.getTopic(), clientName);
                        send(new Message("UNSUBSCRIBED", msg.getTopic(), "BROKER",
                                "Saiu do tópico."));
                        break;

                    case "DELETE":
                        if (topicManager.delete(msg.getTopic(), clientName)) {
                            send(new Message("INFO", msg.getTopic(), "BROKER",
                                    "Tópico removido."));
                        } else {
                            send(new Message("ERROR", msg.getTopic(), "BROKER",
                                    "Você deve ser o único inscrito."));
                        }
                        break;

                    case "PUBLISH":
                        if (!topicManager.isSubscribed(msg.getTopic(), clientName)) {
                            send(new Message("ERROR", msg.getTopic(), "BROKER",
                                    "Você não está inscrito neste tópico."));
                            break;
                        }

                        topicManager.publish(
                                msg.getTopic(),
                                clientName,
                                msg.getContent()
                        );
                        break;

                    case "DISCONNECT":
                        topicManager.userDisconnected(clientName);
                        send(new Message("INFO", "", "BROKER", "Desconectado."));
                        socket.close();
                        return;
                }
            }

        } catch (Exception e) {
            System.out.println("Erro no cliente: " + e.getMessage());
            topicManager.userDisconnected(clientName);
            server.removeClient(this);
        } finally {
            if (clientName != null) {
                topicManager.userDisconnected(clientName);
            }
        }
    }

    public void send(Message message) {
        output.println(message.toString());
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
