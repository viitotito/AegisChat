Diretório para certificados usados pela aplicação.

Uso seguro:
- Não comite chaves privadas (`*.key`, `*.p12`, `*.pem`). Elas já estão listadas em `.gitignore`.
- Adicione apenas certificados públicos se necessário (ex.: `server-ca.crt`, `client-cert.crt`) e com cuidado.

Coloque aqui os arquivos localmente e atualize `application.properties` com caminhos relativos, por exemplo:

```
server.cert.path=certs/server-ca.crt
client.cert.path=certs/client-cert.crt
```
