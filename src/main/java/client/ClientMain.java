package client;

public class ClientMain {
      public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new ClientWindow().setVisible(true);
        });
    }
}
