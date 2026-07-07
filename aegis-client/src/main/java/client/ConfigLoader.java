package client;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Properties;

public class ConfigLoader {
    
    private static final Path CERTS_DIR = resolveCertsDirectory();
    private static final Properties properties = loadProperties();
    
    private static Path resolveCertsDirectory() {
        // Tenta encontrar a pasta 'certs' começando do diretório atual
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        
        while (current != null) {
            Path certsPath = current.resolve("certs");
            if (Files.isDirectory(certsPath)) {
                System.out.println("[ConfigLoader] Pasta 'certs' encontrada em: " + certsPath);
                return certsPath;
            }
            current = current.getParent();
        }
        
        throw new RuntimeException("ERRO: Pasta 'certs' não encontrada! Certifique-se de executar desde a raiz do projeto AegisChat.");
    }
    
    private static Properties loadProperties() {
        Properties props = new Properties();
        Path propsFile = CERTS_DIR.resolve("application.properties");
        
        if (Files.exists(propsFile)) {
            try {
                props.load(Files.newInputStream(propsFile));
                System.out.println("[ConfigLoader] application.properties carregado de: " + propsFile);
            } catch (Exception e) {
                System.err.println("[ConfigLoader] Erro ao carregar application.properties: " + e.getMessage());
            }
        } else {
            System.out.println("[ConfigLoader] Nenhum application.properties encontrado em certs/. Usando valores padrão.");
        }
        
        return props;
    }
    
    public static String getClientCertPath() {
        // PRIORIDADE 1: Tenta obter CLIENT_ID da variável de ambiente ou propriedade do sistema
        String clientId = System.getenv("CLIENT_ID");
        if (clientId == null) {
            clientId = System.getProperty("client.id");
        }
        
        if (clientId != null && !clientId.isEmpty()) {
            String certFileName = "client" + clientId + "-cert.crt";
            System.out.println("[ConfigLoader] CLIENT_ID encontrado: " + clientId + " -> usando " + certFileName);
            Path resolvedPath = CERTS_DIR.resolve(certFileName);
            if (!Files.exists(resolvedPath)) {
                throw new RuntimeException("ERRO: Certificado do cliente não encontrado em " + resolvedPath);
            }
            return resolvedPath.toString();
        }
        
        // PRIORIDADE 2: Tenta ler do application.properties em certs/
        String certPath = properties.getProperty("client.cert.path");
        if (certPath != null && !certPath.isEmpty()) {
            System.out.println("[ConfigLoader] Usando client.cert.path do application.properties: " + certPath);
            Path resolvedPath = resolvePath(certPath);
            if (!Files.exists(resolvedPath)) {
                throw new RuntimeException("ERRO: Certificado do cliente não encontrado em " + resolvedPath);
            }
            return resolvedPath.toString();
        }
        
        // FALLBACK: certificado padrão
        String certFileName = "client-cert.crt";
        System.out.println("[ConfigLoader] Usando certificado padrão (nenhum CLIENT_ID ou properties definido)");
        Path resolvedPath = CERTS_DIR.resolve(certFileName);
        if (!Files.exists(resolvedPath)) {
            throw new RuntimeException("ERRO: Certificado do cliente não encontrado em " + resolvedPath);
        }
        return resolvedPath.toString();
    }
    
    public static String getServerCertPath() {
        // Primeiro tenta obter do properties (se existir application.properties em certs/)
        String certPath = properties.getProperty("server.cert.path");
        
        if (certPath != null && !certPath.isEmpty()) {
            System.out.println("[ConfigLoader] Usando server.cert.path do application.properties: " + certPath);
            Path resolvedPath = resolvePath(certPath);
            if (!Files.exists(resolvedPath)) {
                throw new RuntimeException("ERRO: Certificado da CA do servidor não encontrado em " + resolvedPath);
            }
            return resolvedPath.toString();
        }
        
        // Fallback para valor padrão
        Path certFilePath = CERTS_DIR.resolve("server-ca.crt");
        if (!Files.exists(certFilePath)) {
            throw new RuntimeException("ERRO: Certificado da CA do servidor não encontrado em " + certFilePath);
        }
        return certFilePath.toString();
    }
    
    public static Path getCertsDirectoryPath() {
        return CERTS_DIR;
    }
    
    private static Path resolvePath(String pathStr) {
        Path path = Paths.get(pathStr);
        // Se for caminho relativo, resolve em relação a CERTS_DIR ou diretório atual
        if (!path.isAbsolute()) {
            // Tenta em relação a CERTS_DIR primeiro
            if (Files.exists(CERTS_DIR.resolve(path))) {
                return CERTS_DIR.resolve(path);
            }
            // Depois em relação ao diretório atual
            return Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }
}
