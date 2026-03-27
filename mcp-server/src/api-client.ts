const API_BASE_URL = (process.env.API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");

export class ApiClient {
  private tenantId: string = "";
  private authToken: string | null = null;

  setAuthToken(token: string): void {
    this.authToken = token;
  }

  getAuthToken(): string | null {
    return this.authToken;
  }

  setTenantId(id: string): void {
    this.tenantId = id;
  }

  getTenantId(): string {
    return this.tenantId;
  }

  getBaseUrl(): string {
    return API_BASE_URL;
  }

  async request<T>(path: string, options: ApiOptions = {}): Promise<T> {
    const { method = "GET", body, useTenant = false, useAuth = false } = options;

    const headers: Record<string, string> = {
      "Content-Type": "application/json",
    };

    if (useTenant && this.tenantId) {
      headers["X-Tenant-Id"] = this.tenantId;
    }

    if (useAuth && this.authToken) {
      headers["Authorization"] = `Bearer ${this.authToken}`;
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
        const parts: string[] = [];
        if (errorJson.message) parts.push(errorJson.message);
        if (errorJson.error && errorJson.error !== errorJson.message) parts.push(errorJson.error);
        if (errorJson.details) parts.push(typeof errorJson.details === "string" ? errorJson.details : JSON.stringify(errorJson.details));
        if (errorJson.errors) parts.push(typeof errorJson.errors === "string" ? errorJson.errors : JSON.stringify(errorJson.errors));
        if (errorJson.violations && Array.isArray(errorJson.violations)) {
          const violationMessages = errorJson.violations.map((v: { field?: string; message?: string }) =>
            v.field ? `${v.field}: ${v.message}` : v.message
          ).filter(Boolean);
          if (violationMessages.length > 0) parts.push(violationMessages.join(", "));
        }
        errorMessage = parts.length > 0 ? parts.join(" | ") : (text || `HTTP ${response.status}`);
      } catch {
        errorMessage = text;
      }
      throw new Error(`API error ${response.status}: ${errorMessage}`);
    }

    if (!text) return undefined as T;

    return JSON.parse(text) as T;
  }
}

interface ApiOptions {
  method?: string;
  body?: unknown;
  useTenant?: boolean;
  useAuth?: boolean;
}
