import Link from "next/link";
import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../_components/UiBits";

export default function OrganizationsPage() {
    const items: Array<{ id: string; name: string; slug: string; role: "Owner" | "Member"; memberCount?: number }> = [
        { id: "org_1", name: "Acme Inc", slug: "acme", role: "Owner", memberCount: 10 },
    ];

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

            {items.length === 0 ? (
                <Card title="No organizations yet" description="Create your first organization to get started.">
                    <Button asChild>
                        <Link href="/organizations/create">Create organization</Link>
                    </Button>
                </Card>
            ) : (
                <div className="grid gap-4 md:grid-cols-2">
                    {items.map((org) => (
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
                                    {org.role}
                                </div>
                            </div>
                            <div className="mt-4 text-sm text-zinc-400">
                                Members: {org.memberCount ?? "—"}
                            </div>
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}

