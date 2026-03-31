"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../../../../_components/UiBits";
import { api } from "../../../../../_lib/api";

interface Workspace {
    id: string;
    name: string;
    type: string;
    status: string;
    createdAt: string;
}

export default function WorkspaceOverviewPage({
    params,
}: {
    params: { projectId: string; workspaceId: string };
}) {
    const [workspace, setWorkspace] = useState<Workspace | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function loadWorkspace() {
            setLoading(true);
            try {
                // Since there is no specific GET /{workspaceId} in basic controller yet,
                // we fetch the list and filter for now, or just trust the context.
                // Standardizing to fetch list for compatibility.
                const workspaces = await api.get<Workspace[]>(`/projects/${params.projectId}/workspaces`);
                const found = workspaces.find(w => w.id === params.workspaceId);
                setWorkspace(found || null);
            } catch (error) {
                console.error("Failed to fetch workspace", error);
            } finally {
                setLoading(false);
            }
        }
        loadWorkspace();
    }, [params.projectId, params.workspaceId]);

    if (loading) return <div className="py-12 text-center text-zinc-500">Loading workspace...</div>;
    if (!workspace) return <div className="py-12 text-center text-zinc-500">Workspace not found</div>;

    return (
        <div className="space-y-6">
            <PageHeader
                title={workspace.name}
                subtitle={`Type: ${workspace.type}`}
                actions={
                    <div className="flex gap-3">
                        <Button asChild variant="outline" type="button">
                            <Link href={`/projects/${params.projectId}/workspaces`}>Back to workspaces</Link>
                        </Button>
                        <Button asChild type="button">
                            <a
                                href={`http://localhost:5173/?projectId=${params.projectId}&workspaceId=${workspace.id}`}
                                target="_blank"
                                rel="noreferrer"
                            >
                                Open in Studio
                            </a>
                        </Button>
                    </div>
                }
            />

            <div className="grid gap-4 md:grid-cols-2">
                <Card title="Details">
                    <div className="space-y-2 text-sm">
                        <div className="flex items-center justify-between py-1 border-b border-white/5">
                            <div className="text-zinc-400">Project</div>
                            <div className="font-mono text-zinc-300">{params.projectId}</div>
                        </div>
                        <div className="flex items-center justify-between py-1 border-b border-white/5">
                            <div className="text-zinc-400">Workspace ID</div>
                            <div className="font-mono text-zinc-300">{workspace.id}</div>
                        </div>
                        <div className="flex items-center justify-between py-1 border-b border-white/5">
                            <div className="text-zinc-400">Status</div>
                            <div className="text-zinc-300">{workspace.status}</div>
                        </div>
                        <div className="flex items-center justify-between py-1">
                            <div className="text-zinc-400">Created</div>
                            <div className="text-zinc-300">{new Date(workspace.createdAt).toLocaleDateString()}</div>
                        </div>
                    </div>
                </Card>

                <Card title="Snapshots" description="History of this workspace.">
                    <div className="text-sm text-zinc-500 italic">No snapshots available in this workspace yet.</div>
                </Card>
            </div>
        </div>
    );
}

