package model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigLoader {

    private static final Path CERTS_DIR = resolveCertsDirectory();
    private static final Properties properties = loadProperties();

    private static Path resolveCertsDirectory() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

        while (current != null) {
            Path certsPath = current.resolve("certs");

            if (Files.isDirectory(certsPath)) {
                System.out.println("[ConfigLoader] Pasta 'certs' encontrada em: " + certsPath);
                return certsPath;
            }

            current = current.getParent();
        }

        throw new RuntimeException(
                "ERRO: Pasta 'certs' não encontrada! Certifique-se de executar desde a raiz do projeto AegisChat."
        );
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

    public static Path getCertsDirectoryPath() {
        return CERTS_DIR;
    }

    private static Path resolvePath(String pathStr) {

        Path path = Paths.get(pathStr);

        if (!path.isAbsolute()) {

            if (Files.exists(CERTS_DIR.resolve(path))) {
                return CERTS_DIR.resolve(path);
            }

            return Paths.get(System.getProperty("user.dir")).resolve(path);
        }

        return path;
    }

    /**
     * Certificado do broker (assinado pelo professor)
     */
    public static String getBrokerCertificatePath() {

        Path certFile = CERTS_DIR.resolve("vitor.crt");

        if (!Files.exists(certFile)) {
            throw new RuntimeException("Certificado do broker não encontrado.");
        }

        return certFile.toString();
    }

    /**
     * AC do professor.
     * Usada pelo CLIENTE para validar o broker.
     */
    public static String getBrokerCAPath() {

        Path certFile = CERTS_DIR.resolve("ca.crt");

        if (!Files.exists(certFile)) {
            throw new RuntimeException("CA do broker não encontrada.");
        }

        return certFile.toString();
    }

    /**
     * AC criada pelo grupo.
     * Usada pelo BROKER para validar os clientes.
     */
    public static String getClientCAPath() {

        Path certFile = CERTS_DIR.resolve("server-ca.crt");

        if (!Files.exists(certFile)) {
            throw new RuntimeException("CA dos clientes não encontrada.");
        }

        return certFile.toString();
    }
}