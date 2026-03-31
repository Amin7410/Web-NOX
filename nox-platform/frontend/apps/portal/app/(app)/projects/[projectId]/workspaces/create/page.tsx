"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../../../_components/UiBits";
import { api } from "../../../../../_lib/api";

export default function CreateWorkspacePage({ params }: { params: { projectId: string } }) {
    const router = useRouter();
    const [name, setName] = useState("");
    const [type, setType] = useState("MAIN");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            await api.post(`/projects/${params.projectId}/workspaces`, {
                name,
                type,
                description: `Workspace for project ${params.projectId}`
            });
            router.push(`/projects/${params.projectId}/workspaces`);
            router.refresh();
        } catch (err: any) {
            setError(err.message || "Failed to create workspace");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-6">
            <PageHeader title="Create workspace" subtitle="Add a new environment inside this project." />

            <div className="max-w-xl">
                <Card title="Workspace details">
                    <form onSubmit={handleSubmit} className="space-y-4">
                        {error && (
                            <div className="p-3 bg-red-500/10 border border-red-500/20 text-red-400 text-sm rounded-md">
                                {error}
                            </div>
                        )}

                        <div className="space-y-1.5">
                            <label className="text-sm font-medium text-zinc-300">Workspace name</label>
                            <Input 
                                placeholder="e.g. Production, Staging, Experiment" 
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                required 
                                disabled={loading}
                            />
                        </div>

                        <div className="space-y-1.5">
                            <label className="text-sm font-medium text-zinc-300">Workspace type</label>
                            <select 
                                className="w-full bg-zinc-950 border border-white/10 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:opacity-50"
                                value={type}
                                onChange={(e) => setType(e.target.value)}
                                disabled={loading}
                            >
                                <option value="MAIN">MAIN</option>
                                <option value="SANDBOX">SANDBOX</option>
                                <option value="EXPERIMENT">EXPERIMENT</option>
                            </select>
                        </div>

                        <div className="flex items-center justify-end gap-3 pt-4 border-t border-white/5">
                            <Button asChild variant="outline" type="button" disabled={loading}>
                                <Link href={`/projects/${params.projectId}/workspaces`}>Cancel</Link>
                            </Button>
                            <Button type="submit" disabled={loading}>
                                {loading ? "Creating..." : "Create workspace"}
                            </Button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
}

