'use client';

import { useParams } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, UserPlus, Mail, MoreHorizontal, Trash2 } from 'lucide-react';
import { Button } from '../../../ui/button';
import { Badge } from '../../../ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '../../../ui/avatar';
import { useState, useEffect } from 'react';

interface TeamMember {
  id: string;
  name: string;
  email: string;
  avatar?: string;
  role: string;
  status: 'Active' | 'Inactive' | 'Pending';
}

export function TeamView() {
  const params = useParams();
  const projectId = params.projectId as string;
  const [members, setMembers] = useState<TeamMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showInviteModal, setShowInviteModal] = useState(false);
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState('developer');
  const [inviting, setInviting] = useState(false);
  const [removingId, setRemovingId] = useState<string | null>(null);

  useEffect(() => {
    const fetchTeamMembers = async () => {
      try {
        const response = await fetch(`/api/v1/projects/${projectId}/members`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        });

        if (response.ok) {
          const data = await response.json();
          setMembers(data.data || []);
        } else {
          setError('Failed to load team members');
        }
      } catch (err) {
        console.error('Failed to fetch team members:', err);
        setError('An error occurred while loading team members');
      } finally {
        setLoading(false);
      }
    };

    fetchTeamMembers();
  }, [projectId]);

  const handleInviteMember = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!inviteEmail) {
      setError('Please enter an email');
      return;
    }

    setInviting(true);
    setError(null);

    try {
      const response = await fetch(`/api/v1/projects/${projectId}/members`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: inviteEmail,
          role: inviteRole,
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to invite member');
      }

      const data = await response.json();
      setMembers([...members, data.data]);
      setInviteEmail('');
      setInviteRole('developer');
      setShowInviteModal(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setInviting(false);
    }
  };

  const handleRemoveMember = async (memberId: string) => {
    if (!confirm('Are you sure you want to remove this member?')) {
      return;
    }

    setRemovingId(memberId);
    setError(null);

    try {
      const response = await fetch(`/api/v1/projects/${projectId}/members/${memberId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Failed to remove member');
      }

      setMembers(members.filter(m => m.id !== memberId));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setRemovingId(null);
    }
  };

  return (
    <div className="flex flex-col w-full max-w-5xl mx-auto pb-16 p-6">
      {/* Top Navigation Bar */}
      <div className="flex items-center justify-between mb-8">
        <Link 
          href={`/projects/${projectId}`}
          className="inline-flex items-center text-sm font-medium text-gray-500 hover:text-gray-900 transition-colors"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Project
        </Link>
        <Button 
          onClick={() => setShowInviteModal(true)}
          className="bg-[#4F46E5] hover:bg-[#4338CA] text-white shadow-sm font-medium"
        >
          <UserPlus className="h-4 w-4 mr-2" />
          Invite Member
        </Button>
      </div>

      {/* Header */}
      <div className="flex flex-col gap-4 mb-8">
        <h1 className="text-3xl font-semibold tracking-tight text-gray-900">
          Team Management
        </h1>
        <p className="text-gray-600">Manage project members and their permissions.</p>
      </div>

      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
          {error}
        </div>
      )}

      {/* Invite Modal */}
      {showInviteModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-sm w-full shadow-lg">
            <h2 className="text-lg font-semibold mb-4 text-gray-900">Invite Member</h2>
            <form onSubmit={handleInviteMember} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input
                  type="email"
                  value={inviteEmail}
                  onChange={(e) => setInviteEmail(e.target.value)}
                  placeholder="member@example.com"
                  disabled={inviting}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4F46E5]"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Role</label>
                <select
                  value={inviteRole}
                  onChange={(e) => setInviteRole(e.target.value)}
                  disabled={inviting}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4F46E5]"
                >
                  <option value="developer">Developer</option>
                  <option value="maintainer">Maintainer</option>
                  <option value="admin">Admin</option>
                </select>
              </div>
              <div className="flex gap-2 justify-end">
                <Button variant="outline" onClick={() => setShowInviteModal(false)} disabled={inviting}>
                  Cancel
                </Button>
                <Button 
                  onClick={handleInviteMember}
                  disabled={inviting || !inviteEmail}
                  className="bg-[#4F46E5] hover:bg-[#4338CA] text-white"
                >
                  {inviting ? 'Inviting...' : 'Invite'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Team List */}
      {loading ? (
        <div className="text-gray-500">Loading team members...</div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200">
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-medium text-gray-900">Team Members</h2>
              <span className="text-sm text-gray-500">{members.length} members</span>
            </div>
          </div>
          
          {members.length === 0 ? (
            <div className="p-6 text-center text-gray-500">
              No team members yet. Invite someone to get started.
            </div>
          ) : (
            <div className="divide-y divide-gray-200">
              {members.map((member) => (
                <div key={member.id} className="p-6 flex items-center justify-between hover:bg-gray-50 transition-colors">
                  <div className="flex items-center gap-4">
                    <Avatar className="h-10 w-10">
                      <AvatarImage src={member.avatar} alt={member.name} />
                      <AvatarFallback className="bg-[#4F46E5] text-white">
                        {member.name.split(' ').map(n => n[0]).join('')}
                      </AvatarFallback>
                    </Avatar>
                    
                    <div className="flex flex-col gap-1">
                      <div className="flex items-center gap-2">
                        <span className="font-medium text-gray-900">{member.name}</span>
                        <Badge 
                          variant="secondary" 
                          className={`px-2 py-0.5 text-xs font-medium rounded-md ${
                            member.status === 'Active' ? 'bg-[#22C55E]/10 text-[#16a34a]' :
                            member.status === 'Pending' ? 'bg-amber-100 text-amber-700' :
                            'bg-gray-100 text-gray-600'
                          }`}
                        >
                          {member.status}
                        </Badge>
                      </div>
                      <div className="flex items-center gap-4 text-sm text-gray-500">
                        <span>{member.email}</span>
                        <span>•</span>
                        <span className="capitalize">{member.role}</span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-2">
                    <Button 
                      variant="ghost" 
                      size="sm" 
                      className="h-8 w-8 p-0 text-gray-400 hover:text-gray-900"
                      onClick={() => handleRemoveMember(member.id)}
                      disabled={removingId === member.id}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
