import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { ApiClient } from "../api-client.js";

interface ServiceResponse {
  id: string;
  name: string;
  description: string | null;
  price: number;
  durationMinutes: number;
  active: boolean;
}

export function registerServiceTools(server: McpServer, client: ApiClient): void {

  server.tool(
    "list_services",
    "List all services offered by the current business/tenant. Public endpoint, no auth required.",
    {},
    async () => {
      try {
        const services = await client.request<ServiceResponse[]>("/api/services", { useTenant: true });
        if (services.length === 0) {
          return { content: [{ type: "text" as const, text: "No services found for this business." }] };
        }
        const lines = services.map(
          (s) => `- **${s.name}** (${s.active ? "active" : "inactive"})\n  ID: ${s.id}\n  Price: $${s.price} | Duration: ${s.durationMinutes} min\n  ${s.description || "No description"}`
        );
        return { content: [{ type: "text" as const, text: `Services (${services.length}):\n\n${lines.join("\n\n")}` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to list services: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "get_service",
    "Get details of a specific service by ID.",
    {
      serviceId: z.string().uuid().describe("The service UUID"),
    },
    async ({ serviceId }) => {
      try {
        const s = await client.request<ServiceResponse>(`/api/services/${serviceId}`);
        return {
          content: [{
            type: "text" as const,
            text: `Service: ${s.name}\n- ID: ${s.id}\n- Price: $${s.price}\n- Duration: ${s.durationMinutes} min\n- Active: ${s.active}\n- Description: ${s.description || "N/A"}`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to get service: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "create_service",
    "Create a new service for the current business. Requires ADMIN auth.",
    {
      name: z.string().describe("Service name"),
      price: z.number().positive().describe("Service price"),
      durationMinutes: z.number().int().positive().describe("Duration in minutes"),
      description: z.string().optional().describe("Service description"),
    },
    async ({ name, price, durationMinutes, description }) => {
      try {
        const s = await client.request<ServiceResponse>("/api/services", {
          method: "POST",
          body: { name, price, durationMinutes, description },
          useAuth: true,
        });
        return {
          content: [{
            type: "text" as const,
            text: `Service created!\n- ID: ${s.id}\n- Name: ${s.name}\n- Price: $${s.price}\n- Duration: ${s.durationMinutes} min`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to create service: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "update_service",
    "Update an existing service. Requires ADMIN auth. Only provided fields are updated.",
    {
      serviceId: z.string().uuid().describe("The service UUID to update"),
      name: z.string().optional().describe("New service name"),
      price: z.number().positive().optional().describe("New price"),
      durationMinutes: z.number().int().positive().optional().describe("New duration in minutes"),
      description: z.string().optional().describe("New description"),
    },
    async ({ serviceId, name, price, durationMinutes, description }) => {
      try {
        const body: Record<string, unknown> = {};
        if (name !== undefined) body.name = name;
        if (price !== undefined) body.price = price;
        if (durationMinutes !== undefined) body.durationMinutes = durationMinutes;
        if (description !== undefined) body.description = description;

        const s = await client.request<ServiceResponse>(`/api/services/${serviceId}`, {
          method: "PUT",
          body,
          useAuth: true,
        });
        return {
          content: [{
            type: "text" as const,
            text: `Service updated!\n- ID: ${s.id}\n- Name: ${s.name}\n- Price: $${s.price}\n- Duration: ${s.durationMinutes} min`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to update service: ${(e as Error).message}` }] };
      }
    }
  );
}
