#!/usr/bin/env node

import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { registerAuthTools } from "./tools/auth.js";
import { registerServiceTools } from "./tools/services.js";
import { registerClientAppointmentTools, registerAppointmentTools } from "./tools/appointments.js";
import { registerAdminTools } from "./tools/admin.js";
import { registerPlatformTools } from "./tools/platform.js";

const server = new McpServer({
  name: "my-puppy",
  version: "1.0.0",
});

// Register all tools
registerAuthTools(server);
registerServiceTools(server);
registerClientAppointmentTools(server);
registerAppointmentTools(server);
registerAdminTools(server);
registerPlatformTools(server);

// Start server with stdio transport
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
}

main().catch((error) => {
  console.error("MCP Server failed to start:", error);
  process.exit(1);
});
