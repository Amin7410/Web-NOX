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
} from "lucide-react";
import { Button } from "../../../ui/button";
import { Input } from "../../../ui/input";
import { Badge } from "../../../ui/badge";
import { useRouter } from "next/navigation";
import { MOCK_PROJECTS } from "./data";

export function ProjectsManagement() {
  const router = useRouter();

  return (
    <div className="flex flex-col w-full">
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

      {/* Control Bar & Statistics Container */}
      <div className="flex flex-col gap-6 mt-8">
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
                className="pl-9 w-full bg-[rgb(var(--card))] border-[rgb(var(--border))] focus-visible:ring-[rgb(var(--accent))] focus-visible:border-[rgb(var(--accent))] h-9"
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
      <div className="dotted-grid rounded-[24px] p-6 mt-8">
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {MOCK_PROJECTS.map((project) => (
            <div
              key={project.id}
              onClick={() => router.push(`/projects/${project.id}`)}
              className="cursor-pointer card-interactive bg-[rgb(var(--card))] rounded-[12px] border border-[color:var(--card-border)] p-6 shadow-[0_12px_30px_rgba(15,23,42,0.08)] hover:border-[color:var(--card-border-hover)] hover:-translate-y-[4px] transition-all duration-200 ease-in-out flex flex-col gap-4 group"
            >
            <div className="flex flex-col gap-3">
              <div className="flex items-start justify-between gap-2">
                <h3 className="font-bold text-[20px] leading-[1.5] text-[rgb(var(--text-main))] group-hover:text-[rgb(var(--accent))] transition-colors line-clamp-2">
                  {project.name}
                </h3>
                <Badge
                  variant="secondary"
                  className={`shrink-0 border-0 ${
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

<div className="flex flex-col gap-2 mt-2 text-[14px] leading-[1.5] text-[rgb(var(--text-sub))]">
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-[rgb(var(--text-sub))]" />
                  <span className="text-[rgb(var(--text-sub))]">Created {project.createdAt}</span>
                </div>
                <div className="flex items-center gap-2">
                  <User className="h-4 w-4 text-[rgb(var(--text-sub))]" />
                  <span className="text-[rgb(var(--text-sub))]">{project.owner}</span>
              </div>
            </div>

            <div className="flex items-center gap-2 mt-auto pt-4 border-t border-[rgb(var(--border))]">
              <Button
                variant="outline"
                onClick={(e) => {
                  e.stopPropagation();
                  router.push(`/projects/${project.id}`);
                }}
                className="flex-1 justify-center bg-transparent border-[rgb(var(--accent))] text-[rgb(var(--accent))] hover:bg-[rgb(var(--accent))]/10 text-[14px] h-9 font-medium transition-colors"
              >
                View
              </Button>
              <Button
                variant="outline"
                onClick={(e) => {
                  e.stopPropagation();
                }}
                className="flex-1 justify-center bg-transparent border-[rgb(var(--accent))] text-[rgb(var(--accent))] hover:bg-[rgb(var(--accent))]/10 text-[14px] h-9 font-medium transition-colors"
              >
                Delete
              </Button>
            </div>
          </div>
        ))}
        </div>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-center gap-2 mt-10 mb-4">
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
          className="h-8 w-8 p-0 bg-[rgb(var(--accent))] text-[rgb(var(--accent-foreground))] hover:bg-[#38BDF8] rounded-lg font-medium"
        >
          1
        </Button>
        <Button
          variant="ghost"
          className="h-8 w-8 p-0 text-[rgb(var(--text-main))] hover:bg-[rgb(var(--surface))] hover:text-[rgb(var(--text-main))] rounded-lg font-medium"
        >
          2
        </Button>
        <Button
          variant="ghost"
          className="h-8 w-8 p-0 text-[rgb(var(--text-main))] hover:bg-[rgb(var(--surface))] hover:text-[rgb(var(--text-main))] rounded-lg font-medium"
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
  );
}
