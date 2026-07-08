# AegisChat

Sistema de comunicação **Publish/Subscribe** desenvolvido em Java utilizando Maven.

---

# Autenticação por certificados

O AegisChat utiliza certificados **X.509** para autenticação mútua entre cliente e broker.

A autenticação ocorre em duas etapas:

1. O **broker envia seu certificado** (`vitor.crt`).
2. O **cliente valida** esse certificado utilizando a **CA do professor** (`ca.crt`).
3. O cliente envia seu próprio certificado.
4. O **broker valida** o certificado do cliente utilizando a **CA criada pelo grupo** (`server-ca.crt`).

Essa arquitetura permite adicionar novos clientes sem depender de uma nova assinatura do professor.

---

# Estrutura da pasta `certs`

```
certs/
│
├── ca.crt                 # CA do professor
├── vitor.crt              # Certificado do broker assinado pelo professor
│
├── server-ca.crt          # CA criada pelo grupo
├── server-ca.key
├── server-ca.srl
│
├── client1.key
├── client1.csr
├── client1-cert.crt
│
├── client2.key
├── client2.csr
├── client2-cert.crt
│
└── client3-cert.crt ...
```

---

# Gerando a AC dos clientes

Execute apenas uma vez.

```bash
openssl genrsa -out server-ca.key 2048

openssl req -x509 \
-new \
-nodes \
-key server-ca.key \
-sha256 \
-days 3650 \
-out server-ca.crt \
-subj "/CN=ServerCA"
```

---

# Gerando um cliente

Exemplo para o Cliente1.

```bash
openssl genrsa -out client1.key 2048

openssl req \
-new \
-key client1.key \
-out client1.csr \
-subj "/CN=Cliente1"

openssl x509 \
-req \
-in client1.csr \
-CA server-ca.crt \
-CAkey server-ca.key \
-CAcreateserial \
-out client1-cert.crt \
-days 365 \
-sha256
```

Para novos clientes basta alterar o CN.

Exemplo:

```text
CN=Cliente2
CN=Cliente3
CN=Cliente4
...
```

---

# Funcionamento

Durante a conexão:

1. O broker envia `vitor.crt`.
2. O cliente valida `vitor.crt` usando `ca.crt`.
3. O cliente envia seu certificado (`clientX-cert.crt`).
4. O broker valida o certificado usando `server-ca.crt`.
5. O nome informado na interface deve corresponder ao **CN** presente no certificado.

Exemplo:

```
Certificado: client2-cert.crt
CN: Cliente2

Nome informado na interface:
Cliente2
```

---

# Observações

- `ca.crt` é fornecido pelo professor e **não deve ser alterado**.
- `vitor.crt` é o certificado do broker assinado pelo professor.
- `server-ca.crt` é a autoridade certificadora criada pelo grupo para emitir certificados dos clientes.
- Novos clientes podem ser criados localmente utilizando `server-ca.key`, sem necessidade de solicitar uma nova assinatura ao professor.