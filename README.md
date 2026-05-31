# RAG Backend — Spring Boot + LangChain4j + Chroma + Claude

A production-ready **Retrieval-Augmented Generation (RAG)** backend that lets you:
1. **Ingest** documents (raw text or PDF files)
2. **Query** them with natural language — Claude answers using only what's in your knowledge base

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| LLM Integration | LangChain4j 0.31 |
| LLM | Anthropic Claude 3.5 Sonnet |
| Embeddings | AllMiniLmL6V2 (local, no API key) |
| Vector Store | ChromaDB (local Docker) |
| Document Parsing | Apache PDFBox |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- An Anthropic API key → https://console.anthropic.com

---

## Setup

### 1. Start ChromaDB
```bash
docker-compose up -d
```

### 2. Set your API key
```bash
export ANTHROPIC_API_KEY=sk-ant-...
```

### 3. Run the app
```bash
mvn spring-boot:run
```

The server starts on **http://localhost:8080**

---

## API Reference

### Ingest raw text
```http
POST /api/documents/ingest
Content-Type: application/json

{
  "content": "Your document text here...",
  "source": "my-doc"
}
```

**Response:**
```json
{ "chunksStored": 4, "message": "4 chunk(s) successfully ingested into Chroma." }
```

---

### Ingest a file (TXT or PDF)
```http
POST /api/documents/upload
Content-Type: multipart/form-data

file=@/path/to/document.pdf
```

---

### Ask a question
```http
POST /api/query
Content-Type: application/json

{
  "question": "What is the refund policy?",
  "maxResults": 3
}
```

**Response:**
```json
{
  "answer": "Based on the provided context, the refund policy states...",
  "sources": [
    {
      "text": "Relevant chunk from your document...",
      "score": 0.87,
      "source": "my-doc"
    }
  ]
}
```

---

## Configuration

All settings live in `src/main/resources/application.yml`:

| Property | Default | Description |
|---|---|---|
| `app.anthropic.model-name` | `claude-3-5-sonnet-20241022` | Claude model |
| `app.anthropic.max-tokens` | `1024` | Max response tokens |
| `app.chroma.url` | `http://localhost:8000` | Chroma endpoint |
| `app.chroma.collection-name` | `documents` | Chroma collection |
| `app.rag.max-results` | `5` | Chunks retrieved per query |
| `app.rag.min-score` | `0.6` | Minimum similarity threshold |
| `app.rag.chunk-size` | `500` | Characters per chunk |
| `app.rag.chunk-overlap` | `50` | Overlap between chunks |

---

## Project Structure

```
src/main/java/com/ragapp/
├── RagApplication.java            # Entry point
├── config/
│   ├── AppProperties.java         # Typed config properties
│   └── LangChainConfig.java       # Claude, embeddings, Chroma beans
├── controller/
│   ├── DocumentController.java    # POST /api/documents/*
│   ├── QueryController.java       # POST /api/query
│   └── GlobalExceptionHandler.java
├── model/
│   └── Dto.java                   # Request/Response DTOs
└── service/
    ├── DocumentIngestionService.java  # Split → embed → store
    └── RagQueryService.java           # Retrieve → prompt → Claude
```

## How It Works

```
[User uploads doc]
      │
      ▼
DocumentIngestionService
  ├── Split into chunks (recursive splitter)
  ├── Embed each chunk (AllMiniLmL6V2, local)
  └── Store in ChromaDB

[User asks question]
      │
      ▼
RagQueryService
  ├── Embed question
  ├── Find top-K similar chunks from Chroma
  ├── Build prompt: System + Context + Question
  └── Call Claude → return answer + source chunks
```
