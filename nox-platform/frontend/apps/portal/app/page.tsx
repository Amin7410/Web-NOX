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

import { Button } from "./ui/button";

import { Input } from "./ui/input";

import { Badge } from "./ui/badge";



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



export default function Home() {

  return (

    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50">

      <div className="text-center">

        <h1 className="text-4xl font-bold text-gray-900 mb-4">Welcome to NOX Portal</h1>

        <p className="text-xl text-gray-600 mb-8">Manage your projects and collaborate with your team</p>

        <a 

          href="/projects"

          className="inline-flex items-center px-6 py-3 bg-[#4F46E5] hover:bg-[#4338CA] text-white rounded-lg font-medium transition-colors"

        >

          Go to Projects

        </a>

      </div>

    </div>

  );

}

