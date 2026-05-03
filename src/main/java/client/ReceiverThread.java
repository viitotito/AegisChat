package client;

import model.Message;

import javax.swing.*;
import java.io.BufferedReader;

public class ReceiverThread implements Runnable {

    private final BufferedReader in;
    private final ChatWindow window;

    public ReceiverThread(BufferedReader in, ChatWindow window) {
        this.in = in;
        this.window = window;
    }

    @Override
    public void run() {

        try {
            String line;

            while ((line = in.readLine()) != null) {

                Message message = Message.fromString(line);

                SwingUtilities.invokeLater(() -> {
                    switch (message.getType()) {

                        case "MESSAGE":
                            window.appendMessage("[" + message.getTopic() + "] "
                                    + message.getSender() + ": "
                                    + message.getContent());
                            break;

                        case "INFO":
                            window.appendMessage("[INFO] " + message.getContent());
                            break;

                        case "ERROR":
                            window.appendMessage("[ERRO] " + message.getContent());
                            break;
                    }
                });
            }

        } catch (Exception e) {
            window.appendMessage("[ERRO] Conexão encerrada.");
        } finally {
            try {
                in.close();
            } catch (Exception ignored) {
            }
        }
    }
}
