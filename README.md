# RAG Backend — Spring Boot + LangChain4j + Groq

A production-ready **Retrieval-Augmented Generation (RAG)** backend that lets you:
1. **Ingest** documents (raw text or PDF files)
2. **Query** them with natural language — AI answers using only what's in your knowledge base

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| LLM Integration | LangChain4j 0.36 |
| LLM | Groq — Llama 3.3 70B (Free!) |
| Embeddings | AllMiniLmL6V2 (local, no API key needed) |
| Vector Store | In-Memory Embedding Store |
| Document Parsing | Apache PDFBox |
| API Docs | Swagger UI |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Free Groq API key → https://console.groq.com

---

## Setup

### 1. Get a free Groq API key
Sign up at https://console.groq.com and create an API key.

### 2. Configure application.properties
Create `src/main/resources/application.properties`:

```properties
server.port=8081
groq.api-key=your-groq-api-key-here
groq.model-name=llama-3.3-70b-versatile
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/api-docs
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 3. Run the app
```bash
mvn spring-boot:run
```

### 4. Open Swagger UI
http://localhost:8081/swagger-ui/index.html

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
{ "chunksStored": 4, "message": "4 chunk(s) successfully ingested." }
```

---

### Upload a file (TXT or PDF)
```http
POST /api/documents/upload
Content-Type: multipart/form-data

file=@/path/to/document.pdf
```

**Response:**
```json
{ "chunksStored": 52, "message": "52 chunk(s) successfully ingested." }
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
  "answer": "Based on the provided context...",
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

## Project Structure
src/main/java/com/ragapp/
├── RagApplication.java
├── config/
│   ├── AppProperties.java
│   ├── CorsConfig.java
│   └── LangChainConfig.java
├── controller/
│   ├── DocumentController.java
│   ├── QueryController.java
│   └── GlobalExceptionHandler.java
├── model/
│   └── Dto.java
└── service/
├── DocumentIngestionService.java
└── RagQueryService.java

---

## How It Works
[User uploads PDF]
│
▼
DocumentIngestionService
├── Parse PDF (PDFBox)
├── Split into chunks
├── Embed each chunk (AllMiniLmL6V2 — local, free)
└── Store in InMemoryEmbeddingStore
[User asks question]
│
▼
RagQueryService
├── Embed the question
├── Semantic search — find top-K similar chunks
├── Build prompt: System + Context + Question
└── Call Groq (Llama 3.3 70B) → return answer + sources

---

## Note

Currently uses **in-memory vector store** — documents reset on restart.
Designed to be upgraded to ChromaDB for persistent production storage.

---
