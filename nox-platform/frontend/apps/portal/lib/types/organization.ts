export interface Organization {
  id: string;
  name: string;
  description?: string;
  logo?: string;
  createdAt: string;
  updatedAt: string;
  ownerId: string;
}

export interface CreateOrganizationRequest {
  name: string;
  description?: string;
  logo?: string;
}

export interface UpdateOrganizationRequest {
  name?: string;
  description?: string;
  logo?: string;
}

export interface OrganizationResponse {
  id: string;
  name: string;
  description?: string;
  logo?: string;
  createdAt: string;
  updatedAt: string;
  ownerId: string;
  memberCount?: number;
  role?: string;
}

export type OrganizationWithStats = OrganizationResponse & {
  memberCount: number;
  projectCount: number;
  roleCount: number;
};
