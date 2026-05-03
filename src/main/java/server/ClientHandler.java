package server;

import model.Message;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final TopicManager topicManager;

    private BufferedReader in;
    private PrintWriter out;
    private String clientName;

    public ClientHandler(Socket socket, TopicManager topicManager) {
        this.socket = socket;
        this.topicManager = topicManager;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Digite seu nome:");
            clientName = in.readLine();

            send(new Message("INFO", "", "BROKER",
                    "Conectado como " + clientName));

            String line;

            while ((line = in.readLine()) != null) {

                Message msg = Message.fromString(line);

                switch (msg.getType()) {

                    case "CREATE_TOPIC":
                        if (topicManager.createTopic(msg.getTopic(), this)) {
                            send(new Message("INFO", msg.getTopic(), "BROKER",
                                    "Tópico criado e inscrição realizada."));
                        } else {
                            send(new Message("ERROR", msg.getTopic(), "BROKER",
                                    "Tópico já existe."));
                        }
                        break;

                    case "SUBSCRIBE":
                        if (topicManager.subscribeTopic(msg.getTopic(), this)) {
                            send(new Message("INFO", msg.getTopic(), "BROKER",
                                    "Inscrito no tópico."));
                        } else {
                            send(new Message("ERROR", msg.getTopic(), "BROKER",
                                    "Tópico inexistente."));
                        }
                        break;

                    case "UNSUBSCRIBE":
                        topicManager.unsubscribeTopic(msg.getTopic(), this);
                        send(new Message("INFO", msg.getTopic(), "BROKER",
                                "Saiu do tópico."));
                        break;

                    case "DELETE_TOPIC":
                        if (topicManager.deleteTopic(msg.getTopic(), this)) {
                            send(new Message("INFO", msg.getTopic(), "BROKER",
                                    "Tópico removido."));
                        } else {
                            send(new Message("ERROR", msg.getTopic(), "BROKER",
                                    "Você deve ser o único inscrito."));
                        }
                        break;

                    case "PUBLISH":
                        topicManager.publishTopic(
                                msg.getTopic(),
                                clientName,
                                msg.getContent()
                        );
                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("Erro no cliente: " + e.getMessage());
        }
    }

    public void send(Message message) {
        out.println(message.toString());
    }
}