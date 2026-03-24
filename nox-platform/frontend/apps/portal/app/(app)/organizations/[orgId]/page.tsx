import Link from "next/link";
import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";

export default function OrganizationDashboardPage({ params }: { params: { orgId: string } }) {
    const org = { id: params.orgId, name: "Acme Inc", slug: "acme" };

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
                    <div className="text-2xl font-semibold">10</div>
                    <div className="mt-1 text-sm text-zinc-400">People in this organization</div>
                </Card>
                <Card title="Projects">
                    <div className="text-2xl font-semibold">3</div>
                    <div className="mt-1 text-sm text-zinc-400">Projects owned by this organization</div>
                </Card>
                <Card title="Created">
                    <div className="text-2xl font-semibold">—</div>
                    <div className="mt-1 text-sm text-zinc-400">Creation date</div>
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

