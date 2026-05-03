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
            while (true) {
                String line = in.readLine();

                if (line == null) {
                    throw new Exception("Servidor caiu");
                }

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
                            JOptionPane.showMessageDialog(window,
                                    message.getContent(),
                                    "Erro",
                                    JOptionPane.ERROR_MESSAGE);
                            break;
                    }
                });
            }

        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                window.appendMessage("[ERRO] Servidor desconectado. Conexão encerrada.");
                window.setDisconnected();
            });
        } finally {
            try {
                in.close();
            } catch (Exception ignored) {
            }
        }
    }
}
