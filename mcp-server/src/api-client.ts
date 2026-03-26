const API_BASE_URL = process.env.API_BASE_URL || "http://localhost:8080";

let tenantId: string = process.env.TENANT_ID || "";
let authToken: string | null = null;

export function setAuthToken(token: string): void {
  authToken = token;
}

export function getAuthToken(): string | null {
  return authToken;
}

export function setTenantId(id: string): void {
  tenantId = id;
}

export function getTenantId(): string {
  return tenantId;
}

export function getBaseUrl(): string {
  return API_BASE_URL;
}

interface ApiOptions {
  method?: string;
  body?: unknown;
  useTenant?: boolean;
  useAuth?: boolean;
}

export async function apiRequest<T>(path: string, options: ApiOptions = {}): Promise<T> {
  const { method = "GET", body, useTenant = false, useAuth = false } = options;

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };

  if (useTenant && tenantId) {
    headers["X-Tenant-Id"] = tenantId;
  }

  if (useAuth && authToken) {
    headers["Authorization"] = `Bearer ${authToken}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const text = await response.text();

  if (!response.ok) {
    let errorMessage: string;
    try {
      const errorJson = JSON.parse(text);
      // Collect all useful error fields
      const parts: string[] = [];
      if (errorJson.message) parts.push(errorJson.message);
      if (errorJson.error && errorJson.error !== errorJson.message) parts.push(errorJson.error);
      if (errorJson.details) parts.push(typeof errorJson.details === "string" ? errorJson.details : JSON.stringify(errorJson.details));
      if (errorJson.errors) parts.push(typeof errorJson.errors === "string" ? errorJson.errors : JSON.stringify(errorJson.errors));
      errorMessage = parts.length > 0 ? parts.join(" | ") : text;
    } catch {
      errorMessage = text;
    }
    throw new Error(`API error ${response.status}: ${errorMessage}`);
  }

  if (!text) return undefined as T;

  return JSON.parse(text) as T;
}
