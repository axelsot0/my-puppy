#!/usr/bin/env node

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import { randomUUID } from "node:crypto";
import { createServer, IncomingMessage, ServerResponse } from "node:http";
import { ApiClient } from "./api-client.js";
import { registerTenantAuthTools } from "./tools/auth-tenant.js";
import { registerServiceTools } from "./tools/services.js";
import { registerClientAppointmentTools, registerAppointmentTools } from "./tools/appointments.js";
import { registerAdminTools } from "./tools/admin.js";

const PORT = process.env.PORT ? parseInt(process.env.PORT) : 3000;
const TENANT_ID = process.env.TENANT_ID || "";
const SESSION_TTL_MS = 30 * 60 * 1000;
const CLEANUP_INTERVAL_MS = 5 * 60 * 1000;

if (!TENANT_ID) {
  console.error("ERROR: TENANT_ID environment variable is required.");
  process.exit(1);
}

const sessions = new Map<string, { transport: StreamableHTTPServerTransport; lastActivity: number }>();

function createSession(): StreamableHTTPServerTransport {
  const client = new ApiClient();
  client.setTenantId(TENANT_ID);

  const server = new McpServer({ name: "my-puppy-tenant", version: "1.0.0" });

  registerTenantAuthTools(server, client);
  registerServiceTools(server, client);
  registerClientAppointmentTools(server, client);
  registerAppointmentTools(server, client);
  registerAdminTools(server, client);

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
  if (req.method === "GET" && (req.url === "/health" || req.url === "/")) {
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ status: "ok", tenant: TENANT_ID, sessions: sessions.size }));
    return;
  }

  const sessionId = req.headers["mcp-session-id"] as string | undefined;

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

const cleanupTimer = setInterval(() => {
  const now = Date.now();
  for (const [id, entry] of sessions) {
    if (now - entry.lastActivity > SESSION_TTL_MS) {
      entry.transport.close().catch(() => {});
      sessions.delete(id);
    }
  }
}, CLEANUP_INTERVAL_MS);
cleanupTimer.unref();

function shutdown() {
  console.error("Shutting down MCP tenant server...");
  clearInterval(cleanupTimer);
  const closing = Array.from(sessions.values()).map((e) => e.transport.close().catch(() => {}));
  Promise.all(closing).finally(() => {
    httpServer.close(() => process.exit(0));
    setTimeout(() => process.exit(1), 5000).unref();
  });
}

process.on("SIGTERM", shutdown);
process.on("SIGINT", shutdown);

httpServer.listen(PORT, () => {
  console.error(`MCP tenant server listening on port ${PORT} (tenant: ${TENANT_ID})`);
});
