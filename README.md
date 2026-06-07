# Aegis-Chat-
Sistema de comunicação pub/subscribe desenvolvido em Java com Maven.

## Autenticação por certificado
O cliente e o broker usam certificados X.509 para autenticação offline. O servidor deve assinar o certificado do cliente com sua autoridade de certificação (CA).

### Arquivos esperados
- `server-ca.crt`: certificado da CA do servidor.
- `client-cert.crt`: certificado do cliente assinado pela CA do servidor.

### Geração usando OpenSSL
Execute no terminal:

```bash
openssl genrsa -out server-ca.key 2048
openssl req -x509 -new -nodes -key server-ca.key -sha256 -days 3650 -out server-ca.crt -subj "//CN=ServerCA"

openssl genrsa -out client.key 2048
openssl req -new -key client.key -out client.csr -subj "//CN=Cliente1"
openssl x509 -req -in client.csr -CA server-ca.crt -CAkey server-ca.key -CAcreateserial -out client-cert.crt -days 365 -sha256
```

Use `client-cert.crt` no cliente e `server-ca.crt` no cliente e no servidor.
