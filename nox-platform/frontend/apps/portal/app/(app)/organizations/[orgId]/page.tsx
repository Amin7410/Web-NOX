'use client';

import Link from "next/link";
import { useEffect, useState } from "react";
import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";
import { Loader2 } from "lucide-react";

export default function OrganizationDashboardPage({ params }: { params: { orgId: string } }) {
    const [org, setOrg] = useState<any>(null);
    const [memberCount, setMemberCount] = useState<number | string>("—");
    const [projectCount, setProjectCount] = useState<number | string>("—");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            if (params.orgId.startsWith('mock-')) {
                setOrg({
                    id: params.orgId,
                    name: "NOX Default Team",
                    slug: "nox-default",
                    createdAt: new Date().toISOString()
                });
                setMemberCount(1);
                setProjectCount(3);
                setLoading(false);
                return;
            }

            try {
                // Fetch Org Details
                const orgRes = await fetch(`/api/orgs/${params.orgId}`);
                const orgData = await orgRes.json();
                if (orgData.data) setOrg(orgData.data);

                // Fetch Member Count
                const membersRes = await fetch(`/api/orgs/${params.orgId}/members`);
                const membersData = await membersRes.json();
                if (membersData.data?.totalElements !== undefined) {
                    setMemberCount(membersData.data.totalElements);
                }

                // Fetch Project Count
                const projectsRes = await fetch(`/api/projects?orgId=${params.orgId}`);
                const projectsData = await projectsRes.json();
                if (Array.isArray(projectsData.data)) {
                    setProjectCount(projectsData.data.length);
                } else if (projectsData.data?.totalElements !== undefined) {
                    setProjectCount(projectsData.data.totalElements);
                }
            } catch (error) {
                console.error("Dashboard fetch error:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [params.orgId]);

    if (loading) {
        return (
            <div className="flex h-[400px] items-center justify-center">
                <Loader2 className="h-8 w-8 animate-spin text-[#4F46E5]" />
            </div>
        );
    }

    if (!org) {
        return (
            <div className="py-20 text-center">
                <h2 className="text-xl font-semibold">Organization not found</h2>
                <Button asChild variant="ghost" className="mt-2">
                    <Link href="/organizations">Back to organizations</Link>
                </Button>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <PageHeader
                title={org.name}
                subtitle={`@${org.slug}`}
                actions={
                    <Button asChild variant="outline">
                        <Link href={`/organizations/${org.id}/settings`}>Settings</Link>
                    </Button>
                }
            />

            <div className="grid gap-4 md:grid-cols-3">
                <Card title="Members">
                    <div className="text-3xl font-bold text-gray-900 dark:text-zinc-100">{memberCount === "—" ? 1 : memberCount}</div>
                    <div className="mt-1 text-sm text-gray-500 dark:text-zinc-400">People in this organization</div>
                </Card>
                <Card title="Projects">
                    <div className="text-3xl font-bold text-gray-900 dark:text-zinc-100">{projectCount === "—" ? 0 : projectCount}</div>
                    <div className="mt-1 text-sm text-gray-500 dark:text-zinc-400">Projects owned by this organization</div>
                </Card>
                <Card title="Created">
                    <div className="text-2xl font-bold text-gray-900 dark:text-zinc-100">
                        {org.createdAt ? new Date(org.createdAt).toLocaleDateString() : "Today"}
                    </div>
                    <div className="mt-1 text-sm text-gray-500 dark:text-zinc-400">Creation date</div>
                </Card>
            </div>

            <Card title="Quick links">
                <div className="flex flex-wrap gap-2">
                    <Button asChild variant="outline">
                        <Link href={`/organizations/${org.id}/members`}>Members</Link>
                    </Button>
                    <Button asChild variant="outline">
                        <Link href={`/organizations/${org.id}/roles`}>Roles</Link>
                    </Button>
                    <Button asChild variant="outline">
                        <Link href={`/projects?orgId=${org.id}`}>Projects</Link>
                    </Button>
                </div>
            </Card>
        </div>
    );
}

