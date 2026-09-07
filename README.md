# gemini-mcp-server — MCP Tool Server

Standalone Spring Boot app that exposes database query tools over the **Model Context Protocol (MCP)**. LLM-agnostic — any MCP host (gemini-chat, Claude Desktop, Cursor, etc.) can connect and use the tools without changing a line of server code.

Part of the [gemini-chat](https://github.com/shamprakash2000/gemini-chat) learning project (Phase 5).

---

## What It Does

Exposes 3 database tools over MCP (SSE transport):

| Tool | Description |
|---|---|
| `listTables` | Returns the list of tables the agent is allowed to query |
| `getTableSchema` | Returns column names, types, and 3 sample rows for a table |
| `executeQuery` | Executes a SELECT query and returns results as a markdown table |

Safety enforced server-side:
- Only `SELECT` allowed — `INSERT`, `UPDATE`, `DELETE`, `DROP` all rejected
- Only `da_products` and `da_orders` accessible — system tables blocked
- Results capped at 20 rows automatically

---

## Architecture

```
MCP Host (gemini-chat, port 8080)
    │
    │  MCP protocol — JSON-RPC 2.0 over HTTP/SSE
    ▼
gemini-mcp-server (port 8081)
    │
    ▼
Neon PostgreSQL (da_products, da_orders)
```

The MCP host connects at startup via `GET /sse`, receives a session endpoint, then sends tool calls as `POST` JSON-RPC messages. Tool results flow back over the SSE stream.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 17, Spring Boot 3.4.1 |
| MCP | Spring AI 1.1.8 (`spring-ai-starter-mcp-server-webmvc`) |
| Database | PostgreSQL on Neon (via `JdbcTemplate`) |
| Deployment | Render (Docker) |

---

## Local Setup

### Prerequisites
- Java 17+
- Maven 3.9+
- Neon PostgreSQL credentials (same DB as gemini-chat — tables are seeded by gemini-chat on startup)

### Environment Variables

```bash
NEON_HOST=your_neon_host
NEON_DB=your_db_name
NEON_USER=your_db_user
NEON_PASSWORD=your_db_password
```

### Run

```bash
mvn spring-boot:run
```

Server starts at `http://localhost:8081`

**Start this before gemini-chat** — gemini-chat connects to the MCP server at startup.

---

## MCP Endpoints

| Endpoint | Description |
|---|---|
| `GET /sse` | SSE stream — MCP clients connect here. Server sends session endpoint URL as first event. |
| `POST /mcp/message?sessionId=xxx` | Send JSON-RPC tool call messages (session ID comes from the SSE stream) |

### Testing with MCP Inspector

```bash
npx @modelcontextprotocol/inspector
```

Point it at `http://localhost:8081/sse` to browse tools and call them interactively.

---

## Render Deployment

### Environment Variables (set in Render dashboard)

```
NEON_HOST=...
NEON_DB=...
NEON_USER=...
NEON_PASSWORD=...
```

`PORT` is set automatically by Render — the app reads it via `${PORT:8081}`.

### After Deploy

Copy the Render service URL (e.g. `https://gemini-mcp-server.onrender.com`) and set it as `MCP_SERVER_URL` in gemini-chat's Render environment variables.
