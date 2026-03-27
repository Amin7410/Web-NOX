'use client';

import { useEffect, useState } from "react";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../../_components/UiBits";
import { Loader2, Save } from "lucide-react";
import { useRouter } from "next/navigation";

export default function OrganizationSettingsPage({ params }: { params: { orgId: string } }) {
    const router = useRouter();
    const [name, setName] = useState("");
    const [slug, setSlug] = useState("");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        fetch(`/api/orgs/${params.orgId}`)
            .then(res => res.json())
            .then(data => {
                if (data.data) {
                    setName(data.data.name);
                    setSlug(data.data.slug);
                }
            })
            .catch(err => console.error(err))
            .finally(() => setLoading(false));
    }, [params.orgId]);

    const handleSave = async (e: React.FormEvent) => {
        e.preventDefault();
        setSaving(true);
        try {
            const res = await fetch(`/api/orgs/${params.orgId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name, slug }),
            });
            if (res.ok) {
                alert("Settings updated!");
                router.refresh();
            } else {
                alert("Failed to update settings");
            }
        } catch (error) {
            alert("Network error");
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="flex h-[400px] items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-[#4F46E5]" />
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <PageHeader title="Organization settings" subtitle={`Organization ID: ${params.orgId}`} />

            <div className="max-w-2xl space-y-6">
                <Card title="General" description="Update your organization's basic information.">
                    <form className="space-y-3" onSubmit={handleSave}>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Organization name</label>
                            <Input 
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="Acme Inc" 
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Slug</label>
                            <Input 
                                value={slug}
                                onChange={(e) => setSlug(e.target.value)}
                                placeholder="acme" 
                            />
                        </div>
                        <div className="flex justify-end pt-2">
                            <Button type="submit" disabled={saving}>
                                {saving ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Save className="mr-2 h-4 w-4" />}
                                Save changes
                            </Button>
                        </div>
                    </form>
                </Card>

                <Card
                    title="Advanced"
                    description="Optional settings can be exposed from organization.settings (JSONB) later."
                >
                    <div className="text-sm text-zinc-400">No advanced settings configured yet.</div>
                </Card>
            </div>
        </div>
    );
}
