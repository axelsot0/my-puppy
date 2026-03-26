#!/usr/bin/env node

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import { randomUUID } from "node:crypto";
import { createServer, IncomingMessage, ServerResponse } from "node:http";
import { ApiClient } from "./api-client.js";
import { registerAuthTools } from "./tools/auth.js";
import { registerServiceTools } from "./tools/services.js";
import { registerAppointmentTools } from "./tools/appointments.js";
import { registerAdminTools } from "./tools/admin.js";
import { registerPlatformTools } from "./tools/platform.js";

const PORT = process.env.PORT ? parseInt(process.env.PORT) : 3000;
const SESSION_TTL_MS = 30 * 60 * 1000; // 30 minutes
const CLEANUP_INTERVAL_MS = 5 * 60 * 1000; // check every 5 minutes

// Map of active sessions: sessionId -> { transport, lastActivity }
const sessions = new Map<string, { transport: StreamableHTTPServerTransport; lastActivity: number }>();

function createSession(): StreamableHTTPServerTransport {
  const client = new ApiClient();
  const server = new McpServer({ name: "my-puppy", version: "1.0.0" });

  registerAuthTools(server, client);
  registerServiceTools(server, client);
  registerAppointmentTools(server, client);
  registerAdminTools(server, client);
  registerPlatformTools(server, client);

  const transport = new StreamableHTTPServerTransport({
    sessionIdGenerator: () => randomUUID(),
    onsessioninitialized: (sessionId) => {
      sessions.set(sessionId, { transport, lastActivity: Date.now() });
    },
  });

  transport.onclose = () => {
    if (transport.sessionId) {
      sessions.delete(transport.sessionId);
    }
  };

  server.connect(transport).catch((err) => {
    console.error("Failed to connect server to transport:", err);
  });

  return transport;
}

async function handleRequest(req: IncomingMessage, res: ServerResponse): Promise<void> {
  // Health check — Railway and load balancers probe this
  if (req.method === "GET" && (req.url === "/health" || req.url === "/")) {
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ status: "ok", sessions: sessions.size }));
    return;
  }

  const sessionId = req.headers["mcp-session-id"] as string | undefined;

  // Existing session
  if (sessionId) {
    const entry = sessions.get(sessionId);
    if (!entry) {
      res.writeHead(404, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: "Session not found" }));
      return;
    }
    entry.lastActivity = Date.now();
    await entry.transport.handleRequest(req, res);
    return;
  }

  // New session — only POST can initialize (MCP spec)
  if (req.method !== "POST") {
    res.writeHead(405, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ error: "Method not allowed. Use POST to initialize a session." }));
    return;
  }

  const transport = createSession();
  await transport.handleRequest(req, res);
}

const httpServer = createServer((req, res) => {
  handleRequest(req, res).catch((err) => {
    console.error("Request error:", err);
    if (!res.headersSent) {
      res.writeHead(500).end();
    }
  });
});

// Session TTL cleanup — evict stale sessions
const cleanupTimer = setInterval(() => {
  const now = Date.now();
  for (const [id, entry] of sessions) {
    if (now - entry.lastActivity > SESSION_TTL_MS) {
      entry.transport.close().catch(() => {});
      sessions.delete(id);
    }
  }
}, CLEANUP_INTERVAL_MS);
cleanupTimer.unref(); // don't keep process alive just for cleanup

// Graceful shutdown
function shutdown() {
  console.error("Shutting down MCP server...");
  clearInterval(cleanupTimer);
  const closing = Array.from(sessions.values()).map((e) => e.transport.close().catch(() => {}));
  Promise.all(closing).finally(() => {
    httpServer.close(() => process.exit(0));
    // Force exit after 5s if connections don't drain
    setTimeout(() => process.exit(1), 5000).unref();
  });
}

process.on("SIGTERM", shutdown);
process.on("SIGINT", shutdown);

httpServer.listen(PORT, () => {
  console.error(`MCP server listening on port ${PORT}`);
});
