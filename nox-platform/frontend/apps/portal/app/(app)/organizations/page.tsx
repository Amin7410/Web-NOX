"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../_components/UiBits";
import { api } from "../../_lib/api";

interface Organization {
    id: string;
    name: string;
    slug: string;
    createdAt: string;
}

export default function OrganizationsPage() {
    const [orgs, setOrgs] = useState<Organization[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        // Backend doesn't have a "list my orgs" endpoint yet,
        // so we retrieve org IDs from localStorage (stored on login/join)
        const storedOrgIds: string[] = JSON.parse(localStorage.getItem("org_ids") || "[]");

        if (storedOrgIds.length === 0) {
            setLoading(false);
            return;
        }

        Promise.all(storedOrgIds.map((id) => api.get<Organization>(`/orgs/${id}`)))
            .then(setOrgs)
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, []);

    return (
        <div className="space-y-6">
            <PageHeader
                title="Organizations"
                subtitle="Manage your teams and workspaces."
                actions={
                    <Button asChild>
                        <Link href="/organizations/create">New organization</Link>
                    </Button>
                }
            />

            {loading && (
                <div className="flex items-center justify-center py-12">
                    <div className="h-6 w-6 animate-spin rounded-full border-2 border-blue-500/30 border-t-blue-500" />
                </div>
            )}

            {error && (
                <div className="rounded-xl border border-red-500/20 bg-red-500/5 p-4 text-sm text-red-400">
                    {error}
                </div>
            )}

            {!loading && !error && orgs.length === 0 && (
                <Card title="No organizations yet" description="Create your first organization to get started.">
                    <Button asChild>
                        <Link href="/organizations/create">Create organization</Link>
                    </Button>
                </Card>
            )}

            {orgs.length > 0 && (
                <div className="grid gap-4 md:grid-cols-2">
                    {orgs.map((org) => (
                        <Link
                            key={org.id}
                            href={`/organizations/${org.id}`}
                            className="rounded-2xl border border-white/10 bg-white/5 p-5 hover:bg-white/10 transition"
                        >
                            <div className="flex items-start justify-between gap-3">
                                <div>
                                    <div className="text-base font-semibold">{org.name}</div>
                                    <div className="mt-1 text-sm text-zinc-400">@{org.slug}</div>
                                </div>
                                <div className="rounded-full border border-white/10 bg-black/20 px-2 py-1 text-xs text-zinc-200">
                                    Member
                                </div>
                            </div>
                            <div className="mt-4 text-xs text-zinc-500">
                                Created {new Date(org.createdAt).toLocaleDateString()}
                            </div>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}
