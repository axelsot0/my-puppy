import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { apiRequest } from "../api-client.js";

interface BusinessResponse {
  id: string;
  name: string;
  slug: string;
  type: string;
  description: string | null;
  address: string | null;
  phone: string | null;
  active: boolean;
}

interface UserResponse {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
}

interface SuperAdminResponse {
  id: string;
  email: string;
  name: string;
}

export function registerPlatformTools(server: McpServer): void {

  // --- Businesses ---

  server.tool(
    "list_businesses",
    "List all businesses on the platform. Requires SUPER_ADMIN auth.",
    {},
    async () => {
      try {
        const businesses = await apiRequest<BusinessResponse[]>("/platform/businesses", { useAuth: true });
        if (businesses.length === 0) {
          return { content: [{ type: "text" as const, text: "No businesses found." }] };
        }
        const lines = businesses.map(
          (b) => `- **${b.name}** (${b.slug}) [${b.active ? "ACTIVE" : "INACTIVE"}]\n  ID: ${b.id}\n  Type: ${b.type}\n  ${b.description || ""}\n  ${b.address || ""} ${b.phone || ""}`
        );
        return { content: [{ type: "text" as const, text: `Businesses (${businesses.length}):\n\n${lines.join("\n\n")}` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to list businesses: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "get_business",
    "Get details of a specific business. Requires SUPER_ADMIN auth.",
    {
      businessId: z.string().uuid().describe("Business UUID"),
    },
    async ({ businessId }) => {
      try {
        const b = await apiRequest<BusinessResponse>(`/platform/businesses/${businessId}`, { useAuth: true });
        return {
          content: [{
            type: "text" as const,
            text: `Business: ${b.name}\n- ID: ${b.id}\n- Slug: ${b.slug}\n- Type: ${b.type}\n- Active: ${b.active}\n- Description: ${b.description || "N/A"}\n- Address: ${b.address || "N/A"}\n- Phone: ${b.phone || "N/A"}`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to get business: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "create_business",
    "Create a new business/tenant on the platform. Requires SUPER_ADMIN auth.",
    {
      name: z.string().describe("Business name"),
      slug: z.string().describe("URL-friendly slug (lowercase, no spaces)"),
      type: z.string().describe("Business type (e.g., 'laundry', 'salon', 'veterinary')"),
      description: z.string().optional().describe("Business description"),
      address: z.string().optional().describe("Business address"),
      phone: z.string().optional().describe("Business phone"),
    },
    async ({ name, slug, type, description, address, phone }) => {
      try {
        const b = await apiRequest<BusinessResponse>("/platform/businesses", {
          method: "POST",
          body: { name, slug, type, description, address, phone },
          useAuth: true,
        });
        return {
          content: [{
            type: "text" as const,
            text: `Business created!\n- ID: ${b.id}\n- Name: ${b.name}\n- Slug: ${b.slug}\n- Type: ${b.type}\n\nNext steps:\n1. Call set_tenant_id with ID: ${b.id} (enables tenant-scoped operations)\n2. Call create_business_admin with businessId: ${b.id} to create the admin user`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to create business: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "update_business",
    "Update an existing business. Requires SUPER_ADMIN auth. Only provided fields are updated.",
    {
      businessId: z.string().uuid().describe("Business UUID"),
      name: z.string().optional().describe("New business name"),
      type: z.string().optional().describe("New business type"),
      description: z.string().optional().describe("New description"),
      address: z.string().optional().describe("New address"),
      phone: z.string().optional().describe("New phone"),
    },
    async ({ businessId, name, type, description, address, phone }) => {
      try {
        const body: Record<string, unknown> = {};
        if (name !== undefined) body.name = name;
        if (type !== undefined) body.type = type;
        if (description !== undefined) body.description = description;
        if (address !== undefined) body.address = address;
        if (phone !== undefined) body.phone = phone;

        const b = await apiRequest<BusinessResponse>(`/platform/businesses/${businessId}`, {
          method: "PUT",
          body,
          useAuth: true,
        });
        return { content: [{ type: "text" as const, text: `Business updated! Name: ${b.name}, Type: ${b.type}` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to update business: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "deactivate_business",
    "Deactivate a business. Requires SUPER_ADMIN auth.",
    {
      businessId: z.string().uuid().describe("Business UUID to deactivate"),
    },
    async ({ businessId }) => {
      try {
        const b = await apiRequest<BusinessResponse>(`/platform/businesses/${businessId}/deactivate`, {
          method: "PUT",
          useAuth: true,
        });
        return { content: [{ type: "text" as const, text: `Business "${b.name}" deactivated.` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to deactivate business: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "activate_business",
    "Activate a business. Requires SUPER_ADMIN auth.",
    {
      businessId: z.string().uuid().describe("Business UUID to activate"),
    },
    async ({ businessId }) => {
      try {
        const b = await apiRequest<BusinessResponse>(`/platform/businesses/${businessId}/activate`, {
          method: "PUT",
          useAuth: true,
        });
        return { content: [{ type: "text" as const, text: `Business "${b.name}" activated.` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to activate business: ${(e as Error).message}` }] };
      }
    }
  );

  // --- Business Admin ---

  server.tool(
    "create_business_admin",
    "Create an admin user for a specific business. Requires SUPER_ADMIN auth. Password policy: min 8 chars, at least one uppercase, one lowercase, one digit, and one special character (e.g. Admin@2024!).",
    {
      businessId: z.string().uuid().describe("Business UUID"),
      email: z.string().email().describe("Admin email"),
      firstName: z.string().describe("Admin first name"),
      lastName: z.string().describe("Admin last name"),
      password: z.string().describe("Admin password — must have min 8 chars, uppercase, lowercase, digit, and special character (e.g. Admin@2024!)"),
    },
    async ({ businessId, email, firstName, lastName, password }) => {
      try {
        const u = await apiRequest<UserResponse>(`/platform/businesses/${businessId}/admin`, {
          method: "POST",
          body: { email, firstName, lastName, password },
          useAuth: true,
        });
        return {
          content: [{
            type: "text" as const,
            text: `Business admin created!\n- ID: ${u.id}\n- Name: ${u.firstName} ${u.lastName}\n- Email: ${u.email}\n- Role: ${u.role}`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to create business admin: ${(e as Error).message}` }] };
      }
    }
  );

  // --- Super Admins ---

  server.tool(
    "list_super_admins",
    "List all super admins on the platform. Requires SUPER_ADMIN auth.",
    {},
    async () => {
      try {
        const admins = await apiRequest<SuperAdminResponse[]>("/platform/admins", { useAuth: true });
        if (admins.length === 0) {
          return { content: [{ type: "text" as const, text: "No super admins found." }] };
        }
        const lines = admins.map((a) => `- ${a.name} (${a.email})\n  ID: ${a.id}`);
        return { content: [{ type: "text" as const, text: `Super Admins (${admins.length}):\n\n${lines.join("\n\n")}` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to list super admins: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "create_super_admin",
    "Create a new super admin. Requires SUPER_ADMIN auth.",
    {
      email: z.string().email().describe("New super admin email"),
      password: z.string().describe("Password"),
      name: z.string().describe("Display name"),
    },
    async ({ email, password, name }) => {
      try {
        const a = await apiRequest<SuperAdminResponse>("/platform/admins", {
          method: "POST",
          body: { email, password, name },
          useAuth: true,
        });
        return {
          content: [{
            type: "text" as const,
            text: `Super admin created!\n- ID: ${a.id}\n- Name: ${a.name}\n- Email: ${a.email}`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to create super admin: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "delete_super_admin",
    "Delete a super admin. Cannot delete yourself. Requires SUPER_ADMIN auth.",
    {
      adminId: z.string().uuid().describe("Super admin UUID to delete"),
    },
    async ({ adminId }) => {
      try {
        await apiRequest<void>(`/platform/admins/${adminId}`, { method: "DELETE", useAuth: true });
        return { content: [{ type: "text" as const, text: `Super admin ${adminId} deleted.` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to delete super admin: ${(e as Error).message}` }] };
      }
    }
  );
}
