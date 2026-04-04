'use client';

import Link from "next/link";
import { Button } from "@nox/ui";
import { Card, PageHeader, Alert } from "../../../../_components/UiBits";
import { useState, useEffect } from "react";
import { Trash2 } from "lucide-react";

interface Workspace {
    id: string;
    name: string;
    type: 'MAIN' | 'EXPERIMENT' | 'SANDBOX';
    createdAt: string;
    status?: string;
}

export default function WorkspacesPage({ params }: { params: { projectId: string } }) {
    const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [deletingId, setDeletingId] = useState<string | null>(null);

    useEffect(() => {
        const fetchWorkspaces = async () => {
            try {
                const response = await fetch(`/api/v1/projects/${params.projectId}/workspaces`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });

                if (response.ok) {
                    const data = await response.json();
                    setWorkspaces(data.data || []);
                } else {
                    setError('Failed to load workspaces');
                }
            } catch (err) {
                console.error('Failed to fetch workspaces:', err);
                setError('An error occurred while loading workspaces');
            } finally {
                setLoading(false);
            }
        };

        fetchWorkspaces();
    }, [params.projectId]);

    const handleDeleteWorkspace = async (workspaceId: string) => {
        if (!confirm('Are you sure you want to delete this workspace? This action cannot be undone.')) {
            return;
        }

        setDeletingId(workspaceId);
        setError(null);

        try {
            const response = await fetch(`/api/v1/projects/${params.projectId}/workspaces/${workspaceId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Failed to delete workspace');
            }

            setWorkspaces(workspaces.filter(w => w.id !== workspaceId));
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setDeletingId(null);
        }
    };

    if (loading) {
        return (
            <div className="space-y-6">
                <PageHeader
                    title="Workspaces"
                    subtitle="Different environments inside this project."
                />
                <div className="text-zinc-400">Loading workspaces...</div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <PageHeader
                title="Workspaces"
                subtitle="Different environments inside this project."
                actions={
                    <Button asChild>
                        <Link href={`/projects/${params.projectId}/workspaces/create`}>New workspace</Link>
                    </Button>
                }
            />

            {error && (
                <Alert type="error" title="Error" message={error} />
            )}

            {workspaces.length === 0 ? (
                <Card title="No workspaces yet" description="Create a workspace to start designing in Studio.">
                    <Button asChild>
                        <Link href={`/projects/${params.projectId}/workspaces/create`}>Create workspace</Link>
                    </Button>
                </Card>
            ) : (
                <Card title="Workspaces">
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead className="text-left text-zinc-400">
                                <tr className="border-b border-white/10">
                                    <th className="py-2 pr-4">Name</th>
                                    <th className="py-2 pr-4">Type</th>
                                    <th className="py-2 pr-4">Created</th>
                                    <th className="py-2 text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {workspaces.map((w) => (
                                    <tr key={w.id} className="border-b border-white/10">
                                        <td className="py-3 pr-4">
                                            <div className="font-medium">{w.name}</div>
                                        </td>
                                        <td className="py-3 pr-4">{w.type}</td>
                                        <td className="py-3 pr-4">{new Date(w.createdAt).toLocaleDateString()}</td>
                                        <td className="py-3 text-right">
                                            <div className="inline-flex gap-2">
                                                <Button asChild size="sm" variant="outline">
                                                    <Link href={`/projects/${params.projectId}/workspaces/${w.id}`}>
                                                        Open
                                                    </Link>
                                                </Button>
                                                <Button asChild size="sm">
                                                    <a
                                                        href={`http://localhost:5173/?projectId=${params.projectId}&workspaceId=${w.id}`}
                                                        target="_blank"
                                                        rel="noreferrer"
                                                    >
                                                        Studio
                                                    </a>
                                                </Button>
                                                {w.type !== 'MAIN' && (
                                                    <Button 
                                                        size="sm" 
                                                        variant="ghost"
                                                        onClick={() => handleDeleteWorkspace(w.id)}
                                                        disabled={deletingId === w.id}
                                                        className="text-red-600 hover:text-red-700"
                                                    >
                                                        <Trash2 className="h-4 w-4" />
                                                    </Button>
                                                )}
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

