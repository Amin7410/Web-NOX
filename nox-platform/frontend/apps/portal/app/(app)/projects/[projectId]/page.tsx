"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";
import { api } from "../../../_lib/api";

interface Project {
    id: string;
    orgId: string;
    orgName: string;
    name: string;
    slug: string;
    visibility: string;
    status: string;
    updatedAt: string;
}

export default function ProjectOverviewPage({ params }: { params: { projectId: string } }) {
    const [project, setProject] = useState<Project | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function loadProject() {
            setLoading(true);
            try {
                // Fetch the specific project. 
                // We might not have the correct X-Org-Id yet, 
                // but the backend should allow fetching by ID if the user is a member of its org.
                const data = await api.get<Project>(`/projects/${params.projectId}`);
                setProject(data);
                
                // Set the active organization ID in local storage to ensure 
                // subsequent API calls are scoped correctly to this project's organization.
                if (data.orgId) {
                    localStorage.setItem("active_org_id", data.orgId);
                }
            } catch (error) {
                console.error("Failed to fetch project", error);
                setProject(null);
            } finally {
                setLoading(false);
            }
        }
        loadProject();
    }, [params.projectId]);

    if (loading) {
        return <div className="py-12 text-center text-zinc-500">Loading project details...</div>;
    }

    if (!project) {
        return (
            <Card title="Project not found">
                <div className="space-y-4">
                    <p className="text-sm text-zinc-400">The project you are looking for does not exist or you do not have permission to view it.</p>
                    <Button asChild variant="outline">
                        <Link href="/projects">Back to projects</Link>
                    </Button>
                </div>
            </Card>
        );
    }

    return (
        <div className="space-y-6">
            <PageHeader
                title={project.name}
                subtitle={`${project.orgName} · ${project.visibility} · ${project.status}`}
                actions={
                    <>
                        <Button asChild variant="outline" type="button">
                            <Link href={`/projects/${project.id}/settings`}>Settings</Link>
                        </Button>
                        <Button asChild type="button">
                            <a href={`http://localhost:5173/?projectId=${project.id}`} target="_blank" rel="noreferrer">
                                Open in Studio
                            </a>
                        </Button>
                    </>
                }
            />

            <div className="grid gap-4 md:grid-cols-3">
                <Card title="Organization">
                    <div className="text-sm text-zinc-300">{project.orgName}</div>
                    <div className="mt-3">
                        <Button asChild size="sm" variant="outline" type="button">
                            <Link href={`/organizations/${project.orgId}`}>View organization</Link>
                        </Button>
                    </div>
                </Card>
                <Card title="Project slug">
                    <div className="font-mono text-sm text-zinc-300">{project.slug}</div>
                </Card>
                <Card title="Last updated">
                    <div className="text-sm text-zinc-300">
                        {new Date(project.updatedAt).toLocaleDateString()}
                    </div>
                </Card>
            </div>

            <Card title="Workspaces" description="Manage environments inside this project.">
                <div className="flex flex-wrap gap-2">
                    <Button asChild variant="outline" type="button">
                        <Link href={`/projects/${project.id}/workspaces`}>View workspaces</Link>
                    </Button>
                    <Button asChild type="button">
                        <Link href={`/projects/${project.id}/workspaces/create`}>New workspace</Link>
                    </Button>
                </div>
            </Card>
        </div>
    );
}

