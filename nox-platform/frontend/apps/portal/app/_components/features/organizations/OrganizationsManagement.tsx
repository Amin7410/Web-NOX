'use client';

import { 
  Building2, Plus, Users, Layout, ChevronRight, 
  Search, Loader2
} from "lucide-react";
import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Button } from "../../../ui/button";
import { Input } from "../../../ui/input";
import { Badge } from "../../../ui/badge";
import { mockStore } from "@/lib/mock-store";
import { Organization } from "./data";

const MOCK_ORGS: Organization[] = [
  {
    id: "mock-1",
    name: "NOX Default Team",
    slug: "nox-default",
    role: "Owner",
    memberCount: 1,
    projectCount: 3,
    createdAt: new Date().toLocaleDateString(),
    status: "Active"
  }
];

export function OrganizationsManagement() {
  const router = useRouter();
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [error, setError] = useState<string | null>(null); // Added error state

  useEffect(() => {
    const fetchOrganizations = async () => {
      try {
        const res = await fetch("/api/orgs");
        if (res.status === 401) {
          setError("Session expired. Please login again.");
          setLoading(false);
          return;
        }
        const data = await res.json();
        const orgList = data.data || [];
        if (orgList.length === 0) {
          // Use global mock store
          setOrganizations(mockStore.getOrganizations());
        } else {
          setOrganizations(orgList.map((o: any) => ({
            id: o.id,
            name: o.name,
            slug: o.slug,
            role: "Member",
            memberCount: 1, 
            projectCount: 0,
            createdAt: o.createdAt ? new Date(o.createdAt).toLocaleDateString() : "—",
            status: "Active"
          })));
        }
      } catch (err) {
        console.error("Failed to fetch orgs", err);
        setError("Failed to load organizations.");
      } finally {
        setLoading(false);
      }
    };

    fetchOrganizations();
  }, []);

  const filteredOrgs = organizations.filter(org => 
    org.name.toLowerCase().includes(search.toLowerCase()) || 
    org.slug.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="flex flex-col w-full">
      {/* Header Section */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex flex-col gap-1">
          <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 tracking-tight">
            Organizations
          </h1>
          <p className="text-sm text-gray-500">
            Manage your teams and shared workspaces
          </p>
        </div>
        <Button
          onClick={() => router.push("/organizations/create")}
          className="bg-[#4F46E5] hover:bg-[#4338CA] text-white rounded-lg px-4 py-2 h-auto shrink-0 shadow-sm transition-all"
        >
          <Plus className="h-4 w-4 mr-2" />
          New Organization
        </Button>
      </div>

      {/* Control Bar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mt-8 bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
        <div className="relative w-full sm:w-80">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
          <Input
            placeholder="Search organizations..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9 w-full bg-gray-50 border-gray-200 focus-visible:ring-[#4F46E5] focus-visible:border-[#4F46E5] h-9 transition-all"
          />
        </div>
      </div>

      {/* Organization Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-8">
        {loading ? (
           <div className="col-span-full py-20 flex justify-center items-center">
              <Loader2 className="h-8 w-8 text-[#4F46E5] animate-spin" />
           </div>
        ) : filteredOrgs.length === 0 ? (
          <div className="col-span-full py-20 text-center text-gray-500">
             No organizations found.
          </div>
        ) : filteredOrgs.map((org) => (
          <div
            key={org.id}
            onClick={() => router.push(`/organizations/${org.id}`)}
            className="group cursor-pointer bg-white rounded-xl border border-gray-200 p-6 shadow-sm hover:shadow-md hover:border-[#4F46E5]/30 hover:-translate-y-0.5 transition-all duration-200"
          >
            <div className="flex items-start justify-between mb-4">
              <div className="h-12 w-12 rounded-xl bg-gray-50 border border-gray-100 flex items-center justify-center text-[#4F46E5] group-hover:bg-[#4F46E5]/5 transition-colors">
                <Building2 className="h-6 w-6" />
              </div>
              <Badge 
                variant="secondary" 
                className={`px-2 py-0.5 text-[10px] font-bold uppercase tracking-wider rounded-md border-0 ${
                  org.role === 'Owner' ? 'bg-[#4F46E5]/10 text-[#4F46E5]' : 
                  org.role === 'Admin' ? 'bg-amber-50 text-amber-600' : 
                  'bg-gray-100 text-gray-600'
                }`}
              >
                {org.role}
              </Badge>
            </div>

            <div className="flex flex-col gap-1">
              <h3 className="text-lg font-bold text-gray-900 group-hover:text-[#4F46E5] transition-colors truncate">
                {org.name}
              </h3>
              <p className="text-sm text-gray-500 font-medium">@{org.slug}</p>
            </div>

            <div className="grid grid-cols-2 gap-4 mt-6">
              <div className="flex items-center gap-2.5 text-gray-600 bg-gray-50/50 rounded-lg p-2 border border-gray-100">
                <Users className="h-4 w-4 text-gray-400" />
                <div className="flex flex-col">
                  <span className="text-xs font-semibold text-gray-900 leading-tight">{org.memberCount}</span>
                  <span className="text-[10px] text-gray-500 uppercase font-bold tracking-tighter">Members</span>
                </div>
              </div>
              <div className="flex items-center gap-2.5 text-gray-600 bg-gray-50/50 rounded-lg p-2 border border-gray-100">
                <Layout className="h-4 w-4 text-gray-400" />
                <div className="flex flex-col">
                  <span className="text-xs font-semibold text-gray-900 leading-tight">{org.projectCount}</span>
                  <span className="text-[10px] text-gray-500 uppercase font-bold tracking-tighter">Projects</span>
                </div>
              </div>
            </div>

            <div className="mt-6 pt-5 border-t border-gray-100 flex items-center justify-between text-sm">
              <span className="text-gray-400 font-medium text-xs">Created {org.createdAt}</span>
              <ChevronRight className="h-4 w-4 text-gray-300 group-hover:text-[#4F46E5] group-hover:translate-x-0.5 transition-all" />
            </div>
          </div>
        ))}

        {/* Create New Card */}
        <Link
          href="/organizations/create"
          className="flex flex-col items-center justify-center gap-4 rounded-xl border-2 border-dashed border-gray-200 p-8 hover:border-[#4F46E5] hover:bg-[#4F46E5]/5 transition-all group min-h-[240px]"
        >
          <div className="h-12 w-12 rounded-full bg-gray-50 flex items-center justify-center text-gray-400 group-hover:bg-white group-hover:text-[#4F46E5] group-hover:shadow-sm transition-all">
            <Plus className="h-6 w-6" />
          </div>
          <div className="flex flex-col items-center text-center gap-1">
            <span className="font-bold text-gray-900 group-hover:text-[#4F46E5] transition-colors">Add New Team</span>
            <p className="text-xs text-gray-500 font-medium max-w-[160px]">Create a shared workspace for your projects</p>
          </div>
        </Link>
      </div>
    </div>
  );
}
