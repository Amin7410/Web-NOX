import Link from "next/link";
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
  ChevronRight
} from "lucide-react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Badge } from "../ui/badge";

interface ProjectItem {
  id: string;
  name: string;
  status: 'Active' | 'Archived' | 'Draft';
  createdAt: string;
  owner: string;
}

const projects: ProjectItem[] = [
  { id: "1", name: "Website Redesign", status: "Active", createdAt: "Oct 12, 2025", owner: "Sarah Jenkins" },
  { id: "2", name: "Mobile App V2", status: "Active", createdAt: "Oct 15, 2025", owner: "Mike Ross" },
  { id: "3", name: "Q4 Marketing", status: "Draft", createdAt: "Oct 20, 2025", owner: "Anna Smith" },
  { id: "4", name: "Backend Migration", status: "Active", createdAt: "Nov 02, 2025", owner: "David Chen" },
  { id: "5", name: "User Research", status: "Archived", createdAt: "Aug 10, 2025", owner: "Emma Wilson" },
  { id: "6", name: "Design System", status: "Active", createdAt: "Sep 05, 2025", owner: "Sarah Jenkins" },
  { id: "7", name: "Security Audit", status: "Archived", createdAt: "Jul 22, 2025", owner: "Alex Turner" },
  { id: "8", name: "Social Media Campaign", status: "Draft", createdAt: "Nov 15, 2025", owner: "Anna Smith" },
];

export function ProjectsManagement() {
  return (
    <div className="flex flex-col w-full">
      {/* Header Section */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex flex-col gap-1">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 tracking-tight">Projects Management</h1>
          <p className="text-sm text-gray-500">Manage and organize your projects</p>
        </div>
        <Link href="/projects/new">
          <Button className="bg-[#4F46E5] hover:bg-[#4338CA] text-white rounded-lg px-4 py-2 h-auto shrink-0 shadow-sm transition-all">
            <Plus className="h-4 w-4 mr-2" />
            Create Project
          </Button>
        </Link>
      </div>

      {/* Control Bar & Statistics Container */}
      <div className="flex flex-col gap-6 mt-8">
        {/* Statistics */}
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2 bg-white px-3 py-1.5 rounded-lg border border-gray-200 shadow-sm">
            <span className="text-sm font-medium text-gray-900">12 Projects</span>
          </div>
          <div className="flex items-center gap-2 bg-white px-3 py-1.5 rounded-lg border border-gray-200 shadow-sm">
            <div className="w-2 h-2 rounded-full bg-[#22C55E]"></div>
            <span className="text-sm font-medium text-gray-600">3 Active</span>
          </div>
          <div className="flex items-center gap-2 bg-white px-3 py-1.5 rounded-lg border border-gray-200 shadow-sm">
            <div className="w-2 h-2 rounded-full bg-[#EF4444]"></div>
            <span className="text-sm font-medium text-gray-600">2 Archived</span>
          </div>
        </div>

        {/* Control Bar */}
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
          <div className="flex flex-col sm:flex-row items-center gap-4 w-full lg:w-auto">
            <div className="relative w-full sm:w-80">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
              <Input 
                placeholder="Search projects..." 
                className="pl-9 w-full bg-gray-50 border-gray-200 focus-visible:ring-[#4F46E5] focus-visible:border-[#4F46E5] h-9"
              />
            </div>
            <div className="flex items-center gap-2 w-full sm:w-auto">
              <Button variant="outline" size="sm" className="w-full sm:w-auto border-gray-200 text-gray-600 bg-white hover:bg-gray-50 h-9">
                <Filter className="h-4 w-4 mr-2" />
                Filter
              </Button>
              <Button variant="outline" size="sm" className="w-full sm:w-auto border-gray-200 text-gray-600 bg-white hover:bg-gray-50 h-9">
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
      </div>

      {/* Project Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mt-8">
        {projects.map((project) => (
          <div 
            key={project.id} 
            className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm hover:shadow-md hover:border-[#4F46E5]/30 hover:-translate-y-0.5 transition-all duration-200 flex flex-col gap-4 group"
          >
            <div className="flex flex-col gap-3">
              <div className="flex items-start justify-between gap-2">
                <Link href={`/projects/${project.id}`}>
                  <h3 className="font-semibold text-gray-900 text-lg leading-tight group-hover:text-[#4F46E5] transition-colors line-clamp-2 cursor-pointer">
                    {project.name}
                  </h3>
                </Link>
                <Badge 
                  variant="secondary" 
                  className={`shrink-0 border-0 ${
                    project.status === 'Active' ? 'bg-[#22C55E]/10 text-[#22C55E]' :
                    project.status === 'Archived' ? 'bg-[#EF4444]/10 text-[#EF4444]' :
                    'bg-gray-100 text-gray-600'
                  }`}
                >
                  {project.status}
                </Badge>
              </div>
            </div>
            
            <div className="flex flex-col gap-2 mt-2">
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <Calendar className="h-4 w-4 text-gray-400" />
                <span>Created {project.createdAt}</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <User className="h-4 w-4 text-gray-400" />
                <span>{project.owner}</span>
              </div>
            </div>

            <div className="flex items-center gap-2 mt-auto pt-4 border-t border-gray-100">
              <Link href={`/projects/${project.id}/edit`}>
                <Button 
                  variant="outline" 
                  className="flex-1 bg-white border-gray-200 text-gray-700 hover:bg-gray-50 hover:text-gray-900 text-sm h-9 font-medium transition-colors"
                >
                  Edit
                </Button>
              </Link>
              <Button 
                variant="outline" 
                className="flex-1 bg-white border-red-100 text-[#EF4444] hover:bg-red-50 hover:text-[#DC2626] hover:border-red-200 text-sm h-9 font-medium transition-colors"
              >
                Delete
              </Button>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-center gap-2 mt-10 mb-4">
        <Button variant="outline" size="icon" className="h-8 w-8 border-gray-200 text-gray-500 hover:bg-gray-50 rounded-lg" disabled>
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <Button variant="ghost" className="h-8 w-8 p-0 bg-[#4F46E5] text-white hover:bg-[#4338CA] hover:text-white rounded-lg font-medium">
          1
        </Button>
        <Button variant="ghost" className="h-8 w-8 p-0 text-gray-600 hover:bg-gray-100 hover:text-gray-900 rounded-lg font-medium">
          2
        </Button>
        <Button variant="ghost" className="h-8 w-8 p-0 text-gray-600 hover:bg-gray-100 hover:text-gray-900 rounded-lg font-medium">
          3
        </Button>
        <Button variant="outline" size="icon" className="h-8 w-8 border-gray-200 text-gray-500 hover:bg-gray-50 rounded-lg">
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}
