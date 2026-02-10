---
sidebar_position: 1
slug: /intro
---

# Introduction

**Glass Box** is an educational HTTP/1.1 web server built from scratch in Java 25. It is designed to make the inner workings of a web server transparent and observable — no frameworks, no magic, just raw TCP sockets and a clean architecture you can follow from connection to response.

## Why Glass Box?

Most web frameworks abstract away the details of HTTP. Glass Box takes the opposite approach: every request flows through a visible pipeline you can inspect, extend, and learn from. A built-in Server-Sent Events (SSE) endpoint streams logs and server metrics to a live dashboard in real time.

## What You Can Learn

- How HTTP/1.1 requests are parsed from raw byte streams
- How routing maps URL patterns to handler logic
- How virtual hosting serves multiple domains from one server
- How Server-Sent Events maintain persistent connections
- How Java virtual threads enable lightweight concurrency

## Key Features

| Feature | Description |
|---------|-------------|
| **Raw HTTP/1.1** | Custom request parser and response builder on top of TCP sockets |
| **Virtual Threads** | Java 25 Project Loom threads for high-concurrency request handling |
| **Virtual Hosts** | Serve multiple domains with independent routes and document roots |
| **SSE Streaming** | Real-time server metrics and log streaming to connected clients |
| **REST API** | Built-in ToDo API demonstrating CRUD operations and JSON handling |
| **Static File Serving** | Automatic MIME type detection and directory index resolution |
| **YAML Configuration** | Declarative config for ports, hosts, routes, and log levels |
| **Reflection-based Handlers** | Register handlers by class name — discovered automatically at startup |

## Project Structure

```
BuildYourOwnWebserver/
├── backend/                    # Java web server
│   ├── src/main/java/
│   │   ├── com/ns/tcpframework/    # Core HTTP framework
│   │   └── com/ns/webserver/       # Application handlers & models
│   └── src/main/resources/
│       └── config/                 # YAML configuration files
├── frontend/                   # Next.js dashboard (Glass Box UI)
├── docs/                       # This documentation (Docusaurus)
└── Dockerfile
```

## Next Steps

- **[Getting Started](./getting-started.md)** — Install, build, and run the server
- **[Configuration](./configuration/)** — Configure ports, hosts, routes, and handlers
- **[Architecture](./architecture/)** — Deep dive into the system design
- **[API Reference](./api/)** — Endpoint documentation for the REST and SSE APIs
