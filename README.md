# 🌍 Internet Engineering Projects

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)

Welcome to my **Internet Engineering** repository! It contains the six
progressive computer assignments ("Phases") for the Internet Engineering
course at the **University of Tehran (UT)**: a single article-sharing web
application, rebuilt and extended phase by phase from a raw TCP socket
server all the way to an authenticated, Dockerized, database-backed system.

Each phase folder is a self-contained copy of the project as it stood at
that milestone, so the whole history of design decisions — and the reasons
behind them — stays inspectable side by side instead of being buried in a
single evolving codebase.

---

## 🛠️ Tech Stack & Tools

| Area | Tools |
|---|---|
| **Backend** | Java 21, hand-rolled HTTP server (`java.net.ServerSocket`), Spring Data JPA (from Phase 4) |
| **Frontend** | React 18 + Vite (from Phase 3) |
| **Database** | PostgreSQL 16 (from Phase 4) |
| **Auth** | Hand-rolled JWT (HS256), PBKDF2WithHmacSHA256 password hashing (Phase 5) |
| **Containerization** | Multi-stage Docker builds, Nginx reverse proxy, Docker Compose (Phase 6) |
| **Build tools** | Maven, npm |

A deliberate constraint runs through every phase: the **HTTP layer itself is
hand-written**, parsing request lines and headers off a raw `Socket` instead
of using Spring MVC — the point of the course is to build the protocol
mechanics, not to configure a framework. Spring only enters later, and only
for what it's actually good at: JPA/Hibernate persistence.

---

## 📂 Repository Layout

```
.
├── Phase1/   Static + dynamic content server from raw sockets
├── Phase2/   RESTful JSON API on top of Phase 1
├── Phase3/   React single-page frontend on top of the Phase 2 API
├── Phase4/   PostgreSQL persistence via Spring Data JPA
├── Phase5/   JWT authentication & per-user authorization
└── Phase6/   Dockerized, multi-container deployment
```

Each `PhaseN/Web-Server` folder is a runnable snapshot of the project; most
phases also ship the assignment's own description PDF and, from Phase 5
onward, a detailed Persian `README.md` explaining exactly what changed and
why.

---

## 📦 Phases Overview

### [Phase 1 — Web Server from Scratch](./Phase1)
A minimal HTTP server built directly on `ServerSocket`/`Socket`: it parses
the request line and headers by hand, routes by path (`Router`), and serves
both static assets (`StaticContentServer`) and server-rendered HTML pages
(`DynamicContentServer` + a small `TemplateEngine`) — a home page, an
article page, and an add-article form, backed by an in-memory `Article`
model.

### [Phase 2 — RESTful API](./Phase2)
The same hand-rolled server is extended with a proper JSON REST API
(`ArticleController`/`ArticleService`, manual JSON (de)serialization in
`JsonUtils`, structured `HttpResponse` helpers) so the article data becomes
consumable by any client, not just the server's own templates.

### [Phase 3 — React Frontend](./Phase3)
A React (Vite) single-page app is built against the Phase 2 REST API,
replacing server-rendered templates with client-side routing and rendering
while the Java backend keeps serving the API.

### [Phase 4 — Persistence with Relational Databases](./Phase4)
In-memory storage is replaced with PostgreSQL via Spring Data JPA:
`@Entity`/`@Repository` for `Article`, and a `docker-compose.yml` to spin up
the database. Spring is scoped strictly to persistence — the HTTP server
underneath is still the same hand-rolled socket implementation.

### [Phase 5 — Authentication & Authorization](./Phase5)
Adds accounts on top of Phase 4: a `User` entity with a real `@ManyToOne`
relation to `Article` (a genuine foreign key, not a duplicated username
column), salted **PBKDF2WithHmacSHA256** password hashing, and a **hand-rolled
HS256 JWT** (no JWT library) for stateless auth. `POST /api/articles` and all
`/api/users/me*` routes require a valid bearer token; reads stay public.
Passwords and salts are kept out of API responses via a dedicated `UserDto`.
See [`Phase5/README.md`](./Phase5/README.md) for the full endpoint map and
the reasoning behind each cryptographic choice.

### [Phase 6 — Docker](./Phase6)
Packages the Phase 5 application as three containers behind Docker Compose:
a multi-stage Java build (Maven → JRE-alpine) for the backend, a multi-stage
React build (Node → Nginx) for the frontend, and official `postgres:16-alpine`
for the database. Nginx doubles as a reverse proxy (`/api/*` → backend,
everything else → `index.html` for client-side routing), so the browser only
ever talks to a single origin. Secrets (`POSTGRES_PASSWORD`, `JWT_SECRET`)
are supplied via a git-ignored `.env`, never hardcoded. See
[`Phase6/README.md`](./Phase6/README.md) for the full container/network
breakdown.

---

## 🚀 Getting Started

Each phase runs independently from its own `Web-Server` directory.

**Phases 1–3** (no database):
```bash
cd PhaseN/Web-Server
mvn spring-boot:run   # or: mvn package && java -jar target/*.jar
```

**Phases 4–5** (Postgres required):
```bash
cd PhaseN/Web-Server
docker compose up -d          # starts Postgres only
mvn spring-boot:run
```

**Phase 3 onward** also has a frontend:
```bash
cd PhaseN/Web-Server/src/frontend
npm install
npm run dev
```

**Phase 6** (fully containerized):
```bash
cd Phase6/Web-Server
cp .env.example .env          # fill in POSTGRES_PASSWORD and JWT_SECRET
docker compose up -d --build
# frontend: http://localhost:80
# backend (via the Nginx proxy): http://localhost/api/...
```

---

## 📄 License

This repository is shared for educational purposes. See individual phase
folders for any phase-specific licensing notes.
