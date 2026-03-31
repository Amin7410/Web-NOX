"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";
import { api } from "../../../_lib/api";

interface Organization {
    id: string;
    name: string;
    slug: string;
    createdAt: string;
    updatedAt: string;
}

export default function OrganizationDetailPage({ params }: { params: { orgId: string } }) {
    const router = useRouter();
    const { orgId } = params;

    const [org, setOrg] = useState<Organization | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        api.get<Organization>(`/orgs/${orgId}`)
            .then(setOrg)
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, [orgId]);

    const handleDelete = async () => {
        if (!confirm(`Are you sure you want to delete "${org?.name}"? This cannot be undone.`)) return;

        try {
            await api.delete(`/orgs/${orgId}`);

            // Remove from localStorage
            const existing: string[] = JSON.parse(localStorage.getItem("org_ids") || "[]");
            localStorage.setItem("org_ids", JSON.stringify(existing.filter((id) => id !== orgId)));

            router.push("/organizations");
        } catch (err: any) {
            setError(err.message || "Failed to delete organization.");
        }
    };

    if (loading) {
        return (
            <div className="flex items-center justify-center py-24">
                <div className="h-6 w-6 animate-spin rounded-full border-2 border-blue-500/30 border-t-blue-500" />
            </div>
        );
    }

    if (error) {
        return (
            <div className="rounded-xl border border-red-500/20 bg-red-500/5 p-4 text-sm text-red-400">
                {error}
            </div>
        );
    }

    if (!org) return null;

    return (
        <div className="space-y-6">
            <PageHeader
                title={org.name}
                subtitle={`@${org.slug}`}
                actions={
                    <div className="flex gap-2">
                        <Button asChild variant="outline">
                            <Link href={`/organizations/${orgId}/settings`}>Settings</Link>
                        </Button>
                        <Button variant="outline" onClick={handleDelete} className="text-red-400 hover:text-red-300 border-red-500/20 hover:border-red-400/30">
                            Delete
                        </Button>
                    </div>
                }
            />

            <div className="grid gap-4 md:grid-cols-3">
                <Card title="Created">
                    <div className="text-base font-semibold">{new Date(org.createdAt).toLocaleDateString()}</div>
                    <div className="mt-1 text-sm text-zinc-400">Creation date</div>
                </Card>
                <Card title="Last updated">
                    <div className="text-base font-semibold">{new Date(org.updatedAt).toLocaleDateString()}</div>
                    <div className="mt-1 text-sm text-zinc-400">Last modification</div>
                </Card>
                <Card title="Slug">
                    <div className="text-base font-mono font-semibold">@{org.slug}</div>
                    <div className="mt-1 text-sm text-zinc-400">Used in URLs</div>
                </Card>
            </div>

            <Card title="Quick links">
                <div className="flex flex-wrap gap-2">
                    <Button asChild variant="outline">
                        <Link href={`/organizations/${orgId}/members`}>Members</Link>
                    </Button>
                    <Button asChild variant="outline">
                        <Link href={`/organizations/${orgId}/roles`}>Roles</Link>
                    </Button>
                    <Button asChild variant="outline">
                        <Link href={`/projects?orgId=${orgId}`}>Projects</Link>
                    </Button>
                </div>
            </Card>
        </div>
    );
}
