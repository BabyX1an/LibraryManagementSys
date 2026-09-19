# System Architecture

This document describes the runtime architecture and the two main application
flows of the Intelligent Library Management System.

## Runtime Architecture

```mermaid
flowchart TB
    Browser[Web Browser]

    subgraph Frontend[Frontend Layer]
        Vue[Vue 3 + TypeScript<br/>Vite / Pinia / Element Plus<br/>Port 3001]
    end

    subgraph Platform[Service Infrastructure]
        Gateway[Spring Cloud Gateway<br/>Port 8081]
        Nacos[Nacos<br/>Discovery and Configuration<br/>Port 8848]
    end

    subgraph Services[Application Services]
        Book[Book Service<br/>Books / Loans / Statistics<br/>Port 8105]
        User[User Service<br/>Accounts / Login / Profiles<br/>Port 8106]
        AI[AI Service<br/>Search / Recommendations<br/>Port 8108]
    end

    subgraph Data[Data Layer]
        MySQL[(MySQL 8<br/>book / user / borrow_record)]
        Redis[(Redis<br/>Book Embedding Cache)]
        Images[(Local Image Directory<br/>Book Covers)]
        Model[(Sentence Transformer<br/>Local Model Cache)]
    end

    Browser --> Vue
    Vue -->|HTTP /api/**| Gateway

    Gateway -->|/api/v1/books/**<br/>/api/v1/borrow/**<br/>/api/v1/stats/**| Book
    Gateway -->|/api/v1/users/**| User
    Gateway -->|/api/v1/ai/**| AI

    Gateway -. discovers .-> Nacos
    Book -. registers with .-> Nacos
    User -. registers with .-> Nacos
    AI -. registers with .-> Nacos

    Book --> MySQL
    Book --> Images
    User --> MySQL
    AI --> MySQL
    AI --> Redis
    AI --> Model
```

## Borrowing Transaction

```mermaid
sequenceDiagram
    actor Reader
    participant UI as Vue Frontend
    participant GW as API Gateway
    participant BS as Book Service
    participant DB as MySQL

    Reader->>UI: Select Borrow
    UI->>GW: POST /api/v1/borrow/{bookId}
    GW->>BS: Route request with Authorization header
    BS->>BS: Decode user ID from token
    BS->>DB: SELECT book ... FOR UPDATE
    DB-->>BS: Locked book row
    BS->>DB: Check active loan for the same user and book

    alt Inventory available and no duplicate loan
        BS->>DB: Insert borrow_record with a 14-day due date
        BS->>DB: Decrement stock and increment borrowed_count
        BS->>DB: Commit transaction
        BS-->>GW: Borrowing record
        GW-->>UI: Borrowing record
        UI-->>Reader: Borrowing succeeded
    else Validation fails
        BS->>DB: Roll back transaction
        BS-->>GW: Business error
        GW-->>UI: Business error
        UI-->>Reader: Display error message
    end
```

The row lock and transaction keep the inventory check and stock update atomic,
preventing concurrent requests from borrowing the final copy more than once.

## Semantic Search and Recommendation Flow

```mermaid
flowchart LR
    Catalog[(Book Catalog)] --> Text[Title + Author + Category + Description]
    Text --> Encoder[Multilingual Sentence Transformer]
    Encoder --> Vectors[Normalized Book Embeddings]
    Vectors --> Cache[(Redis Vector Cache)]

    Query[Natural-language Query] --> QueryVector[Query Embedding]
    QueryVector --> Search[Cosine Similarity]
    Cache --> Search
    Search --> SearchTopK[Top-K Semantic Results]

    History[(Borrowing History)] --> Weight[Recency-weighted Average]
    Cache --> Weight
    Weight --> Interest[User Interest Vector]
    Interest --> Recommend[Cosine Similarity]
    Cache --> Recommend
    Recommend --> Exclude[Exclude Previously Borrowed Books]
    Exclude --> RecommendTopK[Top-K Recommendations]

    NoHistory[Anonymous or No History] --> Popular[Popular-books Fallback]
```

Book embeddings are created during AI-service startup when no Redis cache is
available. Catalog changes do not currently invalidate the cache automatically;
call `POST /api/v1/ai/embeddings/rebuild` after significant book updates.

## Service Boundaries

| Component | Owns | Depends on |
| --- | --- | --- |
| Gateway | External API routing | Nacos and all application services |
| Book Service | Catalog, inventory, loans, dashboard data | MySQL and local cover storage |
| User Service | Accounts, login, profiles, user administration | MySQL |
| AI Service | Embeddings, semantic ranking, recommendations | MySQL, Redis, model cache, Nacos |
| Frontend | Reader and administrator workflows | Gateway |

## Security Boundary

The current authentication implementation is intended for an academic demo.
The token contains a Base64-encoded user ID and is not cryptographically signed.
Before production deployment, replace it with signed authentication, enforce
administrator authorization in the backend, use a modern password hash, and
move all credentials out of tracked configuration files.
