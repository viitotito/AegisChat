package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import model.Message;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final TopicManager topicManager;

    private BufferedReader input;
    private PrintWriter output;

    private String clientName;

    public String getClientName() {
        return clientName;
    }

    public ClientHandler(Socket socket, TopicManager topicManager) {
        this.socket = socket;
        this.topicManager = topicManager;
    }

    public void send(Message message) {
        output.println(message.toString());
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            output.println("Digite seu nome:");
            clientName = input.readLine();

            send(new Message("INFO", "", "BROKER", "Conectado como " + clientName));

            String line;

            while ((line = input.readLine()) != null) {

                Message message = Message.fromString(line);

                switch (message.getType()) {
                    case "CREATE":
                        if (topicManager.create(message.getTopic(), this)) {
                            send(new Message("INFO", message.getTopic(), "BROKER", "Tópico " + message.getTopic() + " criado."));
                        } else {
                            send(new Message("ERROR", message.getTopic(), "BROKER", "Tópico " + message.getTopic() + " já existe."));
                        }
                        break;

                    case "SUBSCRIBE":
                        if (topicManager.subscribe(message.getTopic(), this)) {
                            send(new Message("INFO", message.getTopic(), "BROKER", "Inscrito no tópico " + message.getTopic() + "."));
                        } else {
                            send(new Message("ERROR", message.getTopic(), "BROKER", "Tópico " + message.getTopic() + " inexistente."));
                        }
                        break;

                    case "UNSUBSCRIBE":
                        topicManager.unsubscribe(message.getTopic(), this);
                        send(new Message("INFO", message.getTopic(), "BROKER", "Saiu do tópico " + message.getTopic() + "."));
                        break;

                    case "DELETE":
                        if (topicManager.delete(message.getTopic(), this)) {
                            send(new Message("INFO", message.getTopic(), "BROKER", "Tópico " + message.getTopic() + " removido ."));
                        } else {
                            send(new Message("ERROR", message.getTopic(), "BROKER", "Você deve ser o único inscrito no tópico " + message.getTopic() + "."));
                        }
                        break;

                    case "PUBLISH":
                        topicManager.publish(message.getTopic(), this, message.getContent());
                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("Erro ao conectar cliente: " + e.getMessage());
        }
    }

}
