// Real API client for NOX Platform Backend
// Base URL: http://localhost:8081

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8081";

// Storage keys
const STORAGE_KEYS = {
    tokens: "nox_tokens",
    user: "nox_user",
} as const;

// Types
interface ApiResponse<T> {
    success: boolean;
    data: T | null;
    error: {
        code: string;
        message: string;
        details?: any;
    } | null;
    timestamp: string;
}

// Auth Types
interface AuthRequest {
    email: string;
    password: string;
}

interface RegisterRequest {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
}

interface TokenResponse {
    token: string;
    refreshToken: string;
}

interface UserResponse {
    id: string;
    email: string;
    status: string;
}

// Project Types
interface ProjectResponse {
    id: string;
    name: string;
    slug: string;
    organizationId: string;
    visibility: string;
    status: string;
    description?: string;
    createdAt: string;
    updatedAt: string;
}

interface CreateProjectRequest {
    name: string;
    slug?: string;
    description?: string;
    visibility?: string;
}

// Workspace Types
interface WorkspaceResponse {
    id: string;
    projectId: string;
    name: string;
    type: string;
    createdBy: string;
    createdAt: string;
}

interface CreateWorkspaceRequest {
    name: string;
    type: string;
}

interface UpdateWorkspaceRequest {
    name?: string;
    type?: string;
}

// Snapshot Types
interface SnapshotResponse {
    id: string;
    projectId: string;
    name: string;
    description?: string;
    createdBy: string;
    createdAt: string;
}

interface CreateSnapshotRequest {
    name: string;
    description?: string;
}

// Helper functions
const getAuthHeaders = (): Record<string, string> => {
    if (typeof window === "undefined") return {};
    
    const tokens = localStorage.getItem(STORAGE_KEYS.tokens);
    if (!tokens) return {};
    
    try {
        const parsed = JSON.parse(tokens);
        return {
            "Authorization": `Bearer ${parsed.token}`,
            "Content-Type": "application/json",
        };
    } catch {
        return {
            "Content-Type": "application/json",
        };
    }
};

const handleResponse = async <T>(response: Response): Promise<ApiResponse<T>> => {
    if (!response.ok) {
        const error = await response.json().catch(() => ({ message: "Request failed" }));
        throw new Error(error.message || `HTTP ${response.status}`);
    }
    return response.json();
};

// ============ Auth API ============

export const AuthApi = {
    login: async (request: AuthRequest): Promise<ApiResponse<TokenResponse>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(request),
        });
        const data = await handleResponse<TokenResponse>(response);
        if (data.success && typeof window !== "undefined") {
            localStorage.setItem(STORAGE_KEYS.tokens, JSON.stringify(data.data));
        }
        return data;
    },

    register: async (request: RegisterRequest): Promise<ApiResponse<UserResponse>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(request),
        });
        return handleResponse(response);
    },

    logout: async (): Promise<void> => {
        if (typeof window !== "undefined") {
            localStorage.removeItem(STORAGE_KEYS.tokens);
            localStorage.removeItem(STORAGE_KEYS.user);
        }
    },

    getCurrentUser: async (): Promise<ApiResponse<UserResponse>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/auth/me`, {
            headers: getAuthHeaders(),
        });
        return handleResponse(response);
    },

    isAuthenticated: (): boolean => {
        if (typeof window === "undefined") return false;
        return !!localStorage.getItem(STORAGE_KEYS.tokens);
    },
};

// ============ Project API ============

export const ProjectApi = {
    getAll: async (): Promise<ApiResponse<ProjectResponse[]>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects`, {
            headers: getAuthHeaders(),
        });
        return handleResponse(response);
    },

    getById: async (id: string): Promise<ApiResponse<ProjectResponse>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects/${id}`, {
            headers: getAuthHeaders(),
        });
        return handleResponse(response);
    },

    create: async (request: CreateProjectRequest): Promise<ApiResponse<ProjectResponse>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects`, {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        return handleResponse(response);
    },

    update: async (id: string, request: Partial<CreateProjectRequest>): Promise<ApiResponse<ProjectResponse>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects/${id}`, {
            method: "PUT",
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        return handleResponse(response);
    },

    delete: async (id: string): Promise<ApiResponse<void>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects/${id}`, {
            method: "DELETE",
            headers: getAuthHeaders(),
        });
        return handleResponse(response);
    },
};

// ============ Workspace API ============

export const WorkspaceApi = {
    getAll: async (projectId: string): Promise<ApiResponse<WorkspaceResponse[]>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects/${projectId}/workspaces`, {
            headers: getAuthHeaders(),
        });
        return handleResponse(response);
    },

    getById: async (projectId: string, workspaceId: string): Promise<ApiResponse<WorkspaceResponse>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects/${projectId}/workspaces/${workspaceId}`, {
            headers: getAuthHeaders(),
        });
        return handleResponse(response);
    },

    create: async (projectId: string, request: CreateWorkspaceRequest): Promise<ApiResponse<WorkspaceResponse>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects/${projectId}/workspaces`, {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        return handleResponse(response);
    },

    update: async (projectId: string, workspaceId: string, request: UpdateWorkspaceRequest): Promise<ApiResponse<WorkspaceResponse>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects/${projectId}/workspaces/${workspaceId}`, {
            method: "PUT",
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        return handleResponse(response);
    },

    delete: async (projectId: string, workspaceId: string): Promise<ApiResponse<void>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects/${projectId}/workspaces/${workspaceId}`, {
            method: "DELETE",
            headers: getAuthHeaders(),
        });
        return handleResponse(response);
    },
};

// ============ Snapshot API ============

export const SnapshotApi = {
    getByWorkspace: async (projectId: string, workspaceId: string): Promise<ApiResponse<SnapshotResponse[]>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects/${projectId}/snapshots?workspaceId=${workspaceId}`, {
            headers: getAuthHeaders(),
        });
        return handleResponse(response);
    },

    create: async (projectId: string, request: CreateSnapshotRequest): Promise<ApiResponse<SnapshotResponse>> => {
        const response = await fetch(`${API_BASE_URL}/api/v1/projects/${projectId}/snapshots/commit`, {
            method: "POST",
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        return handleResponse(response);
    },
};

export type {
    ApiResponse,
    AuthRequest,
    RegisterRequest,
    TokenResponse,
    UserResponse,
    ProjectResponse,
    CreateProjectRequest,
    WorkspaceResponse,
    CreateWorkspaceRequest,
    UpdateWorkspaceRequest,
    SnapshotResponse,
    CreateSnapshotRequest,
};
