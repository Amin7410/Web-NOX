"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../../../_components/UiBits";
import { api } from "../../../../_lib/api";

interface Workspace {
    id: string;
    name: string;
    type: string;
    status: string;
    createdAt: string;
}

export default function WorkspacesPage({ params }: { params: { projectId: string } }) {
    const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function loadWorkspaces() {
            setLoading(true);
            try {
                // Fetch the workspaces for this project.
                // The X-Org-Id header should be set in localStorage from the ProjectOverviewPage.
                const data = await api.get<Workspace[]>(`/projects/${params.projectId}/workspaces`);
                setWorkspaces(data);
            } catch (error) {
                console.error("Failed to fetch workspaces", error);
            } finally {
                setLoading(false);
            }
        }
        loadWorkspaces();
    }, [params.projectId]);

    return (
        <div className="space-y-6">
            <PageHeader
                title="Workspaces"
                subtitle="Different environments inside this project."
                actions={
                    <Button asChild type="button">
                        <Link href={`/projects/${params.projectId}/workspaces/create`}>New workspace</Link>
                    </Button>
                }
            />

            {loading ? (
                <div className="py-12 text-center text-zinc-500">Loading workspaces...</div>
            ) : workspaces.length === 0 ? (
                <Card title="No workspaces yet" description="Create a workspace to start designing in Studio.">
                    <Button asChild type="button">
                        <Link href={`/projects/${params.projectId}/workspaces/create`}>Create workspace</Link>
                    </Button>
                </Card>
            ) : (
                <Card title="Workspaces">
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead className="text-left text-zinc-400">
                                <tr className="border-b border-white/10">
                                    <th className="py-2 pr-4 font-medium">Name</th>
                                    <th className="py-2 pr-4 font-medium">Type</th>
                                    <th className="py-2 pr-4 font-medium">Status</th>
                                    <th className="py-2 pr-4 font-medium">Created</th>
                                    <th className="py-2 text-right font-medium">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {workspaces.map((w) => (
                                    <tr key={w.id} className="border-b border-white/10">
                                        <td className="py-3 pr-4">
                                            <div className="font-semibold text-zinc-100">{w.name}</div>
                                            <div className="text-xs text-zinc-500 font-mono">{w.id}</div>
                                        </td>
                                        <td className="py-3 pr-4">
                                            <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
                                                w.type === 'MAIN' ? 'bg-blue-500/10 text-blue-400' : 'bg-zinc-800 text-zinc-400'
                                            }`}>
                                                {w.type}
                                            </span>
                                        </td>
                                        <td className="py-3 pr-4">
                                            <span className="text-zinc-400">{w.status}</span>
                                        </td>
                                        <td className="py-3 pr-4 text-zinc-500">
                                            {new Date(w.createdAt).toLocaleDateString()}
                                        </td>
                                        <td className="py-3 text-right">
                                            <div className="inline-flex gap-2">
                                                <Button asChild size="sm" variant="outline" type="button">
                                                    <Link href={`/projects/${params.projectId}/workspaces/${w.id}`}>
                                                        Details
                                                    </Link>
                                                </Button>
                                                <Button asChild size="sm" type="button">
                                                    <a
                                                        href={`http://localhost:5173/?projectId=${params.projectId}&workspaceId=${w.id}`}
                                                        target="_blank"
                                                        rel="noreferrer"
                                                    >
                                                        Studio
                                                    </a>
                                                </Button>
                                            </div>
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

