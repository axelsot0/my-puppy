import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { apiRequest } from "../api-client.js";

interface AppointmentResponse {
  id: string;
  clientId: string;
  clientName: string;
  serviceId: string;
  serviceName: string;
  employeeId: string | null;
  employeeName: string | null;
  date: string;
  time: string;
  status: string;
  notes: string | null;
  metadata: string | null;
}

function formatAppointment(a: AppointmentResponse): string {
  return `- **${a.serviceName}** on ${a.date} at ${a.time}\n  ID: ${a.id}\n  Status: ${a.status}\n  Client: ${a.clientName}\n  Employee: ${a.employeeName || "Unassigned"}\n  Notes: ${a.notes || "None"}`;
}

export function registerAppointmentTools(server: McpServer): void {

  server.tool(
    "book_appointment",
    "Book a new appointment as a CLIENT. Requires authentication.",
    {
      serviceId: z.string().uuid().describe("The service UUID to book"),
      date: z.string().describe("Appointment date (YYYY-MM-DD)"),
      time: z.string().describe("Appointment time (HH:mm)"),
      notes: z.string().optional().describe("Optional notes (e.g., 'White leather shoes, size 42')"),
    },
    async ({ serviceId, date, time, notes }) => {
      try {
        const a = await apiRequest<AppointmentResponse>("/api/appointments", {
          method: "POST",
          body: { serviceId, date, time, notes },
          useAuth: true,
        });
        return {
          content: [{
            type: "text" as const,
            text: `Appointment booked!\n${formatAppointment(a)}`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to book appointment: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "my_appointments",
    "List all appointments for the currently logged-in CLIENT.",
    {},
    async () => {
      try {
        const appointments = await apiRequest<AppointmentResponse[]>("/api/appointments/mine", { useAuth: true });
        if (appointments.length === 0) {
          return { content: [{ type: "text" as const, text: "You have no appointments." }] };
        }
        const lines = appointments.map(formatAppointment);
        return { content: [{ type: "text" as const, text: `Your appointments (${appointments.length}):\n\n${lines.join("\n\n")}` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to list appointments: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "cancel_appointment",
    "Cancel an appointment. Available for CLIENT and ADMIN roles.",
    {
      appointmentId: z.string().uuid().describe("The appointment UUID to cancel"),
    },
    async ({ appointmentId }) => {
      try {
        const a = await apiRequest<AppointmentResponse>(`/api/appointments/${appointmentId}/cancel`, {
          method: "PUT",
          useAuth: true,
        });
        return {
          content: [{
            type: "text" as const,
            text: `Appointment cancelled.\n${formatAppointment(a)}`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to cancel appointment: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "get_appointment",
    "Get details of a specific appointment by ID.",
    {
      appointmentId: z.string().uuid().describe("The appointment UUID"),
    },
    async ({ appointmentId }) => {
      try {
        const a = await apiRequest<AppointmentResponse>(`/api/appointments/${appointmentId}`, { useAuth: true });
        return { content: [{ type: "text" as const, text: formatAppointment(a) }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to get appointment: ${(e as Error).message}` }] };
      }
    }
  );
}
