'use client';

import { Button, Input } from "@nox/ui";
import { Card, PageHeader, Alert } from "../../../../_components/UiBits";
import { useState, useEffect } from "react";

export default function ProjectSettingsPage({ params }: { params: { projectId: string } }) {
    const [general, setGeneral] = useState({
        name: '',
        slug: '',
        description: ''
    });
    const [access, setAccess] = useState({
        visibility: 'organization',
        status: 'active'
    });
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const [activeForm, setActiveForm] = useState<'general' | 'access' | null>(null);

    useEffect(() => {
        const fetchProject = async () => {
            try {
                const response = await fetch(`/api/v1/projects/${params.projectId}`, {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });

                if (response.ok) {
                    const data = await response.json();
                    const project = data.data;
                    setGeneral({
                        name: project.name || '',
                        slug: project.slug || '',
                        description: project.description || ''
                    });
                    setAccess({
                        visibility: project.visibility || 'organization',
                        status: project.status || 'active'
                    });
                } else {
                    setError('Failed to load project settings');
                }
            } catch (err) {
                console.error('Failed to fetch project:', err);
                setError('An error occurred while loading project settings');
            } finally {
                setLoading(false);
            }
        };

        fetchProject();
    }, [params.projectId]);

    const handleGeneralChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target;
        setGeneral(prev => ({ ...prev, [name]: value }));
    };

    const handleAccessChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setAccess(prev => ({ ...prev, [name]: value }));
    };

    const handleSaveGeneral = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setSuccess(false);

        try {
            const response = await fetch(`/api/v1/projects/${params.projectId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: general.name,
                    slug: general.slug,
                    description: general.description,
                    visibility: access.visibility,
                    status: access.status,
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to update project');
            }

            setSuccess(true);
            setActiveForm(null);
            setTimeout(() => setSuccess(false), 5000);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setSaving(false);
        }
    };

    const handleSaveAccess = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setSuccess(false);

        try {
            const response = await fetch(`/api/v1/projects/${params.projectId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: general.name,
                    slug: general.slug,
                    description: general.description,
                    visibility: access.visibility,
                    status: access.status,
                }),
            });

            if (!response.ok) {
                throw new Error('Failed to update access settings');
            }

            setSuccess(true);
            setActiveForm(null);
            setTimeout(() => setSuccess(false), 5000);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="space-y-6">
                <PageHeader title="Project settings" subtitle={`Project ID: ${params.projectId}`} />
                <div className="text-zinc-400">Loading project settings...</div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <PageHeader title="Project settings" subtitle={`Project ID: ${params.projectId}`} />

            {error && (
                <Alert type="error" title="Error" message={error} />
            )}
            
            {success && (
                <Alert type="success" title="Success" message="Settings updated successfully" />
            )}

            <div className="max-w-2xl space-y-6">
                <Card title="General" description="Update your project's basic information.">
                    <form onSubmit={handleSaveGeneral} className="space-y-3">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Project name</label>
                            <Input 
                                name="name"
                                value={general.name}
                                onChange={handleGeneralChange}
                                disabled={saving}
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Slug</label>
                            <Input 
                                name="slug"
                                value={general.slug}
                                onChange={handleGeneralChange}
                                disabled={saving}
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Description</label>
                            <textarea
                                name="description"
                                className="min-h-24 w-full rounded-md border border-zinc-800 bg-zinc-950/50 px-3 py-2 text-sm text-zinc-100 placeholder:text-zinc-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/30"
                                value={general.description}
                                onChange={handleGeneralChange}
                                disabled={saving}
                            />
                        </div>
                        <div className="flex justify-end pt-2">
                            <Button type="submit" disabled={saving}>
                                {saving ? 'Saving...' : 'Save general settings'}
                            </Button>
                        </div>
                    </form>
                </Card>

                <Card title="Access & visibility" description="Control who can view this project and its state.">
                    <form onSubmit={handleSaveAccess} className="grid gap-3 md:grid-cols-2">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Visibility</label>
                            <select
                                name="visibility"
                                value={access.visibility}
                                onChange={(e) => setAccess(prev => ({ ...prev, visibility: e.target.value }))}
                                disabled={saving}
                                className="w-full rounded-md border border-zinc-800 bg-zinc-950/50 px-3 py-2 text-sm text-zinc-100"
                            >
                                <option value="organization">Organization</option>
                                <option value="team">Team</option>
                                <option value="private">Private</option>
                            </select>
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Status</label>
                            <select
                                name="status"
                                value={access.status}
                                onChange={(e) => setAccess(prev => ({ ...prev, status: e.target.value }))}
                                disabled={saving}
                                className="w-full rounded-md border border-zinc-800 bg-zinc-950/50 px-3 py-2 text-sm text-zinc-100"
                            >
                                <option value="active">Active</option>
                                <option value="archived">Archived</option>
                            </select>
                        </div>
                        <div className="md:col-span-2 flex justify-end pt-2">
                            <Button type="submit" disabled={saving}>
                                {saving ? 'Saving...' : 'Save access settings'}
                            </Button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
}

