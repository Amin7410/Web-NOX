'use client';

import { 
  Plus, 
  Search, 
  Filter, 
  ArrowUpDown, 
  Grid as GridIcon, 
  List as ListIcon, 
  Calendar, 
  User as UserIcon, 
  ChevronLeft, 
  ChevronRight,
  Loader2,
  Inbox
} from "lucide-react";
import { useEffect, useState } from "react";
import { Button } from "../../../ui/button";
import { Input } from "../../../ui/input";
import { Badge } from "../../../ui/badge";
import { useRouter } from "next/navigation";
import { mockStore } from "@/lib/mock-store";

export function ProjectsManagement() {
  const router = useRouter();
  const [projects, setProjects] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProjects = async () => {
      try {
        setLoading(true);
        const res = await fetch("/api/projects");
        if (res.status === 401) {
          setError("Session expired. Please login again.");
          setLoading(false);
          return;
        }
        const responseData = await res.json();
        const projectList = responseData.data?.content || responseData.data || [];
        
        if (projectList.length === 0) {
          setProjects(mockStore.getProjects());
        } else {
          setProjects(projectList);
        }
      } catch (err) {
        console.error("Failed to fetch projects", err);
        setProjects(mockStore.getProjects());
      } finally {
        setLoading(false);
      }
    };
    fetchProjects();
  }, []);

  const filteredProjects = Array.isArray(projects) ? projects.filter(p => 
    p.name.toLowerCase().includes(search.toLowerCase())
  ) : [];

  return (
    <div className="flex flex-col w-full">
      {/* Header Section */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex flex-col gap-1">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 tracking-tight">
            Projects Management
          </h1>
          <p className="text-sm text-gray-500">
            Manage and organize your projects
          </p>
        </div>
        <Button
          onClick={() => router.push("/projects/new")}
          className="bg-[#4F46E5] hover:bg-[#4338CA] text-white rounded-lg px-4 py-2 h-auto shrink-0 shadow-sm transition-all"
        >
          <Plus className="h-4 w-4 mr-2" />
          Create Project
        </Button>
      </div>

      {/* Statistics Section */}
      <div className="flex flex-wrap items-center gap-3 mt-8">
        <div className="px-4 py-2 bg-white rounded-xl border border-gray-100 shadow-sm flex items-center gap-2 hover:border-[#4F46E5]/30 transition-colors">
          <span className="text-lg font-bold text-[#4F46E5]">{projects.length}</span>
          <span className="text-xs font-bold text-gray-500 uppercase tracking-wider">Projects</span>
        </div>
        <div className="px-4 py-2 bg-white rounded-xl border border-gray-100 shadow-sm flex items-center gap-2 hover:border-emerald-500/30 transition-colors">
          <div className="h-2 w-2 rounded-full bg-emerald-500" />
          <span className="text-lg font-bold text-gray-900">
            {projects.filter(p => p.status === 'Active').length}
          </span>
          <span className="text-xs font-bold text-gray-500 uppercase tracking-wider">Active</span>
        </div>
        <div className="px-4 py-2 bg-white rounded-xl border border-gray-100 shadow-sm flex items-center gap-2 hover:border-amber-500/30 transition-colors">
          <div className="h-2 w-2 rounded-full bg-amber-500" />
          <span className="text-lg font-bold text-gray-900">
            {projects.filter(p => p.status !== 'Active').length}
          </span>
          <span className="text-xs font-bold text-gray-500 uppercase tracking-wider">Other</span>
        </div>
      </div>

      {/* Control Bar */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white p-4 rounded-xl border border-gray-200 shadow-sm mt-6">
        <div className="flex flex-col sm:flex-row items-center gap-4 w-full lg:w-auto">
          <div className="relative w-full sm:w-80">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
            <Input
              placeholder="Search projects..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-9 w-full bg-gray-50 border-gray-200 focus-visible:ring-[#4F46E5] focus-visible:border-[#4F46E5] h-9"
            />
          </div>
          <div className="flex items-center gap-2 w-full sm:w-auto">
            <Button
              variant="outline"
              size="sm"
              className="w-full sm:w-auto border-gray-200 text-gray-600 bg-white hover:bg-gray-50 h-9 font-semibold"
            >
              <Filter className="h-4 w-4 mr-2" />
              Filter
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="w-full sm:w-auto border-gray-200 text-gray-600 bg-white hover:bg-gray-50 h-9 font-semibold"
            >
              <ArrowUpDown className="h-4 w-4 mr-2" />
              Sort
            </Button>
          </div>
        </div>
        <div className="flex items-center gap-1 bg-gray-100 p-1 rounded-lg self-end lg:self-auto">
          <Button variant="ghost" size="icon" className="h-7 w-7 bg-white shadow-sm text-gray-900 rounded-md">
            <GridIcon className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" className="h-7 w-7 text-gray-500 hover:text-gray-900 rounded-md">
            <ListIcon className="h-4 w-4" />
          </Button>
        </div>
      </div>

      {/* Project Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mt-8">
        {loading ? (
          <div className="col-span-full py-20 flex justify-center items-center">
            <Loader2 className="h-8 w-8 text-[#4F46E5] animate-spin" />
          </div>
        ) : filteredProjects.length === 0 ? (
          <div className="col-span-full py-20 flex flex-col items-center justify-center text-gray-500 bg-gray-50/50 border border-dashed border-gray-200 rounded-2xl">
            <Inbox className="h-10 w-10 text-gray-300 mb-2" />
            <p className="font-medium">No projects found.</p>
            <Button variant="ghost" size="sm" className="mt-2 text-[#4F46E5]" onClick={() => router.push("/projects/new")}>
              Create your first project
            </Button>
          </div>
        ) : (
          filteredProjects.map((project: any) => (
            <div
              key={project.id}
              onClick={() => router.push(`/projects/${project.id}`)}
              className="cursor-pointer bg-white rounded-xl border border-gray-200 p-5 shadow-[0_1px_2px_rgba(0,0,0,0.02)] hover:shadow-md hover:border-[#4F46E5]/30 hover:-translate-y-0.5 transition-all duration-200 flex flex-col gap-4 group"
            >
              <div className="flex flex-col gap-3">
                <div className="flex items-start justify-between gap-2">
                  <h3 className="font-semibold text-gray-900 text-lg leading-tight group-hover:text-[#4F46E5] transition-colors line-clamp-2">
                    {project.name}
                  </h3>
                  <Badge
                    variant="secondary"
                    className={`shrink-0 border-0 ${
                      project.status === "Active"
                        ? "bg-[#22C55E]/10 text-[#22C55E]"
                        : project.status === "Archived"
                          ? "bg-[#EF4444]/10 text-[#EF4444]"
                          : "bg-gray-100 text-gray-600"
                    }`}
                  >
                    {project.status}
                  </Badge>
                </div>
              </div>

              <div className="flex flex-col gap-2 mt-2">
                <div className="flex items-center gap-2 text-sm text-gray-500">
                  <Calendar className="h-4 w-4 text-gray-400" />
                  <span>Created {project.createdAt ? new Date(project.createdAt).toLocaleDateString() : "—"}</span>
                </div>
                <div className="flex items-center gap-2 text-sm text-gray-500">
                  <UserIcon className="h-4 w-4 text-gray-400" />
                  <span className="truncate">@{project.organizationName || "Unknown Org"}</span>
                </div>
              </div>

              <div className="flex items-center gap-2 mt-auto pt-4 border-t border-gray-100">
                <Button variant="outline" onClick={(e) => { e.stopPropagation(); router.push(`/projects/${project.id}`); }} className="flex-1 bg-white border-gray-200 text-gray-700 hover:bg-gray-50 text-sm h-9 font-medium">
                  View
                </Button>
                <Button variant="outline" onClick={(e) => { e.stopPropagation(); }} className="flex-1 bg-white border-red-100 text-[#EF4444] hover:bg-red-50 text-sm h-9 font-medium">
                  Delete
                </Button>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-center gap-2 mt-10 mb-4">
        <Button variant="outline" size="icon" className="h-8 w-8 border-gray-200 text-gray-500 rounded-lg" disabled>
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <Button variant="ghost" className="h-8 w-8 p-0 bg-[#4F46E5] text-white rounded-lg font-medium">1</Button>
        <Button variant="ghost" className="h-8 w-8 p-0 text-gray-600 hover:bg-gray-100 mx-1">2</Button>
        <Button variant="ghost" className="h-8 w-8 p-0 text-gray-600 hover:bg-gray-100">3</Button>
        <Button variant="outline" size="icon" className="h-8 w-8 border-gray-200 text-gray-500 rounded-lg">
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}
