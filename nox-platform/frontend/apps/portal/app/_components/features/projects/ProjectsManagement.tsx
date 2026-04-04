'use client';

import {
  Plus,
  Search,
  Filter,
  ArrowUpDown,
  Calendar,
  User,
  Grid as GridIcon,
  List as ListIcon,
} from "lucide-react";
import { Button, Input } from "@nox/ui";
import { useRouter } from "next/navigation";
import { useState } from "react";

interface Project {
  id: string;
  name: string;
  description?: string;
  status: 'Active' | 'Archived' | 'Draft';
  createdAt: string;
  owner?: string;
}

const MOCK_PROJECTS: Project[] = [
  {
    id: '1',
    name: 'Website Redesign',
    description: 'Complete redesign of the main product website',
    status: 'Active',
    createdAt: new Date('2024-01-15').toISOString(),
    owner: 'John Doe',
  },
  {
    id: '2',
    name: 'Mobile App',
    description: 'Native iOS and Android application',
    status: 'Active',
    createdAt: new Date('2024-02-01').toISOString(),
    owner: 'Jane Smith',
  },
  {
    id: '3',
    name: 'API Backend',
    description: 'RESTful API backend services',
    status: 'Active',
    createdAt: new Date('2024-02-10').toISOString(),
    owner: 'Bob Johnson',
  },
  {
    id: '4',
    name: 'Dashboard',
    description: 'Analytics and reporting dashboard',
    status: 'Active',
    createdAt: new Date('2024-03-01').toISOString(),
    owner: 'Alice Brown',
  },
  {
    id: '5',
    name: 'Legacy System',
    description: 'Old system for archival purposes',
    status: 'Archived',
    createdAt: new Date('2023-06-15').toISOString(),
    owner: 'Charlie Wilson',
  },
  {
    id: '6',
    name: 'Team Collaboration Tool',
    description: 'Internal tool for team communication',
    status: 'Draft',
    createdAt: new Date('2024-03-20').toISOString(),
    owner: 'Diana Prince',
  },
];

