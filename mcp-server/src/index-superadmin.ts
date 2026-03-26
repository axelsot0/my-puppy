#!/usr/bin/env node

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { registerSuperAdminAuthTools } from "./tools/auth-superadmin.js";
import { registerPlatformTools } from "./tools/platform.js";
import { registerServiceTools } from "./tools/services.js";
import { registerAppointmentTools } from "./tools/appointments.js";
import { registerAdminTools } from "./tools/admin.js";

const server = new McpServer({
  name: "my-puppy-superadmin",
  version: "1.0.0",
});

// SuperAdmin agent: all tools (platform + business management)
// Use set_tenant_id to select which business to manage
registerSuperAdminAuthTools(server);
registerPlatformTools(server);
registerServiceTools(server);
registerAppointmentTools(server);
registerAdminTools(server);

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
}

main().catch((error) => {
  console.error("MCP SuperAdmin Server failed to start:", error);
  process.exit(1);
});
