'use client';

import { Button } from '@nox/ui';
import { Trash2 } from 'lucide-react';
import type { OrgMemberResponse, Role } from '@/lib/types';

interface MemberListProps {
  members: OrgMemberResponse[];
  roles: Role[];
  isLoading?: boolean;
  onRemoveMember: (userId: string) => Promise<void>;
  onUpdateRole: (userId: string, roleId: string) => Promise<void>;
  currentUserId?: string;
}

export function MemberList({
  members,
  roles,
  isLoading = false,
  onRemoveMember,
  onUpdateRole,
  currentUserId,
}: MemberListProps) {
  if (members.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-[rgb(var(--text-sub))] text-[14px]">No members yet</p>
      </div>
    );
  }

  const handleRemove = async (member: OrgMemberResponse) => {
    if (confirm(`Are you sure you want to remove ${member.name} from this organization?`)) {
      await onRemoveMember(member.userId);
    }
  };

  return (
    <>
      <div className="rounded-lg border border-[rgb(var(--border))] overflow-hidden">
        <table className="w-full">
          <thead className="bg-[rgb(var(--surface))] border-b border-[rgb(var(--border))]">
            <tr>
              <th className="px-6 py-3 text-left text-[12px] font-semibold text-[rgb(var(--muted-foreground))] uppercase">
                Member
              </th>
              <th className="px-6 py-3 text-left text-[12px] font-semibold text-[rgb(var(--muted-foreground))] uppercase">
                Role
              </th>
              <th className="px-6 py-3 text-left text-[12px] font-semibold text-[rgb(var(--muted-foreground))] uppercase">
                Joined
              </th>
              <th className="px-6 py-3 text-left text-[12px] font-semibold text-[rgb(var(--muted-foreground))] uppercase">
                Status
              </th>
              <th className="px-6 py-3 text-right text-[12px] font-semibold text-[rgb(var(--muted-foreground))] uppercase">
                Actions
              </th>
            </tr>
          </thead>
          <tbody>
            {members.map((member) => (
              <tr
                key={member.id}
                className="border-b border-[rgb(var(--border))] hover:bg-[rgb(var(--surface))] transition-colors"
              >
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <div className="bg-[rgb(var(--accent))] text-white text-[12px] font-bold flex items-center justify-center w-8 h-8 rounded-full">
                      {(member.name || 'U')[0].toUpperCase()}
                    </div>
                    <div className="flex flex-col gap-0.5">
                      <p className="text-[14px] font-medium text-[rgb(var(--text-main))]">
                        {member.name}
                      </p>
                      <p className="text-[12px] text-[rgb(var(--text-sub))]">
                        {member.email}
                      </p>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <select
                    value={member.role}
                    onChange={(e) => onUpdateRole(member.userId, e.target.value)}
                    disabled={member.userId === currentUserId || isLoading}
                    className="px-2 py-1 rounded text-[13px] border border-[rgb(var(--border))] bg-[rgb(var(--card))] text-[rgb(var(--text-main))] focus:outline-none focus:ring-1 focus:ring-[rgb(var(--accent))]"
                  >
                    {roles.map((role) => (
                      <option key={role.id} value={role.id}>
                        {role.name}
                      </option>
                    ))}
                  </select>
                </td>
                <td className="px-6 py-4">
                  <p className="text-[13px] text-[rgb(var(--text-sub))]">
                    {new Date(member.joinedAt).toLocaleDateString()}
                  </p>
                </td>
                <td className="px-6 py-4">
                  <span
                    className={`inline-block px-2 py-1 rounded text-[11px] font-medium ${
                      member.status === 'active'
                        ? 'bg-[#22C55E]/10 text-[#22C55E]'
                        : member.status === 'pending'
                          ? 'bg-[#F59E0B]/10 text-[#F59E0B]'
                          : 'bg-[#6B7280]/10 text-[#6B7280]'
                    }`}
                  >
                    {member.status.charAt(0).toUpperCase() + member.status.slice(1)}
                  </span>
                </td>
                <td className="px-6 py-4 text-right">
                  {member.userId !== currentUserId && (
                    <Button
                      variant="ghost"
                      onClick={() => handleRemove(member)}
                      disabled={isLoading}
                      className="h-8 w-8 text-[#EF4444] hover:bg-[#EF4444]/10"
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
