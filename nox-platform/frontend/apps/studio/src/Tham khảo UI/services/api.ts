// ─────────────────────────────────────────────
//  NOX Studio — Base API Client
//  Reads JWT from localStorage, proxied by Vite
//  to Spring Boot at localhost:8081
// ─────────────────────────────────────────────

const BASE_URL = '/api';

function getToken(): string | null {
    return localStorage.getItem('accessToken');
}

type ApiOptions = Omit<RequestInit, 'body'> & {
    body?: object;
};

export class ApiError extends Error {
    constructor(public status: number, message: string) {
        super(message);
    }
}

async function request<T>(endpoint: string, options: ApiOptions = {}): Promise<T> {
    const token = getToken();

    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        ...(options.headers as Record<string, string> || {}),
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(`${BASE_URL}${endpoint}`, {
        ...options,
        headers,
        body: options.body ? JSON.stringify(options.body) : undefined,
    });

    if (res.status === 204) return undefined as T;

    if (res.status === 401) {
        // Token expired or missing — show message, don't hard redirect if in dev
        const portalUrl = import.meta.env.VITE_PORTAL_URL || 'http://localhost:3000';
        console.warn('[NOX API] 401 Unauthorized → Redirecting to portal login...');
        window.location.href = `${portalUrl}/auth/login?redirect=${encodeURIComponent(window.location.href)}`;
        throw new ApiError(401, 'Unauthorized — Redirecting to login...');
    }

    const json = await res.json().catch(() => null);

    if (!res.ok) {
        const message = json?.message || json?.error || `HTTP ${res.status}`;
        throw new ApiError(res.status, message);
    }

    return json as T;
}

export const api = {
    get: <T>(endpoint: string, options?: ApiOptions) =>
        request<T>(endpoint, { ...options, method: 'GET' }),

    post: <T>(endpoint: string, body: object, options?: ApiOptions) =>
        request<T>(endpoint, { ...options, method: 'POST', body }),

    patch: <T>(endpoint: string, body: object, options?: ApiOptions) =>
        request<T>(endpoint, { ...options, method: 'PATCH', body }),

    put: <T>(endpoint: string, body: object, options?: ApiOptions) =>
        request<T>(endpoint, { ...options, method: 'PUT', body }),

    delete: <T>(endpoint: string, options?: ApiOptions) =>
        request<T>(endpoint, { ...options, method: 'DELETE' }),
};
