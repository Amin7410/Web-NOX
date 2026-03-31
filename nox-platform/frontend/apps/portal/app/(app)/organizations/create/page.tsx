"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";
import { api } from "../../../_lib/api";

interface OrganizationResponse {
    id: string;
    name: string;
    slug: string;
}

export default function CreateOrganizationPage() {
    const router = useRouter();
    const [name, setName] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const org = await api.post<OrganizationResponse>("/orgs", { name });

            // Save org ID to localStorage so list page can retrieve it
            const existing: string[] = JSON.parse(localStorage.getItem("org_ids") || "[]");
            if (!existing.includes(org.id)) {
                localStorage.setItem("org_ids", JSON.stringify([...existing, org.id]));
            }

            router.push(`/organizations/${org.id}`);
        } catch (err: any) {
            setError(err.message || "Failed to create organization.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-6">
            <PageHeader title="Create organization" subtitle="Set up a new team for your projects." />

            <div className="max-w-xl">
                <Card title="Organization details">
                    {error && (
                        <div className="mb-4 rounded-lg border border-red-500/20 bg-red-500/5 p-3 text-sm text-red-400">
                            {error}
                        </div>
                    )}
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Organization name</label>
                            <Input
                                placeholder="Acme Inc"
                                required
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                            />
                            <p className="text-xs text-zinc-500">
                                Choose a name for your team or company.
                            </p>
                        </div>

                        <div className="flex items-center justify-end gap-2 pt-2">
                            <Button asChild variant="outline">
                                <Link href="/organizations">Cancel</Link>
                            </Button>
                            <Button type="submit" disabled={loading}>
                                {loading ? "Creating..." : "Create organization"}
                            </Button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
}
