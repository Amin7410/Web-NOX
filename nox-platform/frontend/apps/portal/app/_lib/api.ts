/**
 * Central API Utility for NOX Portal
 * Handles base URL, common headers, and error parsing.
 * Aligned with backend ApiResponse structure.
 */

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081/api/v1";

export interface ApiErrorDetail {
    code: string;
    message: string;
    details?: any;
}

export interface ApiResponse<T = any> {
    success: boolean;
    data: T;
    error?: ApiErrorDetail;
    timestamp: string;
}

export class ApiError extends Error {
    constructor(public status: number, public message: string, public code?: string, public details?: any) {
        super(message);
        this.name = "ApiError";
    }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
    const url = `${BASE_URL}${path}`;

    const headers = new Headers(options.headers);
    if (!(options.body instanceof FormData)) {
        headers.set("Content-Type", "application/json");
    }

    // Attach JWT and Organization Context if available (client-side only)
    if (typeof window !== "undefined") {
        const token = localStorage.getItem("access_token");
        if (token) {
            headers.set("Authorization", `Bearer ${token}`);
        }

        const orgId = localStorage.getItem("active_org_id");
        if (orgId) {
            headers.set("X-Org-Id", orgId);
        }
    }

    const response = await fetch(url, {
        ...options,
        headers,
    });

    let result: ApiResponse<T>;
    try {
        result = await response.json();
    } catch (e) {
        throw new ApiError(response.status, "Invalid response from server");
    }

    if (!response.ok || !result.success) {
        const errorMsg = result.error?.message || "An error occurred";
        const errorCode = result.error?.code || "INTERNAL_ERROR";
        throw new ApiError(response.status, errorMsg, errorCode, result.error?.details);
    }

    return result.data;
}

export const api = {
    get: <T>(path: string, options?: RequestInit) => 
        request<T>(path, { ...options, method: "GET" }),
    
    post: <T>(path: string, body: any, options?: RequestInit) => 
        request<T>(path, { 
            ...options, 
            method: "POST", 
            body: body instanceof FormData ? body : JSON.stringify(body) 
        }),
    
    put: <T>(path: string, body: any, options?: RequestInit) => 
        request<T>(path, { 
            ...options, 
            method: "PUT", 
            body: body instanceof FormData ? body : JSON.stringify(body) 
        }),
    
    delete: <T>(path: string, options?: RequestInit) => 
        request<T>(path, { ...options, method: "DELETE" }),
};
