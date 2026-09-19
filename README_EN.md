# Intelligent Library Management System

[Chinese README](README.md)

An academic full-stack library management system built with a Spring Cloud
microservice architecture. The application supports book and user
administration, borrowing and return workflows, dashboard analytics,
natural-language semantic search, and personalized book recommendations.

## Highlights

- Manage books, inventory, users, and user status through an administrator UI.
- Browse, filter, borrow, and return books through a reader-facing interface.
- Track current loans, borrowing history, due dates, and inventory changes.
- Route all backend traffic through Spring Cloud Gateway and discover services
  through Nacos.
- Search for books by meaning rather than exact keywords using multilingual
  Sentence Transformer embeddings.
- Recommend books from a reader's borrowing history, with a popular-books
  fallback for new or anonymous users.
- Cache book embeddings in Redis to avoid recomputing them for every request.
- Use database transactions and row-level locking to prevent concurrent
  borrowing requests from overselling available inventory.

## Architecture

```text
Vue 3 frontend (3001)
        |
        v
Spring Cloud Gateway (8081)
        |
        +-- book-service (8105) ---- Books, loans, returns, statistics
        |
        +-- user-service (8106) ---- Users, registration, login, profiles
        |
        +-- ai-service (8108) ------ Semantic search and recommendations
                         |
                         +---------- MySQL + Redis

Nacos (8848) provides service discovery and configuration support.
```

### Services

| Component | Port | Responsibility |
| --- | ---: | --- |
| Frontend | 3001 | Vue-based reader and administrator interface |
| API Gateway | 8081 | Centralized routing and service load balancing |
| Book Service | 8105 | Book CRUD, borrowing, returns, and statistics |
| User Service | 8106 | User CRUD, registration, login, and profiles |
| AI Service | 8108 | Semantic search and personalized recommendations |
| Nacos | 8848 | Service discovery and configuration management |
| MySQL | 3306 | Persistent application data |
| Redis | 6379 | Cached semantic-vector data |

## Technology Stack

### Backend

- Java 21
- Spring Boot 3.2
- Spring Cloud 2023
- Spring Cloud Gateway
- Spring Cloud Alibaba Nacos
- MyBatis-Plus
- MySQL 8
- Redis
- Knife4j / OpenAPI

### AI service

- Python and FastAPI
- Sentence Transformers
- `paraphrase-multilingual-MiniLM-L12-v2`
- NumPy and SQLAlchemy
- Redis vector cache

### Frontend

- Vue 3 and TypeScript
- Vite
- Pinia
- Vue Router
- Element Plus
- Apache ECharts
- Axios

## Recommendation and Search Design

Each book is represented by an embedding generated from its title, author,
category, and description. The vectors are normalized and cached in Redis.

Semantic search encodes a natural-language query and ranks books by cosine
similarity. Personalized recommendations calculate a weighted average of the
user's previously borrowed books, giving more recent loans greater weight. The
service excludes books the user has already borrowed and returns the closest
remaining matches. Users without borrowing history receive popular books
instead.

## Borrowing Workflow

When a reader borrows a book, the Book Service:

1. Locks the corresponding database row with `SELECT ... FOR UPDATE`.
2. Verifies that inventory is available.
3. Prevents the reader from borrowing the same book twice before returning it.
4. Creates a loan with a 14-day due date.
5. Decrements inventory and increments the historical borrowing count in the
   same transaction.

Returning a book verifies ownership of the loan record, marks the loan as
returned, records the return time, and restores inventory.

## Project Structure

```text
LibraryManagementSys/
|-- library-gateway/       # Spring Cloud API Gateway
|-- book-service/          # Book, borrowing, and statistics service
|-- user-service/          # User and account service
|-- ai-service/            # FastAPI recommendation and search service
|-- frontend/              # Vue 3 web application
|-- database/
|   |-- db_init.sql        # Database, book table, user table, seed data
|   `-- borrow_record.sql  # Borrowing-record table
|-- docs/                  # Design and handover documentation
|-- start-all.sh           # Starts all application services
`-- stop-all.sh            # Stops all application services
```

## Documentation

- [Architecture diagrams](docs/ARCHITECTURE.md)
- [Detailed startup guide (Chinese)](docs/STARTUP_GUIDE.md)
- [Environment variable template](.env.example)

## Prerequisites

Install and start the following dependencies before launching the application:

