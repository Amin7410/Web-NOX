'use client';

import { useParams } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, UserPlus, Mail, MoreHorizontal } from 'lucide-react';
import { Button } from '../../../../ui/button';
import { Badge } from '../../../../ui/badge';
import { Avatar, AvatarFallback, AvatarImage } from '../../../../ui/avatar';

const MOCK_TEAM = [
  { id: "1", name: "Sarah Jenkins", email: "sarah@example.com", role: "Owner", status: "Active", avatar: "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=32&h=32&fit=crop&crop=face" },
  { id: "2", name: "Mike Ross", email: "mike@example.com", role: "Developer", status: "Active", avatar: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=32&h=32&fit=crop&crop=face" },
  { id: "3", name: "Anna Smith", email: "anna@example.com", role: "Designer", status: "Active", avatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=32&h=32&fit=crop&crop=face" },
  { id: "4", name: "David Chen", email: "david@example.com", role: "Developer", status: "Inactive", avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=32&h=32&fit=crop&crop=face" },
  { id: "5", name: "Emma Wilson", email: "emma@example.com", role: "Project Manager", status: "Active", avatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=32&h=32&fit=crop&crop=face" }
];

export default function TeamManagementPage() {
  const params = useParams();
  const projectId = params.projectId as string;

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
        <Button className="bg-[#4F46E5] hover:bg-[#4338CA] text-white shadow-sm font-medium">
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

      {/* Team List */}
      <div className="bg-white rounded-xl border border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-medium text-gray-900">Team Members</h2>
            <span className="text-sm text-gray-500">5 members</span>
          </div>
        </div>
        
        <div className="divide-y divide-gray-200">
          {MOCK_TEAM.map((member) => (
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
                        'bg-gray-100 text-gray-600'
                      }`}
                    >
                      {member.status}
                    </Badge>
                  </div>
                  <div className="flex items-center gap-4 text-sm text-gray-500">
                    <span>{member.email}</span>
                    <span>•</span>
                    <span>{member.role}</span>
                  </div>
                </div>
              </div>
              
              <div className="flex items-center gap-2">
                <Button variant="ghost" size="sm" className="h-8 w-8 p-0 text-gray-400 hover:text-gray-900">
                  <Mail className="h-4 w-4" />
                </Button>
                <Button variant="ghost" size="sm" className="h-8 w-8 p-0 text-gray-400 hover:text-gray-900">
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
