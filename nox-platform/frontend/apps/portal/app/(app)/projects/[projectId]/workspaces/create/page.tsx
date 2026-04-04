'use client';

import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader, Alert } from "../../../../../_components/UiBits";
import { useState } from "react";
import { useRouter } from "next/navigation";

export default function CreateWorkspacePage({ params }: { params: { projectId: string } }) {
    const router = useRouter();
    const [formData, setFormData] = useState({
        name: '',
        type: 'SANDBOX'
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        if (!formData.name.trim()) {
            setError('Workspace name is required');
            setLoading(false);
            return;
        }

        try {
            const response = await fetch(`/api/v1/projects/${params.projectId}/workspaces`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: formData.name,
                    type: formData.type,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to create workspace');
            }

            const data = await response.json();
            // Redirect to workspaces list or to the new workspace
            router.push(`/projects/${params.projectId}/workspaces`);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-6">
            <PageHeader title="Create workspace" subtitle="Add a new environment inside this project." />

            {error && (
                <Alert type="error" title="Error" message={error} />
            )}

            <div className="max-w-xl">
                <Card title="Workspace details">
                    <form onSubmit={handleSubmit} className="space-y-3">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Workspace name</label>
                            <Input 
                                name="name"
                                placeholder="Main workspace" 
                                value={formData.name}
                                onChange={handleInputChange}
                                disabled={loading}
                                required 
                            />
                        </div>

                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Workspace type</label>
                            <select
                                name="type"
                                value={formData.type}
                                onChange={handleInputChange}
                                disabled={loading}
                                className="w-full rounded-md border border-zinc-800 bg-zinc-950/50 px-3 py-2 text-sm text-zinc-100"
                            >
                                <option value="MAIN">Main</option>
                                <option value="SANDBOX">Sandbox</option>
                                <option value="EXPERIMENT">Experiment</option>
                            </select>
                        </div>

                        <div className="flex items-center justify-end gap-2 pt-2">
                            <Button asChild variant="outline" disabled={loading}>
                                <Link href={`/projects/${params.projectId}/workspaces`}>Cancel</Link>
                            </Button>
                            <Button type="submit" disabled={loading}>
                                {loading ? 'Creating...' : 'Create workspace'}
                            </Button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
}

