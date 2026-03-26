import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { apiRequest, setAuthToken, getTenantId, setTenantId, getAuthToken, getBaseUrl } from "../api-client.js";

export function registerAuthTools(server: McpServer): void {

  // --- User/Admin Login (tenant-scoped) ---
  server.tool(
    "login",
    "Request OTP login for a CLIENT or ADMIN user of a specific tenant/business. IMPORTANT: call set_tenant_id first — this endpoint is tenant-scoped. Returns a challengeId. The user must check their email for the 6-digit OTP code. Use platform_login instead if authenticating as SUPER_ADMIN.",
    {
      email: z.string().email().describe("User email address"),
      password: z.string().describe("User password"),
    },
    async ({ email, password }) => {
      try {
        const result = await apiRequest<{ challengeId: string; message: string; expiresInSeconds: number }>(
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
        const result = await apiRequest<{ token: string; user: { id: string; email: string; firstName: string; lastName: string; role: string } }>(
          "/api/auth/verify-otp",
          { method: "POST", body: { challengeId, otp }, useTenant: true }
        );
        setAuthToken(result.token);
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
    "Register a new client user in the current tenant/business. Password policy: min 8 chars, at least one uppercase, one lowercase, one digit, and one special character (e.g. Client@2024!).",
    {
      email: z.string().email().describe("User email"),
      firstName: z.string().describe("First name"),
      lastName: z.string().describe("Last name"),
      password: z.string().describe("Password — must have min 8 chars, uppercase, lowercase, digit, and special character (e.g. Client@2024!)"),
    },
    async ({ email, firstName, lastName, password }) => {
      try {
        const result = await apiRequest<{ id: string; email: string; firstName: string; lastName: string; role: string }>(
          "/api/auth/register",
          { method: "POST", body: { email, firstName, lastName, password, authProvider: "LOCAL", providerId: null }, useTenant: true }
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

  // --- SuperAdmin Login ---
  server.tool(
    "platform_login",
    "Request OTP login for a SUPER_ADMIN. No tenant required — do NOT call set_tenant_id before this. Returns a challengeId. Grants access to platform tools (list_businesses, create_business, create_business_admin, etc.). Use login instead for CLIENT or ADMIN users of a specific business.",
    {
      email: z.string().email().describe("SuperAdmin email"),
      password: z.string().describe("SuperAdmin password"),
    },
    async ({ email, password }) => {
      try {
        const result = await apiRequest<{ challengeId: string; message: string; expiresInSeconds: number }>(
          "/platform/auth/login",
          { method: "POST", body: { email, password } }
        );
        return {
          content: [{
            type: "text" as const,
            text: `SuperAdmin OTP challenge created.\n- Challenge ID: ${result.challengeId}\n- ${result.message}\n- Expires in: ${result.expiresInSeconds} seconds\n\nAsk the user for the 6-digit OTP code sent to ${email}, then use platform_verify_otp.`,
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
    "Verify the 6-digit OTP code to complete SuperAdmin login. Stores the JWT token for platform operations.",
    {
      challengeId: z.string().uuid().describe("The challenge ID from platform_login"),
      otp: z.string().length(6).describe("The 6-digit OTP code from email"),
    },
    async ({ challengeId, otp }) => {
      try {
        const result = await apiRequest<{ token: string }>(
          "/platform/auth/verify-otp",
          { method: "POST", body: { challengeId, otp } }
        );
        setAuthToken(result.token);
        return {
          content: [{
            type: "text" as const,
            text: `SuperAdmin login successful! Token stored. You can now use platform tools (list_businesses, create_business, etc.).`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `SuperAdmin OTP verification failed: ${(e as Error).message}` }] };
      }
    }
  );

  // --- Set Tenant ID at runtime ---
  server.tool(
    "set_tenant_id",
    "Set the active business/tenant context. Must be called before ANY tenant-scoped operation: login (as CLIENT/ADMIN), register, list_services, create_service, list_employees, create_employee, book_appointment, and all other business-specific tools. Not required for platform_login or SUPER_ADMIN platform tools.",
    {
      tenantId: z.string().uuid().describe("The business UUID to use as tenant ID"),
    },
    async ({ tenantId }) => {
      setTenantId(tenantId);
      return {
        content: [{
          type: "text" as const,
          text: `Tenant ID set to: ${tenantId}\nTenant-scoped tools (login, register, list_services, etc.) will now use this business.`,
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
      const tid = getTenantId();
      const token = getAuthToken();
      return {
        content: [{
          type: "text" as const,
          text: `MCP Server Config:\n- API Base URL: ${getBaseUrl()}\n- Tenant ID: ${tid || "(not set)"}\n- Authenticated: ${token ? "yes (token present)" : "no"}`,
        }],
      };
    }
  );
}
