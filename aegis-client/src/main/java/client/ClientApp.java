package client;

import model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.cert.X509Certificate;
import javax.swing.JOptionPane;
import model.CertificateUtils;
import model.ConfigLoader;
import model.ConnectResult;

public class ClientApp {

    private final String clientCertPath;
    private final String serverCertPath;

    private final String host;
    private final int port;
    private final String name;
    private final ChatWindow window;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String failureReason = "";

    public ClientApp(String host, int port, String name, ChatWindow window, String selectedClientCertPath) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.window = window;
        this.clientCertPath = selectedClientCertPath;
        this.serverCertPath = ConfigLoader.getBrokerCAPath();
    }

    public ConnectResult connect() {
        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            failureReason = "Não foi possível conectar ao servidor. Verifique se ele está online.";
            return ConnectResult.SERVER_OFFLINE;
        }

        X509Certificate clientCert;
        X509Certificate caCert;

        try {
            clientCert = CertificateUtils.loadCertificate(clientCertPath);
            caCert = CertificateUtils.loadCertificate(serverCertPath);
        } catch (IOException e) {
            closeSocket();
            failureReason = "Falha ao carregar certificados locais. Verifique os arquivos: " + clientCertPath + " e " + serverCertPath;
            return ConnectResult.AUTH_FAILED;
        } catch (Exception e) {
            closeSocket();
            failureReason = "Erro ao carregar ou validar certificados: " + e.getMessage();
            return ConnectResult.AUTH_FAILED;
        }

        try {

            String response = in.readLine();

            if (response == null) {
                closeSocket();
                failureReason = "O servidor não respondeu.";
                return ConnectResult.SERVER_OFFLINE;
            }

            Message brokerMessage = Message.fromString(response);

            if (!"BROKER_CERT".equals(brokerMessage.getType())) {
                closeSocket();
                failureReason = "O broker não enviou seu certificado.";
                return ConnectResult.AUTH_FAILED;
            }

            X509Certificate brokerCertificate = CertificateUtils.decodeCertificate(
                    brokerMessage.getContent()
            );

            if (!CertificateUtils.verifyCertificate(
                    brokerCertificate,
                    caCert
            )) {

                closeSocket();

                failureReason = "O certificado do broker é inválido.";

                return ConnectResult.AUTH_FAILED;
            }

            out.println(
                    new Message(
                            "CERT",
                            "",
                            "",
                            CertificateUtils.encodeCertificate(clientCert)
                    )
            );

            response = in.readLine();
            if (response == null) {
                closeSocket();
                failureReason = "O servidor não respondeu ao pedido de nome.";
                return ConnectResult.SERVER_OFFLINE;
            }

            brokerMessage = Message.fromString(response);
            if (!"REQUEST_NAME".equals(brokerMessage.getType())) {
                closeSocket();
                failureReason = "Resposta inesperada do servidor.";
                return ConnectResult.SERVER_OFFLINE;
            }

            out.println(new Message("NAME", "", name, ""));

            response = in.readLine();
            if (response == null) {
                closeSocket();
                failureReason = "O servidor não respondeu após o envio do nome.";
                return ConnectResult.SERVER_OFFLINE;
            }

            Message msg = Message.fromString(response);
            if ("ERROR".equals(msg.getType())) {
                if (msg.getContent().contains("Nome já está em uso")) {
                    failureReason = msg.getContent();
                    closeSocket();
                    return ConnectResult.NAME_IN_USE;
                }
                failureReason = msg.getContent();
                closeSocket();
                return ConnectResult.AUTH_FAILED;
            }

            new Thread(new ReceiverThread(in, window)).start();
            return ConnectResult.SUCCESS;
        } catch (IOException e) {
            closeSocket();
            failureReason = "Erro de conexão com o servidor: " + e.getMessage();
            return ConnectResult.SERVER_OFFLINE;
        } catch (Exception e) {
            closeSocket();
            failureReason = "Erro inesperado: " + e.getMessage();
            return ConnectResult.AUTH_FAILED;
        }
    }

    public void createTopic(String topic) {
        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(window,
                    "Informe um tópico.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        out.println(new Message("CREATE", topic, name, ""));

        window.appendMessage("[INFO] Criando tópico e entrando: " + topic);
    }

    public void subscribeTopic(String topic) {
        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(window,
                    "Informe um tópico.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        out.println(new Message("SUBSCRIBE", topic, name, ""));

        window.appendMessage("[INFO] Entrando no tópico: " + topic);
    }

    public void unsubscribeTopic(String topic) {
        out.println(new Message("UNSUBSCRIBE", topic, name, ""));

        window.appendMessage("[INFO] Saiu do tópico: " + topic);
    }

    public void deleteTopic(String topic) {
        out.println(new Message("DELETE", topic, name, ""));
    }

    public void publishTopic(String topic, String content) {

        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(window,
                    "Informe um tópico.",
                    "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (content.isEmpty()) {
            return;
        }
        if (out == null) {
            window.appendMessage("[ERRO] Sem conexão com o servidor.");
            return;
        }
        out.println(new Message("PUBLISH", topic, name, content));
    }

    public void disconnect() {
        try {
            if (out != null) {
                out.println(new Message("DISCONNECT", "", name, ""));
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }

    public String getFailureReason() {
        return failureReason;
    }

    private void closeSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