export function ProjectsManagement() {
  const router = useRouter();
  const [projects] = useState<Project[]>(MOCK_PROJECTS);
  const [searchTerm, setSearchTerm] = useState('');
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const handleDeleteProject = (projectId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    
    if (!confirm('Are you sure you want to delete this project?')) {
      return;
    }

    setDeletingId(projectId);
    setTimeout(() => {
      setDeletingId(null);
    }, 1000);
  };

  const filteredProjects = projects.filter(p =>
    p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.description?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const activeCount = projects.filter(p => p.status === 'Active').length;
  const archivedCount = projects.filter(p => p.status === 'Archived').length;

  return (
    <div className="flex flex-col w-full gap-6">
      {/* Header Section */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 px-6 pt-6">
        <div className="flex flex-col gap-1">
          <h1 className="text-2xl font-bold text-[rgb(var(--text-main))]">
            Projects
          </h1>
          <p className="text-sm text-[rgb(var(--text-sub))]">
            Manage and organize your projects
          </p>
        </div>
        <Button
          onClick={() => router.push("/projects/new")}
          className="bg-[rgb(var(--accent))] hover:bg-[rgb(var(--accent))]/90 text-[rgb(var(--accent-foreground))]"
        >
          <Plus className="h-4 w-4 mr-2" />
          New Project
        </Button>
      </div>

      {/* Stats Bar */}
      <div className="px-6 flex flex-wrap gap-3">
        <div className="bg-[rgb(var(--surface))] px-4 py-2 rounded-lg border border-[rgb(var(--border))]">
          <span className="text-sm font-medium text-[rgb(var(--text-main))]">
            {projects.length} Total
          </span>
        </div>
        <div className="bg-[rgb(var(--surface))] px-4 py-2 rounded-lg border border-[rgb(var(--border))]">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-green-500"></div>
            <span className="text-sm font-medium text-[rgb(var(--text-main))]">
              {activeCount} Active
            </span>
          </div>
        </div>
        <div className="bg-[rgb(var(--surface))] px-4 py-2 rounded-lg border border-[rgb(var(--border))]">
          <div className="flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-red-500"></div>
            <span className="text-sm font-medium text-[rgb(var(--text-main))]">
              {archivedCount} Archived
            </span>
          </div>
        </div>
      </div>

      {/* Search and Filter Bar */}
      <div className="px-6 flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[rgb(var(--muted-foreground))]" />
          <Input
            placeholder="Search projects..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <div className="flex gap-2">
          <Button variant="outline" size="sm">
            <Filter className="h-4 w-4 mr-2" />
            Filter
          </Button>
          <Button variant="outline" size="sm">
            <ArrowUpDown className="h-4 w-4 mr-2" />
            Sort
          </Button>
          <div className="flex items-center gap-1 bg-[rgb(var(--surface))] p-1 rounded-lg border border-[rgb(var(--border))]">
            <Button variant="ghost" className="h-8 w-8 p-0">
              <GridIcon className="h-4 w-4" />
            </Button>
            <Button variant="ghost" className="h-8 w-8 p-0">
              <ListIcon className="h-4 w-4" />
            </Button>
          </div>
        </div>
      </div>

      {/* Projects Grid */}
      <div className="px-6 pb-6">
        {filteredProjects.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-[rgb(var(--text-sub))]">
              {searchTerm ? 'No projects found matching your search' : 'No projects yet'}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredProjects.map((project) => (
              <div
                key={project.id}
                onClick={() => router.push(`/projects/${project.id}`)}
                className="cursor-pointer bg-[rgb(var(--card))] rounded-lg border border-[rgb(var(--border))] hover:border-[rgb(var(--accent))] transition-all duration-200 overflow-hidden group"
              >
                {/* Project Header */}
                <div className="h-24 bg-gradient-to-br from-[rgb(var(--accent))]/20 to-[rgb(var(--accent))]/5 flex items-center justify-center border-b border-[rgb(var(--border))]">
                  <div className="text-[rgb(var(--accent))]/40 group-hover:text-[rgb(var(--accent))]/60 transition-colors">
                    <GridIcon className="h-8 w-8" />
                  </div>
                </div>

                {/* Project Content */}
                <div className="p-4 flex flex-col gap-3">
                  <div className="flex justify-between items-start gap-2">
                    <h3 className="font-semibold text-[rgb(var(--text-main))] line-clamp-1 group-hover:text-[rgb(var(--accent))] transition-colors">
                      {project.name}
                    </h3>
                    <span
                      className={`text-xs px-2 py-1 rounded whitespace-nowrap shrink-0 ${
                        project.status === 'Active'
                          ? 'bg-green-100 text-green-700'
                          : project.status === 'Archived'
                          ? 'bg-red-100 text-red-700'
                          : 'bg-gray-100 text-gray-700'
                      }`}
                    >
                      {project.status}
                    </span>
                  </div>

                  {project.description && (
                    <p className="text-sm text-[rgb(var(--text-sub))] line-clamp-2">
                      {project.description}
                    </p>
                  )}

                  <div className="flex flex-col gap-2 text-xs text-[rgb(var(--text-sub))]">
                    <div className="flex items-center gap-2">
                      <Calendar className="h-3 w-3" />
                      <span>
                        {new Date(project.createdAt).toLocaleDateString()}
                      </span>
                    </div>
                    {project.owner && (
                      <div className="flex items-center gap-2">
                        <User className="h-3 w-3" />
                        <span>{project.owner}</span>
                      </div>
                    )}
                  </div>

                  {/* Action Buttons */}
                  <div className="flex gap-2 mt-2 pt-3 border-t border-[rgb(var(--border))]">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={(e) => {
                        e.stopPropagation();
                        router.push(`/projects/${project.id}`);
                      }}
                      className="flex-1 text-xs"
                    >
                      View
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={(e) => handleDeleteProject(project.id, e)}
                      disabled={deletingId === project.id}
                      className="flex-1 text-xs text-red-600 hover:text-red-700"
                    >
                      {deletingId === project.id ? '...' : 'Delete'}
                    </Button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
