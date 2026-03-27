import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { ApiClient } from "../api-client.js";

export function registerTenantAuthTools(server: McpServer, client: ApiClient): void {

  // --- User/Admin Login (tenant-scoped) ---
  server.tool(
    "login",
    "Request OTP login for a CLIENT or ADMIN user of this business. Returns a challengeId. The user must check their email for the 6-digit OTP code.",
    {
      email: z.string().email().describe("User email address"),
      password: z.string().describe("User password"),
    },
    async ({ email, password }) => {
      try {
        const result = await client.request<{ challengeId: string; message: string; expiresInSeconds: number }>(
          "/api/auth/login",
          { method: "POST", body: { email, password }, useTenant: true }
        );
        return {
          content: [{
            type: "text" as const,
            text: `OTP challenge created.\n- Challenge ID: ${result.challengeId}\n- ${result.message}\n- Expires in: ${result.expiresInSeconds} seconds\n\nAsk the user for the 6-digit OTP code sent to ${email}, then use verify_otp.`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Login failed: ${(e as Error).message}` }] };
      }
    }
  );

  // --- Verify OTP (tenant-scoped) ---
  server.tool(
    "verify_otp",
    "Verify the 6-digit OTP code to complete login. Returns a JWT token that is stored automatically for subsequent requests.",
    {
      challengeId: z.string().uuid().describe("The challenge ID from the login step"),
      otp: z.string().length(6).describe("The 6-digit OTP code from email"),
    },
    async ({ challengeId, otp }) => {
      try {
        const result = await client.request<{ token: string; user: { id: string; email: string; firstName: string; lastName: string; role: string } }>(
          "/api/auth/verify-otp",
          { method: "POST", body: { challengeId, otp }, useTenant: true }
        );
        client.setAuthToken(result.token);
        return {
          content: [{
            type: "text" as const,
            text: `Login successful! Token stored.\n- User: ${result.user.firstName} ${result.user.lastName}\n- Email: ${result.user.email}\n- Role: ${result.user.role}\n- ID: ${result.user.id}`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `OTP verification failed: ${(e as Error).message}` }] };
      }
    }
  );

  // --- Register User (tenant-scoped) ---
  server.tool(
    "register",
    "Register a new client user in this business. Password policy: min 8 chars, at least one uppercase, one lowercase, one digit, and one special character (e.g. Client@2024!).",
    {
      email: z.string().email().describe("User email"),
      firstName: z.string().describe("First name"),
      lastName: z.string().describe("Last name"),
      password: z.string().describe("Password — must have min 8 chars, uppercase, lowercase, digit, and special character (e.g. Client@2024!)"),
    },
    async ({ email, firstName, lastName, password }) => {
      try {
        const result = await client.request<{ id: string; email: string; firstName: string; lastName: string; role: string }>(
          "/api/auth/register",
          { method: "POST", body: { email, firstName, lastName, password, authProvider: "LOCAL" }, useTenant: true }
        );
        return {
          content: [{
            type: "text" as const,
            text: `User registered successfully.\n- ID: ${result.id}\n- Name: ${result.firstName} ${result.lastName}\n- Email: ${result.email}\n- Role: ${result.role}`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Registration failed: ${(e as Error).message}` }] };
      }
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
          text: `MCP Server Config (Tenant Mode):\n- API Base URL: ${client.getBaseUrl()}\n- Tenant ID: ${tid || "(not set)"}\n- Authenticated: ${token ? "yes (token present)" : "no"}`,
        }],
      };
    }
  );
}
