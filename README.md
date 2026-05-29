# Vocash — Assistente Financeiro por Voz

API REST que permite ao usuário registrar e consultar gastos pessoais usando a **voz**. O áudio é transcrito automaticamente e interpretado por um LLM, que decide qual ação executar sem necessidade de formulários ou comandos textuais.

---

## Sumário

- [Descrição](#descrição)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Como rodar com Docker](#como-rodar-com-docker)
- [Como usar o endpoint](#como-usar-o-endpoint)

---

## Descrição

O usuário envia um arquivo de áudio e o Vocash:

1. **Transcreve** a fala com o modelo **Whisper** (via Groq).
2. **Interpreta** o texto com o LLM **llama-3.3-70b-versatile** (via Groq).
3. **Executa** automaticamente a ação correta por meio de *tool calling* do Spring AI:
   - `registrarGasto` — persiste o gasto no PostgreSQL.
   - `gerarRelatorio` — consulta e formata os gastos registrados.
4. **Retorna** uma resposta amigável em português.

**Stack:** Java 21 · Spring Boot 3.5 · Spring AI 1.0 · PostgreSQL 16 · Groq API.

---

## Arquitetura

O projeto segue **Domain-Driven Design (DDD)** com quatro camadas bem delimitadas:

```
interfaces.web          → Recebe a requisição HTTP (AssistenteController)
       ↓
application.service     → Orquestra o pipeline de IA (OrquestradorService)
                          e define as tools do LLM (GastoTools)
       ↓
domain                  → Entidade Gasto + contrato GastoRepository
       ↓
infrastructure          → Transcrição Whisper (TranscricaoService)
                          + persistência JPA (GastoRepositoryImpl)
```

### Pipeline detalhado

```
POST /api/v1/assistente/processar
        │
        ▼
AssistenteController
        │  MultipartFile (áudio)
        ▼
OrquestradorService
        │
        ├─► TranscricaoService ──► Groq Whisper API ──► texto transcrito
        │
        └─► ChatClient (LLM llama-3.3-70b-versatile)
                │
                ├─► [intenção: registrar gasto]
                │       └─► GastoTools.registrarGasto()
                │               └─► GastoRepositoryImpl.salvar()
                │                       └─► PostgreSQL
                │
                └─► [intenção: gerar relatório]
                        └─► GastoTools.gerarRelatorio()
                                └─► GastoRepositoryImpl.buscarTodos()
                                        └─► PostgreSQL
```

### Estrutura de pacotes

```
src/main/java/com/davi/vocash/
├── VocashApplication.java
├── application/
│   └── service/
│       ├── GastoTools.java          # Tools @Tool expostas ao LLM
│       └── OrquestradorService.java # Orquestração do pipeline
├── domain/
│   ├── model/
│   │   └── Gasto.java               # Entidade de domínio
│   └── repository/
│       └── GastoRepository.java     # Porta de saída (interface)
├── infrastructure/
│   ├── TranscricaoService.java      # Integração Whisper / Groq
│   └── persistence/
│       ├── GastoJpaRepository.java  # Spring Data JPA (package-private)
│       └── GastoRepositoryImpl.java # Adapter JPA → GastoRepository
└── interfaces/
    └── web/
        └── AssistenteController.java # Controller REST
```

---

## Pré-requisitos

| Requisito | Versão mínima |
|-----------|---------------|
| Java | 21 |
| Maven | 3.9+ |
| Docker + Docker Compose | qualquer versão recente |
| Conta na [Groq](https://console.groq.com) | — |

---

## Variáveis de ambiente

| Variável | Descrição | Obrigatória |
|----------|-----------|-------------|
| `GROQ_API_KEY` | Chave de API da Groq (usada tanto para o LLM quanto para o Whisper) | Sim |

Para obter a chave: acesse [console.groq.com](https://console.groq.com), crie uma conta e gere uma API key em **API Keys**.

Configure no terminal antes de iniciar a aplicação:

```bash
# Linux / macOS
export GROQ_API_KEY=gsk_...

# Windows (PowerShell)
$env:GROQ_API_KEY = "gsk_..."
```

---

## Como rodar com Docker

O `docker-compose.yml` sobe apenas o **PostgreSQL**. A aplicação Spring Boot é executada localmente (ou pode ser conteinerizada à parte).

### 1. Subir o banco

```bash
docker compose up -d
```

Isso cria o banco `vocash` em `localhost:5432` com usuário `postgres` e senha `postgres`. O Hibernate cria a tabela `gastos` automaticamente na primeira execução (`ddl-auto: update`).

### 2. Iniciar a aplicação

```bash
# Com Maven Wrapper
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

### 3. Parar o banco

```bash
docker compose down
```

Para remover também o volume de dados:

```bash
docker compose down -v
```

---

## Como usar o endpoint

### `POST /api/v1/assistente/processar`

Envia um arquivo de áudio e recebe a resposta do assistente em texto.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `audio` | `multipart/form-data` | Arquivo de áudio (mp3, wav, m4a, etc. — até 25 MB) |

**Resposta:** `200 OK` com o texto da resposta do assistente no corpo.

---

#### Exemplo — registrar um gasto

```bash
curl -X POST http://localhost:8080/api/v1/assistente/processar \
  -F "audio=@/caminho/para/audio.mp3"
```

O usuário diz no áudio: *"Gastei 45 reais no almoço hoje no restaurante Sabor & Arte."*

Resposta esperada:
```
Gasto de R$ 45,00 em alimentação registrado com sucesso! 🍽️
```

---

#### Exemplo — consultar relatório

O usuário diz: *"Quanto eu gastei esse mês?"*

Resposta esperada:
```
Relatório de gastos:
- R$ 45,00 em alimentacao (almoço) em 2025-05-29
- R$ 12,50 em transporte (Uber) em 2025-05-29
Total: R$ 57,50
```

---

#### Exemplo com Python

```python
import requests

with open("audio.mp3", "rb") as f:
    response = requests.post(
        "http://localhost:8080/api/v1/assistente/processar",
        files={"audio": ("audio.mp3", f, "audio/mpeg")}
    )

print(response.text)
```

---

#### Exemplo com Postman / Insomnia

1. Método: `POST`
2. URL: `http://localhost:8080/api/v1/assistente/processar`
3. Body: `form-data`
   - Key: `audio` (tipo **File**)
   - Value: selecionar o arquivo de áudio

---

## Configuração avançada

As configurações da aplicação ficam em [`src/main/resources/application.yml`](src/main/resources/application.yml).

| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| `spring.ai.openai.chat.options.model` | `llama-3.3-70b-versatile` | Modelo LLM da Groq |
| `spring.ai.openai.audio.transcription.options.model` | `whisper-large-v3` | Modelo de transcrição |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5432/vocash` | URL do banco |
| `spring.servlet.multipart.max-file-size` | `25MB` | Tamanho máximo do áudio |
