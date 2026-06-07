package client;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                // Tenta carregar do diretório raiz (quando rodado via IDE)
                java.nio.file.Files.lines(java.nio.file.Paths.get("application.properties"))
                    .filter(line -> !line.startsWith("#") && !line.trim().isEmpty())
                    .forEach(line -> {
                        String[] parts = line.split("=", 2);
                        if (parts.length == 2) {
                            properties.setProperty(parts[0].trim(), parts[1].trim());
                        }
                    });
            } else {
                properties.load(input);
            }
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível carregar application.properties. Usando valores padrão.");
        }
    }
    
    public static String getClientCertPath() {
        return properties.getProperty("client.cert.path", "client-cert.crt");
    }
    
    public static String getServerCertPath() {
        return properties.getProperty("server.cert.path", "server-ca.crt");
    }
}
