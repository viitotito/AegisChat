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

### Opção 2: Via Terminal PowerShell (Recomendado para múltiplos)

**1. Compile o projeto:**
```powershell
cd c:\Users\fabri\Downloads\AegisChat
mvn clean install  # Ou se não funcionar, compile via NetBeans
```

**2. Terminal 1 - Servidor:**
```powershell
cd c:\Users\fabri\Downloads\AegisChat

# Define CLIENT_ID e roda o servidor
java -cp aegis-server/target/classes:aegis-model/target/classes server.ServerMain
```

**3. Terminal 2 - Cliente 1:**
```powershell
cd c:\Users\fabri\Downloads\AegisChat

# Define CLIENT_ID=1
$env:CLIENT_ID=1
java -cp aegis-client/target/classes:aegis-model/target/classes client.ClientMain
```

Quando a interface pedir dados:
- **Host:** `localhost`
- **Port:** `5000`
- **Name:** `Cliente1` ✅ (DEVE corresponder ao CN do certificado)

**4. Terminal 3 - Cliente 2:**
```powershell
cd c:\Users\fabri\Downloads\AegisChat

# Define CLIENT_ID=2
$env:CLIENT_ID=2
java -cp aegis-client/target/classes:aegis-model/target/classes client.ClientMain
```

Quando a interface pedir dados:
- **Host:** `localhost`
- **Port:** `5000`
- **Name:** `Cliente2` ✅ (DEVE corresponder ao CN do certificado)

**5. Terminal 4 - Cliente 3 (opcional):**
```powershell
$env:CLIENT_ID=3
java -cp aegis-client/target/classes:aegis-model/target/classes client.ClientMain
```

Quando a interface pedir dados:
- **Host:** `localhost`
- **Port:** `5000`
- **Name:** `Cliente3` ✅

---

### Opção 3: Script Automatizado (.bat)

Crie um arquivo `run_clients.bat` na raiz do projeto:

```batch
@echo off
cd c:\Users\fabri\Downloads\AegisChat

REM Servidor em janela nova
start "Servidor" cmd /k "java -cp aegis-server/target/classes;aegis-model/target/classes server.ServerMain"

REM Aguarda 2 segundos
timeout /t 2

REM Cliente 1
start "Cliente 1" cmd /k "set CLIENT_ID=1 && java -cp aegis-client/target/classes;aegis-model/target/classes client.ClientMain"

REM Cliente 2
start "Cliente 2" cmd /k "set CLIENT_ID=2 && java -cp aegis-client/target/classes;aegis-model/target/classes client.ClientMain"

REM Cliente 3
start "Cliente 3" cmd /k "set CLIENT_ID=3 && java -cp aegis-client/target/classes;aegis-model/target/classes client.ClientMain"
```

Execute:
```bash
run_clients.bat
```

Vai abrir 4 janelas automaticamente! 🎉

---

## 🔐 Como Funciona a Autenticação

1. **ClientApp** lê o `CLIENT_ID` e procura o certificado correspondente:
   - `CLIENT_ID=1` → usa `client1-cert.crt` (CN=Cliente1)
   - `CLIENT_ID=2` → usa `client2-cert.crt` (CN=Cliente2)
   - etc.

2. **ConfigLoader** busca os certificados **exclusivamente** na pasta `certs/`

3. **Servidor valida** que o nome digitado (`Cliente1`, `Cliente2`, etc.) corresponde ao CN do certificado

4. **Cada cliente** usa seu certificado único, então não há conflitos!

---

## ✅ Checklist para Múltiplos Clientes

- [ ] Certificados gerados (CA + Cliente1, Cliente2, etc.)
- [ ] Projeto compilado com `mvn clean install`
- [ ] Servidor rodando
- [ ] Variável `CLIENT_ID` definida para cada cliente
- [ ] Nome digitado corresponde ao CN do certificado
- [ ] Cada cliente em um terminal/janela separada

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

## 📝 Resumo Técnico

| Recurso | Localização | Descrição |
|---------|------------|-----------|
| Certificados | `certs/` | CA e certificados dos clientes |
| ConfigLoader | `aegis-client/ConfigLoader.java` | Lê CLIENT_ID e busca certificados |
| | `aegis-server/ConfigLoader.java` | Lê configuração do servidor |
| Validação | `aegis-server/ClientHandler.java` | Valida nome vs. CN do certificado |
| Múltiplos clientes | BrokerServer.java | Suporta via ConcurrentHashMap |

---

**Pronto! Você pode agora rodar quantos clientes quiser simultaneamente! 🚀**
