package client;

import model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

    public void connect() throws IOException {

        socket = new Socket(host, port);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        in.readLine();
        out.println(name);

        new Thread(new ReceiverThread(in, window)).start();
    }

    public void createTopic(String topic) {
        if (topic.isEmpty()) {
            window.appendMessage("[ERRO] Informe um tópico.");
            return;
        }

        out.println(new Message("CREATE_TOPIC", topic, name, ""));

        window.appendMessage("[INFO] Criando tópico e entrando: " + topic);
    }

    public void subscribeTopic(String topic) {
        if (topic.isEmpty()) {
            window.appendMessage("[ERRO] Informe um tópico.");
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
        out.println(new Message("DELETE_TOPIC", topic, name, ""));
    }

    public void publishTopic(String topic, String content) {

        if (topic.isEmpty()) {
            window.appendMessage("[ERRO] Informe um tópico.");
            return;
        }

        if (content.isEmpty()) {
            return;
        }

        out.println(new Message("PUBLISH", topic, name, content));
    }
}
