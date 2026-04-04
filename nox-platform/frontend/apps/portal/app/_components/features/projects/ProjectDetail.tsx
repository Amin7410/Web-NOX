'use client';

import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { 
  ArrowLeft, Settings, Trash2, Calendar, User, 
  CheckCircle2, Clock, Users, Tag, 
  Edit2, Share, Download, Plus, Mail, Upload,
  MoreHorizontal
} from 'lucide-react';
import { Button } from '../../../ui/button';
import { Badge } from '../../../ui/badge';
import { useState, useEffect } from 'react';

interface ProjectData {
  id: string;
  name: string;
  description: string;
  status: 'Active' | 'Archived' | 'Draft';
  owner?: string;
  createdAt: string;
  stats?: {
    tasksCompleted: number;
    tasksTotal: number;
    membersActive: number;
    lastUpdated: string;
  };
  tags?: string[];
}

export function ProjectDetail() {
  const params = useParams();
  const router = useRouter();
  const projectId = params.projectId as string;
  
  const [project, setProject] = useState<ProjectData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    const fetchProject = async () => {
      try {
        const response = await fetch(`/api/v1/projects/${projectId}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        });

        if (response.ok) {
          const data = await response.json();
          setProject(data.data || null);
        } else {
          setError('Failed to load project');
        }
      } catch (err) {
        console.error('Failed to fetch project:', err);
        setError('An error occurred while loading project');
      } finally {
        setLoading(false);
      }
    };

    fetchProject();
  }, [projectId]);

  const handleDeleteProject = async () => {
    if (!confirm('Are you sure you want to delete this project? This action cannot be undone.')) {
      return;
    }

    setDeleting(true);

    try {
      const response = await fetch(`/api/v1/projects/${projectId}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error('Failed to delete project');
      }

      router.push('/projects');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
      setDeleting(false);
    }
  };

  if (loading) {
    return <div className="text-gray-500 py-12">Loading project...</div>;
  }

  if (!project) {
    return <div className="text-red-600 py-12">Project not found</div>;
  }

  return (
    <div className="flex flex-col w-full max-w-5xl mx-auto pb-16">
      {/* Top Navigation Bar */}
      <div className="flex items-center justify-between mb-8">
        <Link 
          href="/projects" 
          className="inline-flex items-center text-sm font-medium text-gray-500 hover:text-gray-900 transition-colors"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Projects
        </Link>
        <div className="flex items-center gap-2">
          <Button 
            onClick={() => router.push(`/projects/${projectId}/settings`)}
            variant="outline" 
            size="sm" 
            className="h-8 border-gray-200 text-gray-700 bg-white hover:bg-gray-50"
          >
            <Settings className="h-4 w-4 mr-2" />
            Settings
          </Button>
          <Button 
            onClick={handleDeleteProject}
            disabled={deleting}
            variant="outline" 
            size="sm" 
            className="h-8 border-red-200 text-[#EF4444] bg-red-50 hover:bg-red-100 hover:border-red-300 transition-colors"
          >
            <Trash2 className="h-4 w-4 mr-2" />
            {deleting ? 'Deleting...' : 'Delete'}
          </Button>
        </div>
      </div>

      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
          {error}
        </div>
      )}

      {/* Header Info */}
      <div className="flex flex-col gap-4 mb-10">
        <h1 className="text-3xl font-semibold tracking-tight text-gray-900">
          {project.name}
        </h1>
        <div className="flex flex-wrap items-center gap-4 text-sm">
          <Badge 
            variant="secondary" 
            className={`px-2.5 py-0.5 border-0 font-medium rounded-md ${
              project.status === 'Active' ? 'bg-[#22C55E]/10 text-[#16a34a]' :
              project.status === 'Archived' ? 'bg-[#EF4444]/10 text-[#dc2626]' :
              'bg-gray-100 text-gray-600'
            }`}
          >
            <span className={`w-1.5 h-1.5 rounded-full mr-1.5 ${
              project.status === 'Active' ? 'bg-[#22C55E]' :
              project.status === 'Archived' ? 'bg-[#EF4444]' :
              'bg-gray-400'
            }`}></span>
            {project.status}
          </Badge>
          {project.owner && (
            <div className="flex items-center gap-1.5 text-gray-500">
              <User className="h-4 w-4 text-gray-400" />
              <span>{project.owner}</span>
            </div>
          )}
          <div className="flex items-center gap-1.5 text-gray-500">
            <Calendar className="h-4 w-4 text-gray-400" />
            <span>Created {new Date(project.createdAt).toLocaleDateString()}</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">
        {/* Main Content Area (Left Column) */}
        <div className="lg:col-span-2 flex flex-col gap-8">
          
          {/* Project Overview */}
          <section className="flex flex-col">
            <div className="flex items-center border-b border-gray-200 pb-3 mb-6">
              <h2 className="text-lg font-medium text-gray-900">Project Overview</h2>
            </div>
            
            <div className="flex flex-col gap-8">
              {/* Description */}
              <div className="flex flex-col gap-2">
                <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider">Description</h3>
                <p className="text-[15px] leading-relaxed text-gray-600">
                  {project.description || 'No description provided'}
                </p>
              </div>

              {/* Action Buttons Section */}
              <div className="flex flex-col gap-4 mt-4">
                <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider">Primary Actions</h3>
                <div className="flex flex-wrap items-center gap-3">
                  <Button 
                    onClick={() => router.push(`/projects/${projectId}/settings`)}
                    className="bg-[#4F46E5] hover:bg-[#4338CA] text-white shadow-sm font-medium"
                  >
                    <Edit2 className="h-4 w-4 mr-2" />
                    Edit Project
                  </Button>
                  <Button 
                    onClick={() => router.push(`/projects/${projectId}/team`)}
                    variant="outline" 
                    className="border-gray-200 text-gray-700 bg-white hover:bg-gray-50 font-medium"
                  >
                    <Users className="h-4 w-4 mr-2" />
                    Manage Team
                  </Button>
                </div>

                <div className="flex flex-wrap items-center gap-3 mt-2">
                  <Button variant="ghost" size="sm" className="text-gray-600 hover:text-gray-900 hover:bg-gray-100 font-medium h-9">
                    <Share className="h-4 w-4 mr-2" />
                    Share
                  </Button>
                  <Button variant="ghost" size="sm" className="text-gray-600 hover:text-gray-900 hover:bg-gray-100 font-medium h-9">
                    <Download className="h-4 w-4 mr-2" />
                    Export
                  </Button>
                </div>
              </div>
            </div>
          </section>
        </div>

        {/* Sidebar (Right Column) */}
        <div className="lg:col-span-1 flex flex-col gap-6">
          
          {/* Statistics Card */}
          {project.stats && (
            <div className="bg-white rounded-xl border border-gray-200 shadow-[0_1px_2px_rgba(0,0,0,0.02)] p-5 flex flex-col gap-5">
              <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider">Statistics</h3>
              
              <div className="flex flex-col gap-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2.5 text-gray-600">
                    <CheckCircle2 className="h-[18px] w-[18px] text-emerald-500" />
                    <span className="text-sm">Tasks</span>
                  </div>
                  <span className="text-sm font-medium text-gray-900">
                    {project.stats.tasksCompleted} / {project.stats.tasksTotal}
                  </span>
                </div>
                
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2.5 text-gray-600">
                    <Users className="h-[18px] w-[18px] text-blue-500" />
                    <span className="text-sm">Members</span>
                  </div>
                  <span className="text-sm font-medium text-gray-900">
                    {project.stats.membersActive} active
                  </span>
                </div>
                
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2.5 text-gray-600">
                    <Clock className="h-[18px] w-[18px] text-amber-500" />
                    <span className="text-sm">Last updated</span>
                  </div>
                  <span className="text-sm font-medium text-gray-900">
                    {project.stats.lastUpdated}
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* Tags */}
          {project.tags && project.tags.length > 0 && (
            <div className="bg-white rounded-xl border border-gray-200 shadow-[0_1px_2px_rgba(0,0,0,0.02)] p-5 flex flex-col gap-4">
              <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider">Tags</h3>
              <div className="flex flex-wrap gap-2">
                {project.tags.map(tag => (
                  <Badge 
                    key={tag} 
                    variant="secondary" 
                    className="bg-gray-100 text-gray-700 hover:bg-gray-200 font-medium px-2.5 py-1 rounded-md border-0 transition-colors cursor-pointer"
                  >
                    <Tag className="h-3 w-3 mr-1.5 text-gray-500" />
                    {tag}
                  </Badge>
                ))}
              </div>
            </div>
          )}

          {/* Quick Actions */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-[0_1px_2px_rgba(0,0,0,0.02)] p-5 flex flex-col gap-3">
            <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-1">Quick Actions</h3>
            <Button variant="ghost" className="w-full justify-start text-gray-600 hover:text-gray-900 hover:bg-gray-50 font-medium px-3">
              <Plus className="h-4 w-4 mr-3 text-gray-400" />
              Add Task
            </Button>
            <Button 
              onClick={() => router.push(`/projects/${projectId}/team`)}
              variant="ghost" 
              className="w-full justify-start text-gray-600 hover:text-gray-900 hover:bg-gray-50 font-medium px-3"
            >
              <Mail className="h-4 w-4 mr-3 text-gray-400" />
              Invite Member
            </Button>
            <Button variant="ghost" className="w-full justify-start text-gray-600 hover:text-gray-900 hover:bg-gray-50 font-medium px-3">
              <Upload className="h-4 w-4 mr-3 text-gray-400" />
              Upload Files
            </Button>
          </div>

        </div>
      </div>
    </div>
  );
}

  return (
    <div className="flex flex-col w-full max-w-5xl mx-auto pb-16">
      {/* Top Navigation Bar */}
      <div className="flex items-center justify-between mb-8">
        <Link 
          href="/projects" 
          className="inline-flex items-center text-sm font-medium text-gray-500 hover:text-gray-900 transition-colors"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Projects
        </Link>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" className="h-8 border-gray-200 text-gray-700 bg-white hover:bg-gray-50">
            <Settings className="h-4 w-4 mr-2" />
            Settings
          </Button>
          <Button variant="outline" size="sm" className="h-8 border-red-200 text-[#EF4444] bg-red-50 hover:bg-red-100 hover:border-red-300 transition-colors">
            <Trash2 className="h-4 w-4 mr-2" />
            Delete
          </Button>
        </div>
      </div>

      {/* Header Info */}
      <div className="flex flex-col gap-4 mb-10">
        <h1 className="text-3xl font-semibold tracking-tight text-gray-900">
          {project.name}
        </h1>
        <div className="flex flex-wrap items-center gap-4 text-sm">
          <Badge 
            variant="secondary" 
            className={`px-2.5 py-0.5 border-0 font-medium rounded-md ${
              project.status === 'Active' ? 'bg-[#22C55E]/10 text-[#16a34a]' :
              project.status === 'Archived' ? 'bg-[#EF4444]/10 text-[#dc2626]' :
              'bg-gray-100 text-gray-600'
            }`}
          >
            <span className={`w-1.5 h-1.5 rounded-full mr-1.5 ${
              project.status === 'Active' ? 'bg-[#22C55E]' :
              project.status === 'Archived' ? 'bg-[#EF4444]' :
              'bg-gray-400'
            }`}></span>
            {project.status}
          </Badge>
          <div className="flex items-center gap-1.5 text-gray-500">
            <User className="h-4 w-4 text-gray-400" />
            <span>{project.owner}</span>
          </div>
          <div className="flex items-center gap-1.5 text-gray-500">
            <Calendar className="h-4 w-4 text-gray-400" />
            <span>Created {project.createdAt}</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-10">
        {/* Main Content Area (Left Column) */}
        <div className="lg:col-span-2 flex flex-col gap-8">
          
          {/* Project Overview */}
          <section className="flex flex-col">
            <div className="flex items-center border-b border-gray-200 pb-3 mb-6">
              <h2 className="text-lg font-medium text-gray-900">Project Overview</h2>
            </div>
            
            <div className="flex flex-col gap-8">
              {/* Description */}
              <div className="flex flex-col gap-2">
                <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider">Description</h3>
                <p className="text-[15px] leading-relaxed text-gray-600">
                  {project.description}
                </p>
              </div>

              {/* Action Buttons Section */}
              <div className="flex flex-col gap-4 mt-4">
                <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider">Primary Actions</h3>
                <div className="flex flex-wrap items-center gap-3">
                  <Button 
                    onClick={() => router.push(`/projects/${projectId}/edit`)}
                    className="bg-[#4F46E5] hover:bg-[#4338CA] text-white shadow-sm font-medium"
                  >
                    <Edit2 className="h-4 w-4 mr-2" />
                    Edit Project
                  </Button>
                  <Button 
                    onClick={() => router.push(`/projects/${projectId}/team`)}
                    variant="outline" 
                    className="border-gray-200 text-gray-700 bg-white hover:bg-gray-50 font-medium"
                  >
                    <Users className="h-4 w-4 mr-2" />
                    Manage Team
                  </Button>
                  <Button 
                    onClick={() => router.push(`/projects/${projectId}/analytics`)}
                    variant="outline" 
                    className="border-gray-200 text-gray-700 bg-white hover:bg-gray-50 font-medium"
                  >
                    <MoreHorizontal className="h-4 w-4 mr-2" />
                    View Analytics
                  </Button>
                </div>

                <div className="flex flex-wrap items-center gap-3 mt-2">
                  <Button variant="ghost" size="sm" className="text-gray-600 hover:text-gray-900 hover:bg-gray-100 font-medium h-9">
                    <Share className="h-4 w-4 mr-2" />
                    Share
                  </Button>
                  <Button variant="ghost" size="sm" className="text-gray-600 hover:text-gray-900 hover:bg-gray-100 font-medium h-9">
                    <Download className="h-4 w-4 mr-2" />
                    Export
                  </Button>
                </div>
              </div>
            </div>
          </section>
        </div>

        {/* Sidebar (Right Column) */}
        <div className="lg:col-span-1 flex flex-col gap-6">
          
          {/* Statistics Card */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-[0_1px_2px_rgba(0,0,0,0.02)] p-5 flex flex-col gap-5">
            <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider">Statistics</h3>
            
            <div className="flex flex-col gap-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5 text-gray-600">
                  <CheckCircle2 className="h-[18px] w-[18px] text-emerald-500" />
                  <span className="text-sm">Tasks</span>
                </div>
                <span className="text-sm font-medium text-gray-900">
                  {project.stats.tasksCompleted} / {project.stats.tasksTotal}
                </span>
              </div>
              
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5 text-gray-600">
                  <Users className="h-[18px] w-[18px] text-blue-500" />
                  <span className="text-sm">Members</span>
                </div>
                <span className="text-sm font-medium text-gray-900">
                  {project.stats.membersActive} active
                </span>
              </div>
              
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2.5 text-gray-600">
                  <Clock className="h-[18px] w-[18px] text-amber-500" />
                  <span className="text-sm">Last updated</span>
                </div>
                <span className="text-sm font-medium text-gray-900">
                  {project.stats.lastUpdated}
                </span>
              </div>
            </div>
          </div>

          {/* Tags */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-[0_1px_2px_rgba(0,0,0,0.02)] p-5 flex flex-col gap-4">
            <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider">Tags</h3>
            <div className="flex flex-wrap gap-2">
              {project.tags.map(tag => (
                <Badge 
                  key={tag} 
                  variant="secondary" 
                  className="bg-gray-100 text-gray-700 hover:bg-gray-200 font-medium px-2.5 py-1 rounded-md border-0 transition-colors cursor-pointer"
                >
                  <Tag className="h-3 w-3 mr-1.5 text-gray-500" />
                  {tag}
                </Badge>
              ))}
            </div>
          </div>

          {/* Quick Actions */}
          <div className="bg-white rounded-xl border border-gray-200 shadow-[0_1px_2px_rgba(0,0,0,0.02)] p-5 flex flex-col gap-3">
            <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-1">Quick Actions</h3>
            <Button variant="ghost" className="w-full justify-start text-gray-600 hover:text-gray-900 hover:bg-gray-50 font-medium px-3">
              <Plus className="h-4 w-4 mr-3 text-gray-400" />
              Add Task
            </Button>
            <Button variant="ghost" className="w-full justify-start text-gray-600 hover:text-gray-900 hover:bg-gray-50 font-medium px-3">
              <Mail className="h-4 w-4 mr-3 text-gray-400" />
              Invite Member
            </Button>
            <Button variant="ghost" className="w-full justify-start text-gray-600 hover:text-gray-900 hover:bg-gray-50 font-medium px-3">
              <Upload className="h-4 w-4 mr-3 text-gray-400" />
              Upload Files
            </Button>
          </div>

        </div>
      </div>
    </div>
  );
}
