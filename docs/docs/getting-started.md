---
sidebar_position: 2
---

# Getting Started

This guide walks you through building and running Glass Box locally.

## Prerequisites

- **Java 25+** — [Download from Oracle](https://www.oracle.com/java/technologies/downloads/)
- **Maven** — Included via the Maven Wrapper (`./mvnw`), no separate install needed
- **Node.js 20+** — [Download from nodejs.org](https://nodejs.org/) (for frontend and docs)
- **Git** — For cloning the repository
- **Docker** (optional) — For containerized production builds

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/z10n-dev/BuildYourOwnWebserver.git
cd BuildYourOwnWebserver
```

### 2. Build the Backend

```bash
cd backend
./mvnw clean package
```

This compiles the Java source code and creates an executable uber-JAR using the Maven Shade plugin.

### 3. Build the Frontend

```bash
cd ../frontend
npm install
npm run build
```

Creates an optimized static export in the `out/` directory.

### 4. Build the Documentation

```bash
cd ../docs
npm install
npm run build
```

Creates the static documentation site in `build/`.

## Running the Server

### Development Mode

There are two options for development:

#### Option 1: Run Backend and Frontend Separately

**Pros:** Frontend reloads automatically on changes.
**Cons:** Frontend runs on a different port and cannot communicate with the backend API.

```bash
# Terminal 1 — Backend
cd backend
./mvnw exec:java -Dexec.mainClass=com.ns.webserver.Main dev

# Terminal 2 — Frontend
cd frontend
npm run dev

# Terminal 3 — Documentation
cd docs
npm start
```

| Service       | URL                     |
|---------------|-------------------------|
| Backend       | `http://localhost:8080`  |
| Frontend      | `http://localhost:3000`  |
| Documentation | `http://localhost:3001`  |

#### Option 2: Build Frontend First, Then Run Backend

**Pros:** Behaves like production — backend serves the frontend assets directly.
**Cons:** No hot-reload; you must rebuild the frontend after every change.

```bash
cd frontend && npm run build
cd ../docs && npm run build
cd ../backend
./mvnw exec:java -Dexec.mainClass=com.ns.webserver.Main dev
```

The server will serve both the frontend and API at `http://localhost:8080`.

### Production Mode (Docker)

```bash
# Build everything
cd frontend && npm run build
cd ../docs && npm run build
cd ../backend && ./mvnw clean package

# Build and run the Docker image
cd ..
docker build -t glassbox .
docker run -p 8080:8080 glassbox
```

The server starts with the `prod` configuration profile by default.

### Configuration Profiles

The server loads a YAML config file based on the environment argument:

```bash
# Loads config/config.dev.yaml
./mvnw exec:java -Dexec.mainClass=com.ns.webserver.Main dev

# Loads config/config.prod.yaml (default when no argument is given)
./mvnw exec:java -Dexec.mainClass=com.ns.webserver.Main prod
```

See [Server Configuration](./configuration/Server%20Configuration.md) for details on configuration options.

## Verify It Works

Once the server is running, test the endpoints:

```bash
# Static page
curl http://localhost:8080/

# Hello World handler
curl http://localhost:8080/hello

# ToDo API — list all
curl http://localhost:8080/api/todos

# ToDo API — create one
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -d '{"title": "Learn HTTP", "completed": false}'

# SSE stream (stays open, Ctrl+C to stop)
curl -N http://localhost:8080/api/sse
```

## CI/CD

You can automate building and deploying with a CI pipeline. Below is an example using GitHub Actions:

<details>
<summary>Example: GitHub Actions Pipeline</summary>

```yml
name: Build and Push Docker Image

on:
  push:
    branches: [ "main" ]

env:
  REGISTRY: ghcr.io

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up Java 25
        uses: actions/setup-java@v4
        with:
          java-version: '25'
          distribution: 'temurin'
          cache: 'maven'

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: 'frontend/package-lock.json'

      - name: Install frontend dependencies
        working-directory: ./frontend
        run: npm ci

      - name: Build Next.js frontend
        working-directory: ./frontend
        run: npm run build

      - name: Copy static files to Java resources
        run: |
          mkdir -p backend/src/main/resources/static/glassbox
          rm -rf backend/src/main/resources/static/glassbox/*
          cp -r frontend/out/* backend/src/main/resources/static/glassbox/

      - name: Convert repository name to lowercase
        run: echo "IMAGE_NAME=${GITHUB_REPOSITORY,,}" >> ${GITHUB_ENV}

      - name: Build Java application
        working-directory: ./backend
        run: ./mvnw clean package -DskipTests

      - name: Log in to registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:latest
```

</details>
