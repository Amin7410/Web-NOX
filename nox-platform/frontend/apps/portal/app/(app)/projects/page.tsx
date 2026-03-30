"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../_components/UiBits";
import { api } from "../../_lib/api";

interface Project {
    id: string;
    orgId: string;
    orgName: string;
    name: string;
    slug: string;
    description: string;
    visibility: string;
    status: string;
    updatedAt: string;
}

interface Organization {
    id: string;
    name: string;
}

interface Page<T> {
    content: T[];
}

export default function ProjectsPage() {
    const [projects, setProjects] = useState<Project[]>([]);
    const [organizations, setOrganizations] = useState<Organization[]>([]);
    const [activeOrgId, setActiveOrgId] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // First, fetch organizations to allow filtering and picking a default
        async function loadInitial() {
            try {
                // Fetch known org IDs from local storage (workaround since backend lacks "list mine")
                const savedIds = JSON.parse(localStorage.getItem("nox_org_ids") || "[]");
                const orgs: Organization[] = [];
                
                for (const id of savedIds) {
                    try {
                        const org = await api.get<Organization>(`/orgs/${id}`);
                        orgs.push(org);
                    } catch (e) {
                        console.error(`Failed to fetch org ${id}`, e);
                    }
                }
                setOrganizations(orgs);

                // Determine active org
                let current = localStorage.getItem("active_org_id");
                if (!current && orgs.length > 0) {
                    current = orgs[0].id;
                    localStorage.setItem("active_org_id", current);
                }
                setActiveOrgId(current);
            } catch (e) {
                console.error("Failed to load organizations", e);
            }
        }
        loadInitial();
    }, []);

    useEffect(() => {
        if (!activeOrgId) return;

        async function loadProjects() {
            setLoading(true);
            try {
                // API helper will automatically attach activeOrgId via X-Org-Id header
                const response = await api.get<Page<Project>>("/projects");
                setProjects(response.content || []);
            } catch (e) {
                console.error("Failed to fetch projects", e);
                setProjects([]);
            } finally {
                setLoading(false);
            }
        }
        loadProjects();
    }, [activeOrgId]);

    const handleOrgChange = (id: string) => {
        localStorage.setItem("active_org_id", id);
        setActiveOrgId(id);
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Projects"
                subtitle="Browse and manage all your projects."
                actions={
                    <Button asChild>
                        <Link href="/projects/create">New project</Link>
                    </Button>
                }
            />

            <Card title="Context & Filters">
                <div className="grid gap-3 md:grid-cols-4">
                    <div className="space-y-1">
                        <label className="text-xs text-zinc-500">Active Organization</label>
                        <select 
                            className="w-full rounded-md border border-zinc-800 bg-zinc-950 px-3 py-2 text-sm text-zinc-100 focus:outline-none focus:ring-2 focus:ring-blue-500/30"
                            value={activeOrgId || ""}
                            onChange={(e) => handleOrgChange(e.target.value)}
                        >
                            {organizations.length === 0 && <option value="">No organizations found</option>}
                            {organizations.map(org => (
                                <option key={org.id} value={org.id}>{org.name}</option>
                            ))}
                        </select>
                    </div>
                    <Input placeholder="Visibility (All)" disabled />
                    <Input placeholder="Status (All)" disabled />
                    <Input placeholder="Search by name or slug..." disabled />
                </div>
            </Card>

            {loading ? (
                <div className="py-12 text-center text-zinc-500">Loading projects...</div>
            ) : projects.length === 0 ? (
                <Card title="No projects yet" description={activeOrgId ? "Create your first project in this organization." : "Select or create an organization first."}>
                    <Button asChild disabled={!activeOrgId}>
                        <Link href="/projects/create">Create project</Link>
                    </Button>
                </Card>
            ) : (
                <Card title="Projects">
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead className="text-left text-zinc-400">
                                <tr className="border-b border-white/10">
                                    <th className="py-2 pr-4">Name</th>
                                    <th className="py-2 pr-4">Organization</th>
                                    <th className="py-2 pr-4">Visibility</th>
                                    <th className="py-2 pr-4">Status</th>
                                    <th className="py-2 pr-4">Updated</th>
                                    <th className="py-2 text-right">Open</th>
                                </tr>
                            </thead>
                            <tbody>
                                {projects.map((p) => (
                                    <tr key={p.id} className="border-b border-white/10">
                                        <td className="py-3 pr-4">
                                            <div className="font-medium">{p.name}</div>
                                            <div className="text-xs text-zinc-500">{p.slug}</div>
                                        </td>
                                        <td className="py-3 pr-4">{p.orgName}</td>
                                        <td className="py-3 pr-4 text-xs">
                                            <span className="rounded-full bg-zinc-800 px-2 py-0.5">{p.visibility}</span>
                                        </td>
                                        <td className="py-3 pr-4 text-xs font-medium text-emerald-500">{p.status}</td>
                                        <td className="py-3 pr-4 text-zinc-500">{new Date(p.updatedAt).toLocaleDateString()}</td>
                                        <td className="py-3 text-right">
                                            <Button asChild size="sm" variant="outline">
                                                <Link href={`/projects/${p.id}`}>Open</Link>
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </Card>
            )}
        </div>
    );
}

