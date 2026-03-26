#!/usr/bin/env node

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { registerTenantAuthTools } from "./tools/auth-tenant.js";
import { registerServiceTools } from "./tools/services.js";
import { registerClientAppointmentTools, registerAppointmentTools } from "./tools/appointments.js";
import { registerAdminTools } from "./tools/admin.js";

const server = new McpServer({
  name: "my-puppy-tenant",
  version: "1.0.0",
});

// Tenant agent: business tools only, no platform tools, no set_tenant_id
// Tenant ID is fixed from TENANT_ID environment variable
registerTenantAuthTools(server);
registerServiceTools(server);
registerClientAppointmentTools(server);
registerAppointmentTools(server);
registerAdminTools(server);

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
}

main().catch((error) => {
  console.error("MCP Tenant Server failed to start:", error);
  process.exit(1);
});