- JDK 21
- Node.js and npm
- Python 3
- MySQL 8
- Redis 6 or later
- Nacos 2.x in standalone mode

The Maven Wrapper is included, so a separate Maven installation is optional.

## Configuration

The Java services read their local configuration from:

- `book-service/src/main/resources/application.yml`
- `user-service/src/main/resources/application.yml`
- `library-gateway/src/main/resources/application.yml`

The AI service reads defaults from `ai-service/app/config.py` and supports
overrides through environment variables or an `ai-service/.env` file.

Before running the project, update the following values for your environment:

- MySQL host, username, and password
- Redis host and port
- Nacos server address, namespace, and credentials
- Book-cover image directory in the Book Service configuration

Do not commit real production credentials to the repository.

## Database Setup

Both SQL scripts are required:

```bash
mysql -u root -p < database/db_init.sql
mysql -u root -p library_system < database/borrow_record.sql
```

The scripts create the following main tables:

- `book`
- `user`
- `borrow_record`

`db_init.sql` also inserts sample books and users for local development.

## Running the Application

### Option 1: Start all application services

Make sure MySQL, Redis, and Nacos are already running, then execute:

```bash
chmod +x start-all.sh stop-all.sh
./start-all.sh
```

Open the frontend at [http://localhost:3001](http://localhost:3001).

To stop the application services:

```bash
./stop-all.sh
```

Logs are written to the `logs/` directory.

### Option 2: Start services manually

Open separate terminals and run:

```bash
# Gateway
cd library-gateway
../mvnw spring-boot:run

# Book Service
cd book-service
../mvnw spring-boot:run

# User Service
cd user-service
../mvnw spring-boot:run

# AI Service
cd ai-service
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python -m app.main

# Frontend
cd frontend
npm install
npm run dev
```

The AI service currently loads its Sentence Transformer model from the local
Hugging Face cache in offline mode. Ensure that
`paraphrase-multilingual-MiniLM-L12-v2` is already cached before starting it.

## Main API Routes

All frontend API requests are sent through the gateway at
`http://localhost:8081`.

| Method | Route | Description |
| --- | --- | --- |
| `GET` | `/api/v1/books` | List and filter books |
| `GET` | `/api/v1/books/{id}` | Get book details |
| `POST` | `/api/v1/books` | Create a book |
| `PUT` | `/api/v1/books/{id}` | Update a book |
| `DELETE` | `/api/v1/books/{id}` | Delete a book |
| `GET` | `/api/v1/books/popular` | List popular books |
| `POST` | `/api/v1/borrow/{bookId}` | Borrow a book |
| `PUT` | `/api/v1/borrow/{recordId}/return` | Return a book |
| `GET` | `/api/v1/borrow/current` | List current loans |
| `GET` | `/api/v1/borrow/history` | List borrowing history |
| `GET` | `/api/v1/stats/dashboard` | Get dashboard statistics |
| `POST` | `/api/v1/users/login` | Log in |
| `GET` | `/api/v1/users/me` | Get the current user profile |
| `PUT` | `/api/v1/users/me` | Update the current user profile |
| `GET` | `/api/v1/ai/search?q=...` | Perform semantic search |
| `GET` | `/api/v1/ai/recommend` | Get book recommendations |
| `POST` | `/api/v1/ai/embeddings/rebuild` | Rebuild the vector cache |

Knife4j API documentation is available from the Java service instances after
they start:

- Book Service: [http://localhost:8105/doc.html](http://localhost:8105/doc.html)
- User Service: [http://localhost:8106/doc.html](http://localhost:8106/doc.html)

## Development Commands

Build the Java modules:

```bash
./mvnw clean package
```

Run the frontend development server:

```bash
cd frontend
npm run dev
```

Build the frontend for production:

```bash
cd frontend
npm run build
```

## Current Limitations

This repository is an academic project and requires additional hardening before
production use:

- The current login token is a Base64-encoded user ID, not a signed JWT.
- Passwords are hashed with MD5 rather than a modern adaptive password hash.
- Administrative authorization is primarily enforced by frontend routing and
  should also be enforced by the backend.
- Book embeddings are not refreshed automatically after every book update;
  call the rebuild endpoint when the catalog changes.
- The overdue status is defined in the data model but is not updated by a
  scheduled task.
- The frontend development server runs, but the current TypeScript definitions
  need cleanup before the production build succeeds.

## License

No license file is currently included. Add a license before redistributing the
project publicly.
