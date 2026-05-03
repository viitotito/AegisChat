package client;

import model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JOptionPane;
import model.ConnectResult;

public class ClientApp {

    private final String host;
    private final int port;
    private final String name;
    private final ChatWindow window;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientApp(String host, int port, String name, ChatWindow window) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.window = window;
    }

    public ConnectResult connect() {
        try {
            socket = new Socket(host, port);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            in.readLine(); 
            out.println(name);

            String response = in.readLine();

            if (response == null) {
                return ConnectResult.SERVER_OFFLINE;
            }

            Message msg = Message.fromString(response);

            if ("ERROR".equals(msg.getType())) {
                return ConnectResult.NAME_IN_USE;
            }

            new Thread(new ReceiverThread(in, window)).start();

            return ConnectResult.SUCCESS;

        } catch (IOException e) {
            return ConnectResult.SERVER_OFFLINE;
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
}
