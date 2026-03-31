import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { ApiClient } from "../api-client.js";

export function registerSuperAdminAuthTools(server: McpServer, client: ApiClient): void {

  // --- SuperAdmin Login ---
  server.tool(
    "platform_login",
    "Request OTP login for a SUPER_ADMIN. No tenant required. Returns a challengeId. Grants access to all platform and business management tools.",
    {
      email: z.string().email().describe("SuperAdmin email"),
      password: z.string().describe("SuperAdmin password"),
    },
    async ({ email, password }) => {
      try {
        const result = await client.request<{ challengeId: string; message: string; expiresInSeconds: number }>(
          "/platform/auth/login",
          { method: "POST", body: { email, password } }
        );
        return {
          content: [{
            type: "text" as const,
            text: `SuperAdmin OTP challenge created.\n- challengeId: ${result.challengeId}\n- Expires in: ${result.expiresInSeconds} seconds\n\nIMPORTANT: Stop here. Tell the user to check their email (${email}) for the 6-digit OTP code and send it. Do NOT call platform_verify_otp until the user provides the code in their next message.`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `SuperAdmin login failed: ${(e as Error).message}` }] };
      }
    }
  );

  // --- SuperAdmin Verify OTP ---
  server.tool(
    "platform_verify_otp",
    "Verify the 6-digit OTP code to complete SuperAdmin login. Stores the JWT token for all operations.",
    {
      challengeId: z.string().uuid().describe("The challenge ID from platform_login"),
      otp: z.string().length(6).describe("The 6-digit OTP code from email"),
    },
    async ({ challengeId, otp }) => {
      try {
        const result = await client.request<{ token: string }>(
          "/platform/auth/verify-otp",
          { method: "POST", body: { challengeId, otp } }
        );
        client.setAuthToken(result.token);
        return {
          content: [{
            type: "text" as const,
            text: `SuperAdmin login successful! Token stored for this session.\nYou can now call list_businesses to see all businesses, or set_tenant_id to manage a specific one.`,
          }],
        };
      } catch (e: unknown) {
        const msg = (e as Error).message;
        const hint = msg.includes("401")
          ? "The OTP code was incorrect or has expired. Please call platform_login again to get a new code."
          : msg;
        return { content: [{ type: "text" as const, text: `SuperAdmin OTP verification failed: ${hint}` }] };
      }
    }
  );

  // --- Set Tenant ID at runtime ---
  server.tool(
    "set_tenant_id",
    "Set the active business/tenant context. Required before managing business internals (employees, services, appointments, availability). Use list_businesses to find business UUIDs.",
    {
      tenantId: z.string().uuid().describe("The business UUID to use as tenant ID"),
    },
    async ({ tenantId }) => {
      client.setTenantId(tenantId);
      return {
        content: [{
          type: "text" as const,
          text: `Tenant ID set to: ${tenantId}\nYou can now manage this business's employees, services, appointments, and availability.`,
        }],
      };
    }
  );

  // --- Show current config ---
  server.tool(
    "show_config",
    "Show the current MCP server configuration (API URL, tenant ID, auth status).",
    {},
    async () => {
      const tid = client.getTenantId();
      const token = client.getAuthToken();
      return {
        content: [{
          type: "text" as const,
          text: `MCP Server Config (SuperAdmin Mode):\n- API Base URL: ${client.getBaseUrl()}\n- Tenant ID: ${tid || "(not set)"}\n- Authenticated: ${token ? "yes (token present)" : "no"}`,
        }],
      };
    }
  );
}
