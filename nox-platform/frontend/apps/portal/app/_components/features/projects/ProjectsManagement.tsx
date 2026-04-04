'use client';

import {
  Plus,
  Search,
  Filter,
  ArrowUpDown,
  Grid as GridIcon,
  List as ListIcon,
  Calendar,
  User,
  ChevronLeft,
  ChevronRight,
  GitBranch,
  Database,
  Layers,
  Lightbulb,
  Activity,
  ArrowRight,
} from "lucide-react";
import { Button } from "../../../ui/button";
import { Input } from "../../../ui/input";
import { Badge } from "../../../ui/badge";
import { useRouter } from "next/navigation";
import { MOCK_PROJECTS } from "./data";

export function ProjectsManagement() {
  const router = useRouter();

  const templates = [
    { id: 1, name: "Flowchart", icon: GitBranch, description: "Visual flow diagram" },
    { id: 2, name: "Database ERD", icon: Database, description: "Entity relationships" },
    { id: 3, name: "System Architecture", icon: Layers, description: "System design" },
    { id: 4, name: "Mindmap", icon: Lightbulb, description: "Brainstorming board" },
  ];

  const activities = [
    { id: 1, action: "Created project", project: "Website Redesign", time: "2 hours ago" },
    { id: 2, action: "Updated design docs", project: "Mobile App", time: "4 hours ago" },
    { id: 3, action: "Added team member", project: "API Backend", time: "1 day ago" },
    { id: 4, action: "Deployed version 1.0", project: "Dashboard", time: "2 days ago" },
  ];

  return (
    <div className="flex flex-col w-full gap-8">
      {/* Header Section */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex flex-col gap-1">
          <h1 className="text-[20px] sm:text-[22px] font-bold text-[rgb(var(--text-main))] tracking-tight leading-[1.25]">
            Projects Management
          </h1>
          <p className="text-[14px] leading-[1.5] text-[rgb(var(--muted-foreground))]">
            Manage and organize your projects
          </p>
        </div>
        <Button
          onClick={() => router.push("/projects/new")}
          className="bg-[rgb(var(--accent))] hover:bg-[rgb(var(--accent))]/90 text-[rgb(var(--accent-foreground))] rounded-lg px-4 py-2 h-auto shrink-0 shadow-sm transition-all"
        >
          <Plus className="h-4 w-4 mr-2" />
          Create Project
        </Button>
      </div>

      {/* Grid Layout: Main Content + Activity Feed */}
      <div className="grid grid-cols-1 lg:grid-cols-[1fr_300px] gap-8">
        <div className="flex flex-col gap-8">
          {/* Quick Templates Section */}
          <div className="flex flex-col gap-4">
            <div className="flex flex-col gap-1">
              <h2 className="text-[16px] font-semibold text-[rgb(var(--text-main))]">Quick Templates</h2>
              <p className="text-[13px] text-[rgb(var(--muted-foreground))]">Start with a template to quickly create a new project</p>
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              {templates.map((template) => {
                const Icon = template.icon;
                return (
                  <button
                    key={template.id}
                    onClick={() => router.push("/projects/new")}
                    className="flex flex-col items-center gap-2 p-4 rounded-lg border border-[rgb(var(--border))] bg-[rgb(var(--card))] hover:border-[rgb(var(--accent))] hover:bg-[rgb(var(--surface))] transition-all duration-200 group"
                  >
                    <div className="h-10 w-10 rounded-lg bg-[rgb(var(--accent))]/10 flex items-center justify-center group-hover:bg-[rgb(var(--accent))]/20">
                      <Icon className="h-5 w-5 text-[rgb(var(--accent))]" />
                    </div>
                    <p className="text-[12px] font-medium text-[rgb(var(--text-main))] text-center line-clamp-2">{template.name}</p>
                    <p className="text-[11px] text-[rgb(var(--muted-foreground))] text-center">{template.description}</p>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Control Bar & Statistics Container */}
          <div className="flex flex-col gap-6">
            {/* Statistics */}
            <div className="flex flex-wrap items-center gap-3">
              <div className="flex items-center gap-2 bg-[rgb(var(--surface))] px-3 py-1.5 rounded-lg border border-[rgb(var(--border))] shadow-sm">
                <span className="text-sm font-medium text-[rgb(var(--text-main))]">
                  12 Projects
                </span>
              </div>
              <div className="flex items-center gap-2 bg-[rgb(var(--surface))] px-3 py-1.5 rounded-lg border border-[rgb(var(--border))] shadow-sm">
                <div className="w-2 h-2 rounded-full bg-[#22C55E]"></div>
                <span className="text-sm font-medium text-[rgb(var(--muted-foreground))]">
                  3 Active
                </span>
              </div>
              <div className="flex items-center gap-2 bg-[rgb(var(--surface))] px-3 py-1.5 rounded-lg border border-[rgb(var(--border))] shadow-sm">
                <div className="w-2 h-2 rounded-full bg-[#EF4444]"></div>
                <span className="text-sm font-medium text-[rgb(var(--muted-foreground))]">
                  2 Archived
                </span>
              </div>
            </div>

            {/* Control Bar */}
            <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-[rgb(var(--surface))] p-4 rounded-xl border border-[rgb(var(--border))] shadow-sm">
              <div className="flex flex-col sm:flex-row items-center gap-3 w-full lg:w-auto">
                <div className="relative w-full sm:w-80">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[rgb(var(--muted-foreground))]" />
                  <Input
                    placeholder="Search projects..."
                    className="pl-9 w-full bg-[rgb(var(--card))] border-[rgb(var(--border))] text-[rgb(var(--text-main))] placeholder:text-[rgb(var(--text-sub))] focus-visible:ring-[rgb(var(--accent))] focus-visible:border-[rgb(var(--accent))] h-9"
                  />
                </div>
                <div className="flex items-center gap-2 w-full sm:w-auto">
                  <Button
                    variant="outline"
                    size="sm"
                    className="w-full sm:w-auto border-[rgb(var(--border))] text-[rgb(var(--text-main))] bg-[rgb(var(--surface))] hover:bg-[rgb(var(--surface))] h-9"
                  >
                    <Filter className="h-4 w-4 mr-2" />
                    Filter
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    className="w-full sm:w-auto border-[rgb(var(--border))] text-[rgb(var(--text-main))] bg-[rgb(var(--surface))] hover:bg-[rgb(var(--surface))] h-9"
                  >
                    <ArrowUpDown className="h-4 w-4 mr-2" />
                    Sort
                  </Button>
                </div>
              </div>
              <div className="flex items-center gap-1 bg-[rgb(var(--surface))] p-1 rounded-lg self-end lg:self-auto">
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7 text-[rgb(var(--text-main))] rounded-md hover:bg-[rgb(var(--surface))]"
                >
                  <GridIcon className="h-4 w-4" />
                </Button>
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7 text-[rgb(var(--text-main))] hover:text-[rgb(var(--accent))] rounded-md hover:bg-[rgb(var(--surface))]"
                >
                  <ListIcon className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>

          {/* Project Grid */}
          <div className="dotted-grid rounded-[24px] p-6">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {MOCK_PROJECTS.map((project) => (
                <div
                  key={project.id}
                  onClick={() => router.push(`/projects/${project.id}`)}
                  className="cursor-pointer card-interactive bg-[rgb(var(--card))] rounded-[12px] border-[1px] border-[rgb(var(--border))] shadow-[0_12px_30px_rgba(15,23,42,0.08)] hover:border-[rgb(var(--accent))] hover:-translate-y-[4px] transition-all duration-200 ease-in-out flex flex-col gap-0 group overflow-hidden"
                >
                  {/* Thumbnail */}
                  <div className="h-32 dotted-grid bg-[rgb(var(--surface))] border-b border-[rgb(var(--border))] flex items-center justify-center relative group/thumb">
                    <div className="text-[rgb(var(--accent))]/40 group-hover/thumb:text-[rgb(var(--accent))]/60 transition-colors">
                      <Layers className="h-8 w-8" />
                    </div>
                  </div>

                  {/* Content */}
                  <div className="p-[24px] flex flex-col gap-4 flex-1 justify-between">
                    <div className="flex flex-col gap-3">
                      <div className="flex items-start justify-between gap-2">
                        <h3 className="font-semibold text-[16px] leading-[1.5] text-[rgb(var(--text-main))] group-hover:text-[rgb(var(--accent))] transition-colors line-clamp-2">
                          {project.name}
                        </h3>
                        <Badge
                          variant="secondary"
                          className={`shrink-0 border-0 text-[11px] ${
                            project.status === "Active"
                              ? "bg-[#22C55E]/10 text-[#22C55E]"
                              : project.status === "Archived"
                                ? "bg-[#EF4444]/10 text-[#EF4444]"
                                : "bg-[rgb(var(--surface))] text-[rgb(var(--text-main))]"
                          }`}
                        >
                          {project.status}
                        </Badge>
                      </div>
                    </div>

                    <div className="flex flex-col gap-2 mt-2 text-[13px] leading-[1.5] text-[rgb(var(--text-sub))]">
                      <div className="flex items-center gap-2">
                        <Calendar className="h-3.5 w-3.5 text-[rgb(var(--text-sub))]" />
                        <span className="text-[rgb(var(--text-sub))]">Created {project.createdAt}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <User className="h-3.5 w-3.5 text-[rgb(var(--text-sub))]" />
                        <span className="text-[rgb(var(--text-sub))]">{project.owner}</span>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 mt-auto pt-4 border-t border-[rgb(var(--border))]">
                      <Button
                        variant="ghost"
                        onClick={(e) => {
                          e.stopPropagation();
                          router.push(`/projects/${project.id}`);
                        }}
                        className="flex-1 justify-center bg-transparent border-[1px] border-[rgb(var(--accent))] text-[rgb(var(--accent))] hover:bg-[rgb(var(--accent))]/10 text-[13px] h-8 font-medium transition-all"
                      >
                        View
                      </Button>
                      <Button
                        variant="ghost"
                        onClick={(e) => {
                          e.stopPropagation();
                        }}
                        className="flex-1 justify-center bg-transparent border-[1px] border-[rgb(var(--border))] text-[rgb(var(--text-sub))] hover:bg-[rgb(var(--surface))] text-[13px] h-8 font-medium transition-all"
                      >
                        Delete
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Pagination */}
          <div className="flex items-center justify-center gap-2 mt-4 mb-4">
            <Button
              variant="outline"
              size="icon"
              className="h-8 w-8 border-[rgb(var(--border))] text-[rgb(var(--muted-foreground))] hover:bg-[rgb(var(--surface))] rounded-lg"
              disabled
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              className="h-8 w-8 p-0 bg-[rgb(var(--accent))] text-[rgb(var(--accent-foreground))] hover:bg-[#38BDF8] rounded-lg font-medium text-[13px]"
            >
              1
            </Button>
            <Button
              variant="ghost"
              className="h-8 w-8 p-0 text-[rgb(var(--text-main))] hover:bg-[rgb(var(--surface))] hover:text-[rgb(var(--text-main))] rounded-lg font-medium text-[13px]"
            >
              2
            </Button>
            <Button
              variant="ghost"
              className="h-8 w-8 p-0 text-[rgb(var(--text-main))] hover:bg-[rgb(var(--surface))] hover:text-[rgb(var(--text-main))] rounded-lg font-medium text-[13px]"
            >
              3
            </Button>
            <Button
              variant="outline"
              size="icon"
              className="h-8 w-8 border-[rgb(var(--border))] text-[rgb(var(--muted-foreground))] hover:bg-[rgb(var(--surface))] rounded-lg"
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>

        {/* Activity Feed Sidebar */}
        <aside className="lg:sticky lg:top-24 lg:self-start">
          <div className="bg-[rgb(var(--card))] rounded-lg border border-[rgb(var(--border))] p-4">
            <div className="flex items-center gap-2 mb-4 pb-4 border-b border-[rgb(var(--border))]">
              <Activity className="h-4 w-4 text-[rgb(var(--accent))]" />
              <h3 className="text-[14px] font-semibold text-[rgb(var(--text-main))]">Recent Activity</h3>
            </div>
            <div className="flex flex-col gap-3">
              {activities.map((activity) => (
                <div key={activity.id} className="flex flex-col gap-1.5 py-2.5 px-0 hover:bg-[rgb(var(--surface))] rounded-md transition-colors cursor-pointer">
                  <p className="text-[12px] font-medium text-[rgb(var(--text-main))]">{activity.action}</p>
                  <p className="text-[11px] text-[rgb(var(--text-sub))]">{activity.project}</p>
                  <div className="flex items-center gap-1">
                    <span className="text-[10px] text-[rgb(var(--muted-foreground))]">{activity.time}</span>
                    <ArrowRight className="h-3 w-3 text-[rgb(var(--accent))] opacity-0 group-hover:opacity-100" />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}
