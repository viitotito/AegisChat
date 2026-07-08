package model;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class CertificateUtils {

    public static X509Certificate loadCertificate(String path) throws Exception {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String pem = new String(bytes, StandardCharsets.UTF_8);
        return parsePemCertificate(pem);
    }

    public static X509Certificate decodeCertificate(String base64) throws Exception {
        byte[] der = Base64.getDecoder().decode(base64);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(der));
    }

    public static String encodeCertificate(X509Certificate certificate) throws Exception {
        return Base64.getEncoder().encodeToString(certificate.getEncoded());
    }

    public static boolean verifyCertificate(X509Certificate certificate, X509Certificate issuerCertificate) {
        try {
            certificate.verify(issuerCertificate.getPublicKey());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getCommonName(X509Certificate certificate) {
        String subject = certificate.getSubjectX500Principal().getName();
        for (String part : subject.split(",")) {
            part = part.trim();
            if (part.startsWith("CN=")) {
                return part.substring(3);
            }
        }
        return "";
    }

    private static X509Certificate parsePemCertificate(String pem) throws Exception {
        String normalized = pem
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", "");

        byte[] der = Base64.getDecoder().decode(normalized);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(der));
    }
}
