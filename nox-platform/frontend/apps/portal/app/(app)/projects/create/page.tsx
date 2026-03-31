"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";
import { api } from "../../../_lib/api";

interface Organization {
    id: string;
    name: string;
}

export default function CreateProjectPage() {
    const router = useRouter();
    const [organizations, setOrganizations] = useState<Organization[]>([]);
    const [selectedOrgId, setSelectedOrgId] = useState<string>("");
    const [name, setName] = useState("");
    const [description, setDescription] = useState("");
    const [visibility, setVisibility] = useState("PRIVATE");
    const [loading, setLoading] = useState(false);
    const [fetchingOrgs, setFetchingOrgs] = useState(true);

    useEffect(() => {
        async function loadOrgs() {
            try {
                const savedIds = JSON.parse(localStorage.getItem("nox_org_ids") || "[]");
                const orgs: Organization[] = [];
                for (const id of savedIds) {
                    try {
                        const org = await api.get<Organization>(`/orgs/${id}`);
                        orgs.push(org);
                    } catch (e) {}
                }
                setOrganizations(orgs);
                
                // Set default from localStorage or first available
                const active = localStorage.getItem("active_org_id");
                if (active) {
                    setSelectedOrgId(active);
                } else if (orgs.length > 0) {
                    setSelectedOrgId(orgs[0].id);
                    localStorage.setItem("active_org_id", orgs[0].id);
                }
            } catch (e) {
                console.error("Failed to load organizations", e);
            } finally {
                setFetchingOrgs(false);
            }
        }
        loadOrgs();
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedOrgId) {
            alert("Please select an organization");
            return;
        }

        setLoading(true);
        try {
            // Ensure localStorage is synced so the header is correct
            localStorage.setItem("active_org_id", selectedOrgId);
            
            await api.post("/projects", {
                name,
                description,
                visibility,
                status: "ACTIVE"
            });
            router.push("/projects");
        } catch (error: any) {
            console.error("Failed to create project", error);
            alert(error.message || "Failed to create project. Check if you have permissions in this organization.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-6">
            <PageHeader title="Create project" subtitle="Create a new project in an organization." />

            <div className="max-w-2xl space-y-6">
                <Card title="Project details">
                    <form className="space-y-4" onSubmit={handleSubmit}>
                        <div className="space-y-1">
                            <label className="text-sm font-medium text-zinc-300">Organization</label>
                            {fetchingOrgs ? (
                                <div className="text-xs text-zinc-500 italic">Loading organizations...</div>
                            ) : (
                                <select 
                                    className="w-full rounded-md border border-zinc-800 bg-zinc-950 px-3 py-2 text-sm text-zinc-100 focus:outline-none focus:ring-2 focus:ring-blue-500/30"
                                    value={selectedOrgId}
                                    onChange={(e) => setSelectedOrgId(e.target.value)}
                                    required
                                >
                                    <option value="" disabled>Select an organization</option>
                                    {organizations.map(org => (
                                        <option key={org.id} value={org.id}>{org.name}</option>
                                    ))}
                                </select>
                            )}
                        </div>

                        <div className="space-y-1">
                            <label className="text-sm font-medium text-zinc-300">Project name</label>
                            <Input 
                                placeholder="e.g. Acme Billing System" 
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                required 
                            />
                        </div>

                        <div className="space-y-1">
                            <label className="text-sm font-medium text-zinc-300">Description</label>
                            <textarea
                                className="min-h-24 w-full rounded-md border border-zinc-800 bg-zinc-950 px-3 py-2 text-sm text-zinc-100 placeholder:text-zinc-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/30"
                                placeholder="What is this project about?"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                            />
                        </div>

                        <div className="space-y-1">
                            <label className="text-sm font-medium text-zinc-300">Visibility</label>
                            <select 
                                className="w-full rounded-md border border-zinc-800 bg-zinc-950 px-3 py-2 text-sm text-zinc-100 focus:outline-none focus:ring-2 focus:ring-blue-500/30"
                                value={visibility}
                                onChange={(e) => setVisibility(e.target.value)}
                            >
                                <option value="PRIVATE">Private (Only you)</option>
                                <option value="ORGANIZATION">Organization (All members)</option>
                                <option value="PUBLIC">Public (Everyone)</option>
                            </select>
                        </div>

                        <div className="flex items-center justify-end gap-2 pt-4">
                            <Button asChild variant="outline" type="button">
                                <Link href="/projects">Cancel</Link>
                            </Button>
                            <Button type="submit" disabled={loading || !selectedOrgId}>
                                {loading ? "Creating..." : "Create project"}
                            </Button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
}

