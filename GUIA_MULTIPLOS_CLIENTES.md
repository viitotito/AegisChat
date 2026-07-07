# 🚀 Guia: Rodar AegisChat com Múltiplos Clientes

## Visão Geral

Este projeto suporta **múltiplos clientes simultâneos** conectados ao mesmo servidor, cada um com seu próprio certificado de autenticação.

---

## 📋 Pré-requisitos

1. **Certificados gerados** na pasta `certs/`
   - `server-ca.crt` - Certificado da CA do servidor
   - `server-ca.key` - Chave privada da CA (para gerar certificados)
   - `client-cert.crt` - Certificado padrão do cliente (opcional)
   - `client1-cert.crt`, `client2-cert.crt`, etc. - Certificados específicos dos clientes

2. **Projeto compilado**
   ```bash
   mvn clean install
   # OU pelo NetBeans: Clean and Build
   ```

---

## 🔑 Gerando Certificados

Se ainda não tem os certificados, execute **uma vez** na pasta `certs/`:

```bash
cd certs/

# Certificado da CA (apenas uma vez)
openssl genrsa -out server-ca.key 2048
openssl req -x509 -new -nodes -key server-ca.key -sha256 -days 3650 -out server-ca.crt -subj "//CN=ServerCA"

# Cliente 1
openssl genrsa -out client1.key 2048
openssl req -new -key client1.key -out client1.csr -subj "//CN=Cliente1"
openssl x509 -req -in client1.csr -CA server-ca.crt -CAkey server-ca.key -CAcreateserial -out client1-cert.crt -days 365 -sha256

# Cliente 2
openssl genrsa -out client2.key 2048
openssl req -new -key client2.key -out client2.csr -subj "//CN=Cliente2"
openssl x509 -req -in client2.csr -CA server-ca.crt -CAkey server-ca.key -CAcreateserial -out client2-cert.crt -days 365 -sha256

# Cliente 3 (opcional)
openssl genrsa -out client3.key 2048
openssl req -new -key client3.key -out client3.csr -subj "//CN=Cliente3"
openssl x509 -req -in client3.csr -CA server-ca.crt -CAkey server-ca.key -CAcreateserial -out client3-cert.crt -days 365 -sha256
```

✅ Isso garante que cada cliente tenha um CN único (Cliente1, Cliente2, etc.)

---

## 🖥️ Executando

### Opção 1: Via NetBeans (Fácil)

**1. Inicie o Servidor:**
- Clique direito em `aegis-server/ServerMain.java` → **Run File**
- Aguarde a mensagem: `"Broker iniciado na porta 5000"`

**2. Inicie múltiplos clientes:**
- Clique direito em `aegis-client/ClientMain.java` → **Run File** (primeira instância)
  - Digite: `localhost`, porta `5000`, nome **`Cliente1`**
  
- Clique direito novamente em `ClientMain.java` → **Run File** (segunda instância)
  - Digite: `localhost`, porta `5000`, nome **`Cliente2`**

- Repita para quantos clientes quiser

---

Quando a interface pedir dados:
- **Host:** `localhost`
- **Port:** `5000`
- **Name:** `Cliente1` ✅ (DEVE corresponder ao CN do certificado)

## 🔐 Como Funciona a Autenticação

1. **ClientApp** lê o `CLIENT_ID` e procura o certificado correspondente:
   - `CLIENT_ID=1` → usa `client1-cert.crt` (CN=Cliente1)
   - `CLIENT_ID=2` → usa `client2-cert.crt` (CN=Cliente2)
   - etc.

2. **ConfigLoader** busca os certificados **exclusivamente** na pasta `certs/`

3. **Servidor valida** que o nome digitado (`Cliente1`, `Cliente2`, etc.) corresponde ao CN do certificado

4. **Cada cliente** usa seu certificado único, então não há conflitos!

---

## ❌ Troubleshooting

**"Certificado não encontrado"**
- ✅ Garanta que `client1-cert.crt` existe em `certs/`
- ✅ Rode desde a raiz do projeto `AegisChat/`

**"Nome não corresponde ao certificado"**
- ✅ Se usar `CLIENT_ID=1`, digite **exatamente** `Cliente1` na interface
- ✅ Verifique o CN do certificado: `openssl x509 -in client1-cert.crt -text -noout`

**"Nome já está em uso"**
- ✅ Outro cliente já está conectado com esse nome
- ✅ Use um `CLIENT_ID` diferente ou mude o nome

**Conexão recusada**
- ✅ Servidor está rodando? Verifique se porta 5000 está livre
- ✅ Host/Port estão corretos?

---