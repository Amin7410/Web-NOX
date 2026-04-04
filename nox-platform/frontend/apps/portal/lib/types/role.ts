export type PermissionLevel = 'read' | 'write' | 'manage' | 'admin';

export interface Permission {
  id: string;
  name: string;
  description: string;
  resource: 'projects' | 'organizations' | 'teams' | 'roles' | 'settings';
  level: PermissionLevel;
}

export interface Role {
  id: string;
  name: string;
  description?: string;
  level?: number;
  orgId?: string;
  isBuiltIn: boolean;
  permissions: Permission[];
  createdAt: string;
  updatedAt: string;
}

export interface RoleResponse {
  id: string;
  name: string;
  description?: string;
  level?: number;
  isBuiltIn: boolean;
  permissions: Permission[];
  memberCount?: number;
}

export interface CreateRoleRequest {
  name: string;
  description?: string;
  permissions: string[]; // permission IDs
}

export interface UpdateRoleRequest {
  name?: string;
  description?: string;
  permissions?: string[]; // permission IDs
}

// Built-in roles
export const BUILT_IN_ROLES = {
  ADMIN: 'admin',
  EDITOR: 'editor',
  VIEWER: 'viewer',
} as const;

export type BuiltInRole = typeof BUILT_IN_ROLES[keyof typeof BUILT_IN_ROLES];
