export interface User {
  id: string;
  email: string;
  name: string;
  avatar?: string;
  bio?: string;
  emailVerified: boolean;
  mfaEnabled: boolean;
  createdAt: string;
  updatedAt: string;
  lastLoginAt?: string;
}

export interface OrgMember {
  id: string;
  userId: string;
  orgId: string;
  role: string;
  joinedAt: string;
  user?: User;
}

export interface OrgMemberResponse {
  id: string;
  userId: string;
  email: string;
  name: string;
  avatar?: string;
  role: string;
  joinedAt: string;
  status: 'active' | 'pending' | 'inactive';
}

export interface AddMemberRequest {
  email: string;
  role: string;
}

export interface UpdateMemberRoleRequest {
  role: string;
}

export interface InviteResponse {
  inviteId: string;
  email: string;
  status: 'pending' | 'sent' | 'accepted';
  expiresAt: string;
  createdAt: string;
}
