import { apiClient } from '@/lib/utils/api-client';
import {
  CreateOrganizationRequest,
  UpdateOrganizationRequest,
  OrganizationResponse,
  OrgMemberResponse,
  AddMemberRequest,
  Role,
  CreateRoleRequest,
  UpdateRoleRequest,
} from '@/lib/types';

// Organization APIs
export const organizationService = {
  async listOrganizations(page = 0, pageSize = 10) {
    return apiClient.get(`/api/v1/orgs`, {
      params: { page, pageSize },
    });
  },

  async getOrganization(orgId: string): Promise<OrganizationResponse> {
    return apiClient.get(`/api/v1/orgs/${orgId}`);
  },

  async createOrganization(
    data: CreateOrganizationRequest
  ): Promise<OrganizationResponse> {
    return apiClient.post(`/api/v1/orgs`, data);
  },

  async updateOrganization(
    orgId: string,
    data: UpdateOrganizationRequest
  ): Promise<OrganizationResponse> {
    return apiClient.put(`/api/v1/orgs/${orgId}`, data);
  },

  async deleteOrganization(orgId: string): Promise<void> {
    return apiClient.delete(`/api/v1/orgs/${orgId}`);
  },
};

// Member APIs
export const memberService = {
  async listMembers(
    orgId: string,
    page = 0,
    pageSize = 20
  ): Promise<{ content: OrgMemberResponse[]; totalPages: number; totalElements: number }> {
    return apiClient.get(`/api/v1/orgs/${orgId}/members`, {
      params: { page, pageSize },
    });
  },

  async inviteMember(
    orgId: string,
    data: AddMemberRequest
  ): Promise<{ inviteId: string; status: string }> {
    return apiClient.post(`/api/v1/orgs/${orgId}/members`, data);
  },

  async removeMember(orgId: string, userId: string): Promise<void> {
    return apiClient.delete(`/api/v1/orgs/${orgId}/members/${userId}`);
  },

  async updateMemberRole(
    orgId: string,
    userId: string,
    roleId: string
  ): Promise<OrgMemberResponse> {
    return apiClient.put(`/api/v1/orgs/${orgId}/members/${userId}`, {
      role: roleId,
    });
  },
};

// Role APIs
export const roleService = {
  async listRoles(orgId: string): Promise<Role[]> {
    return apiClient.get(`/api/v1/orgs/${orgId}/roles`);
  },

  async createRole(orgId: string, data: CreateRoleRequest): Promise<Role> {
    return apiClient.post(`/api/v1/orgs/${orgId}/roles`, data);
  },

  async updateRole(
    orgId: string,
    roleId: string,
    data: UpdateRoleRequest
  ): Promise<Role> {
    return apiClient.put(`/api/v1/orgs/${orgId}/roles/${roleId}`, data);
  },

  async deleteRole(orgId: string, roleName: string): Promise<void> {
    return apiClient.delete(`/api/v1/orgs/${orgId}/roles/${roleName}`);
  },
};
