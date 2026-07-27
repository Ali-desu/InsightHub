# InsightHub

Upload your documents, ask questions in plain English, and get answers with citations pointing back to
the exact passage they came from.

I'm building this to sharpen my backend and full-stack skills end to end — real auth, cloud storage, a
proper RAG pipeline, and a frontend that ties it together — rather than another CRUD tutorial. It's a
work in progress; some pieces are done and solid, others are mid-build (see [Status](#status) below).

## What it does

- **Accounts & auth** — register / log in, stateless JWT auth, BCrypt-hashed passwords.
- **Document upload** — files upload *straight to S3* via presigned URLs (the backend never streams the
  bytes), with the metadata tracked in Postgres.
- **RAG (in progress)** — uploaded documents get chunked and embedded into a vector database, so a
  question can be answered from the most relevant passages, with citations back to the source.

## Tech stack

**Backend**
- Java 21, Spring Boot 4
- Spring Security (JWT via JJWT), Spring Data JPA
- PostgreSQL + [pgvector](https://github.com/pgvector/pgvector) for vector search
- AWS S3 (presigned uploads) and AWS Bedrock (Titan embeddings)
- Maven

**Frontend**
- React + TypeScript (Vite)

## Architecture (high level)

Right now it's a single Spring Boot app organised into clear internal layers (a "modular monolith"),
with the boundaries drawn so it can be split into services later.

```
React frontend  ──►  Spring Boot API  ──►  Postgres + pgvector   (users, documents, chunks)
      │                     │
      │                     └──►  AWS Bedrock   (Titan embeddings)
      │
      └── PUT file bytes ──►  AWS S3            (presigned URL — bypasses the backend)
```

Upload flow: the client asks the API for a presigned URL, uploads the file directly to S3, then confirms.
On confirm, the document is queued for ingestion (extract text → chunk → embed → store vectors), which
makes it searchable for question-answering.

## Getting started

**Prerequisites:** JDK 21+, Docker, Node 18+, and an AWS account (an S3 bucket + Bedrock model access,
with credentials in `~/.aws/credentials`).

**1. Start Postgres with pgvector**

```bash
docker run -d --name docqa-pg \
  -e POSTGRES_PASSWORD=password -e POSTGRES_USER=docqa -e POSTGRES_DB=docqa \
  -p 5432:5432 pgvector/pgvector:pg16

docker exec docqa-pg psql -U docqa -d docqa -c "CREATE EXTENSION IF NOT EXISTS vector;"
```

**2. Configure**

In `docqa/src/main/resources/application.properties`, set your S3 bucket, AWS regions, and a JWT secret
(the committed values are dev placeholders). AWS credentials are read from `~/.aws/credentials` — nothing
secret lives in the repo.

**3. Run the backend**

```bash
cd docqa
./mvnw spring-boot:run       # http://localhost:8080
```

**4. Run the frontend**

```bash
cd frontend
npm install
npm run dev                  # http://localhost:5173
```

Open http://localhost:5173, create an account, and upload a document.

## Status

Done:
- [x] JWT auth (register, login, token filter)
- [x] Presigned S3 upload + confirm flow, document metadata in Postgres
- [x] React frontend (auth + upload UI)
- [x] pgvector storage + Bedrock (Titan) embeddings

In progress:
- [ ] Ingestion pipeline (text extraction → chunking → embedding → store)
- [ ] Question-answering endpoint (retrieve → prompt → cited answer)

Planned:
- [ ] Async ingestion via a message queue
- [ ] Schema migrations (Flyway)
- [ ] Deployment (containerised, on AWS)

## Notes

- The `document_chunks` table (with the `vector(1024)` column) is created manually for now — a Flyway
  migration to version-control the schema is on the roadmap.
- This is a personal project and a learning vehicle, so expect it to keep evolving.
