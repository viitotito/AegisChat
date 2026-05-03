package server;

public class ServerMain {

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> {
            new BrokerWindow().setVisible(true);
        });
    }
}
