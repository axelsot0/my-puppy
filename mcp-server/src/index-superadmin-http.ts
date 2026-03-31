#!/usr/bin/env node

/**
 * SuperAdmin MCP Server — HTTP/Streamable transport
 * Deploy this on Railway for platform-level management.
 * No TENANT_ID required to start. Use set_tenant_id after platform_login.
 */

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import { createServer, IncomingMessage, ServerResponse } from "node:http";
import { randomUUID } from "node:crypto";

import { ApiClient } from "./api-client.js";
import { registerSuperAdminAuthTools } from "./tools/auth-superadmin.js";
import { registerPlatformTools } from "./tools/platform.js";
import { registerServiceTools } from "./tools/services.js";
import { registerClientAppointmentTools, registerAppointmentTools } from "./tools/appointments.js";
import { registerAdminTools } from "./tools/admin.js";

const PORT = parseInt(process.env.PORT || "3000");

// Session store: one transport per session ID
const sessions = new Map<string, StreamableHTTPServerTransport>();

function buildMcpServer(): McpServer {
  // Each session gets its own ApiClient so auth tokens and tenant IDs are isolated
  const client = new ApiClient();
  const server = new McpServer({
    name: "my-puppy-superadmin",
    version: "1.0.0",
  });
  registerSuperAdminAuthTools(server, client); // platform_login, platform_verify_otp, set_tenant_id, show_config
  registerPlatformTools(server, client);       // list_businesses, create_business, create_business_admin, etc.
  registerServiceTools(server, client);        // list_services, get_service, create_service, update_service
  registerClientAppointmentTools(server, client); // book_appointment, my_appointments, cancel_appointment
  registerAppointmentTools(server, client);    // assign_appointment, reject_appointment, mark_appointment_done
  registerAdminTools(server, client);          // list_employees, create_employee, create_availability, etc.
  return server;
}

async function readBody(req: IncomingMessage): Promise<Buffer> {
  return new Promise((resolve, reject) => {
    const chunks: Buffer[] = [];
    req.on("data", (chunk: Buffer) => chunks.push(chunk));
    req.on("end", () => resolve(Buffer.concat(chunks)));
    req.on("error", reject);
  });
}

const httpServer = createServer(async (req: IncomingMessage, res: ServerResponse) => {
  // CORS — required for n8n and browser-based clients
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type, mcp-session-id");

  if (req.method === "OPTIONS") {
    res.writeHead(204).end();
    return;
  }

  try {
    const sessionId = req.headers["mcp-session-id"] as string | undefined;

    // Reuse existing session transport if available
    if (sessionId && sessions.has(sessionId)) {
      const existingTransport = sessions.get(sessionId)!;
      const body = req.method === "POST" ? await readBody(req) : undefined;
      await existingTransport.handleRequest(req, res, body ? JSON.parse(body.toString()) : undefined);
      return;
    }

    // New session: create isolated ApiClient + McpServer + transport
    let transport: StreamableHTTPServerTransport;
    transport = new StreamableHTTPServerTransport({
      sessionIdGenerator: () => randomUUID(),
      onsessioninitialized: (id) => {
        sessions.set(id, transport);
      },
    });

    // Clean up session on close
    transport.onclose = () => {
      const id = req.headers["mcp-session-id"] as string | undefined;
      if (id) sessions.delete(id);
    };

    const mcpServer = buildMcpServer();
    await mcpServer.connect(transport);

    const body = req.method === "POST" ? await readBody(req) : undefined;
    await transport.handleRequest(req, res, body ? JSON.parse(body.toString()) : undefined);

  } catch (err) {
    console.error("Request error:", err);
    if (!res.headersSent) {
      res.writeHead(500, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: "Internal server error" }));
    }
  }
});

httpServer.listen(PORT, () => {
  console.log(`✅ MyPuppy SuperAdmin MCP Server — port ${PORT}`);
  console.log(`   API: ${process.env.API_BASE_URL || "http://localhost:8080"}`);
});
