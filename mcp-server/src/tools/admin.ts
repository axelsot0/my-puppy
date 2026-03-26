import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import { apiRequest } from "../api-client.js";

interface UserResponse {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  active: boolean;
}

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
}

interface AvailabilityResponse {
  id: string;
  employeeId: string;
  employeeName: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
}

export function registerAdminTools(server: McpServer): void {

  // --- Employees ---

  server.tool(
    "list_employees",
    "List all employees in the current business. Requires ADMIN auth (must be authenticated via login as a business ADMIN — SUPER_ADMIN token does NOT work here and will return 403). Workflow: call set_tenant_id, then login as business admin, then call this tool.",
    {},
    async () => {
      try {
        const employees = await apiRequest<UserResponse[]>("/api/employees", { useAuth: true });
        if (employees.length === 0) {
          return { content: [{ type: "text" as const, text: "No employees found." }] };
        }
        const lines = employees.map(
          (e) => `- ${e.firstName} ${e.lastName} (${e.email})\n  ID: ${e.id} | Active: ${e.active}`
        );
        return { content: [{ type: "text" as const, text: `Employees (${employees.length}):\n\n${lines.join("\n\n")}` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to list employees: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "create_employee",
    "Create a new employee in the current business. Requires ADMIN auth.",
    {
      email: z.string().email().describe("Employee email"),
      firstName: z.string().describe("First name"),
      lastName: z.string().describe("Last name"),
      password: z.string().describe("Employee password"),
    },
    async ({ email, firstName, lastName, password }) => {
      try {
        const emp = await apiRequest<UserResponse>("/api/employees", {
          method: "POST",
          body: { email, firstName, lastName, password },
          useAuth: true,
        });
        return {
          content: [{
            type: "text" as const,
            text: `Employee created!\n- ID: ${emp.id}\n- Name: ${emp.firstName} ${emp.lastName}\n- Email: ${emp.email}`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to create employee: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "deactivate_employee",
    "Deactivate an employee. Requires ADMIN auth.",
    {
      employeeId: z.string().uuid().describe("Employee UUID to deactivate"),
    },
    async ({ employeeId }) => {
      try {
        await apiRequest<void>(`/api/employees/${employeeId}`, { method: "DELETE", useAuth: true });
        return { content: [{ type: "text" as const, text: `Employee ${employeeId} deactivated.` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to deactivate employee: ${(e as Error).message}` }] };
      }
    }
  );

  // --- Employee-Service assignment ---

  server.tool(
    "assign_service_to_employee",
    "Assign a service to an employee so they can perform it. Requires ADMIN auth.",
    {
      employeeId: z.string().uuid().describe("Employee UUID"),
      serviceId: z.string().uuid().describe("Service UUID"),
    },
    async ({ employeeId, serviceId }) => {
      try {
        await apiRequest<void>(`/api/employees/${employeeId}/services/${serviceId}`, { method: "POST", useAuth: true });
        return { content: [{ type: "text" as const, text: `Service ${serviceId} assigned to employee ${employeeId}.` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to assign service: ${(e as Error).message}` }] };
      }
    }
  );

  // --- Appointments management ---

  server.tool(
    "employee_appointments",
    "List appointments for a specific employee on a given date. Requires ADMIN or EMPLOYEE auth.",
    {
      employeeId: z.string().uuid().describe("Employee UUID"),
      date: z.string().optional().describe("Date (YYYY-MM-DD). Defaults to today."),
    },
    async ({ employeeId, date }) => {
      try {
        const query = date ? `?date=${date}` : "";
        const appointments = await apiRequest<AppointmentResponse[]>(
          `/api/appointments/employee/${employeeId}${query}`,
          { useAuth: true }
        );
        if (appointments.length === 0) {
          return { content: [{ type: "text" as const, text: `No appointments for this employee on ${date || "today"}.` }] };
        }
        const lines = appointments.map(
          (a) => `- ${a.time} | ${a.serviceName} | Client: ${a.clientName} | Status: ${a.status}\n  ID: ${a.id}`
        );
        return { content: [{ type: "text" as const, text: `Appointments (${appointments.length}):\n\n${lines.join("\n\n")}` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to list employee appointments: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "assign_appointment",
    "Assign an employee to an appointment. Requires ADMIN auth.",
    {
      appointmentId: z.string().uuid().describe("Appointment UUID"),
      employeeId: z.string().uuid().describe("Employee UUID to assign"),
    },
    async ({ appointmentId, employeeId }) => {
      try {
        const a = await apiRequest<AppointmentResponse>(
          `/api/appointments/${appointmentId}/assign/${employeeId}`,
          { method: "PUT", useAuth: true }
        );
        return {
          content: [{
            type: "text" as const,
            text: `Appointment assigned!\n- ${a.serviceName} on ${a.date} at ${a.time}\n- Employee: ${a.employeeName}\n- Status: ${a.status}`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to assign appointment: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "reject_appointment",
    "Reject an appointment. Requires ADMIN auth.",
    {
      appointmentId: z.string().uuid().describe("Appointment UUID to reject"),
    },
    async ({ appointmentId }) => {
      try {
        const a = await apiRequest<AppointmentResponse>(`/api/appointments/${appointmentId}/reject`, {
          method: "PUT",
          useAuth: true,
        });
        return { content: [{ type: "text" as const, text: `Appointment ${appointmentId} rejected. Status: ${a.status}` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to reject appointment: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "mark_appointment_done",
    "Mark an appointment as completed. Requires ADMIN or EMPLOYEE auth.",
    {
      appointmentId: z.string().uuid().describe("Appointment UUID to mark as done"),
    },
    async ({ appointmentId }) => {
      try {
        const a = await apiRequest<AppointmentResponse>(`/api/appointments/${appointmentId}/done`, {
          method: "PUT",
          useAuth: true,
        });
        return { content: [{ type: "text" as const, text: `Appointment ${appointmentId} marked as done. Status: ${a.status}` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to mark appointment as done: ${(e as Error).message}` }] };
      }
    }
  );

  // --- Availability ---

  server.tool(
    "list_availability",
    "List availability schedule for an employee. Requires ADMIN or EMPLOYEE auth.",
    {
      employeeId: z.string().uuid().describe("Employee UUID"),
    },
    async ({ employeeId }) => {
      try {
        const slots = await apiRequest<AvailabilityResponse[]>(
          `/api/availabilities/employee/${employeeId}`,
          { useAuth: true }
        );
        if (slots.length === 0) {
          return { content: [{ type: "text" as const, text: "No availability configured for this employee." }] };
        }
        const lines = slots.map(
          (s) => `- ${s.dayOfWeek}: ${s.startTime} - ${s.endTime} (ID: ${s.id})`
        );
        return { content: [{ type: "text" as const, text: `Availability for ${slots[0].employeeName}:\n\n${lines.join("\n")}` }] };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to list availability: ${(e as Error).message}` }] };
      }
    }
  );

  server.tool(
    "create_availability",
    "Create an availability slot for an employee. Requires ADMIN or EMPLOYEE auth. Schema: employeeId (UUID), dayOfWeek (MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY|SUNDAY), startTime (HH:mm), endTime (HH:mm). Returns: id, employeeName, dayOfWeek, startTime, endTime.",
    {
      employeeId: z.string().uuid().describe("Employee UUID"),
      dayOfWeek: z.enum(["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"]).describe("Day of week"),
      startTime: z.string().describe("Start time (HH:mm), e.g. '09:00'"),
      endTime: z.string().describe("End time (HH:mm), e.g. '18:00'"),
    },
    async ({ employeeId, dayOfWeek, startTime, endTime }) => {
      try {
        const s = await apiRequest<AvailabilityResponse>("/api/availabilities", {
          method: "POST",
          body: { employeeId, dayOfWeek, startTime, endTime },
          useAuth: true,
        });
        return {
          content: [{
            type: "text" as const,
            text: `Availability created!\n- ${s.dayOfWeek}: ${s.startTime} - ${s.endTime}\n- Employee: ${s.employeeName}\n- ID: ${s.id}`,
          }],
        };
      } catch (e: unknown) {
        return { content: [{ type: "text" as const, text: `Failed to create availability: ${(e as Error).message}` }] };
      }
    }
  );
}
