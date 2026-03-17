import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../_components/UiBits";

export default function ProjectsPage() {
    const items = [
        {
            id: "p_1",
            name: "Payment Engine",
            slug: "payment-engine",
            org: "Acme Inc",
            visibility: "ORG",
            status: "ACTIVE",
            updatedAt: "2026-03-16",
        },
    ];

    return (
        <div className="space-y-6">
            <PageHeader
                title="Projects"
                subtitle="Browse and manage all your projects."
                actions={
                    <Button asChild>
                        <Link href="/projects/create">New project</Link>
                    </Button>
                }
            />

            <Card title="Filters" description="Placeholder filters — wire to API later.">
                <div className="grid gap-3 md:grid-cols-4">
                    <Input placeholder="Organization (All)" />
                    <Input placeholder="Visibility (All)" />
                    <Input placeholder="Status (All)" />
                    <Input placeholder="Search by name or slug..." />
                </div>
            </Card>

            {items.length === 0 ? (
                <Card title="No projects yet" description="Create your first project to get started.">
                    <Button asChild>
                        <Link href="/projects/create">Create project</Link>
                    </Button>
                </Card>
            ) : (
                <Card title="Projects">
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead className="text-left text-zinc-400">
                                <tr className="border-b border-white/10">
                                    <th className="py-2 pr-4">Name</th>
                                    <th className="py-2 pr-4">Organization</th>
                                    <th className="py-2 pr-4">Visibility</th>
                                    <th className="py-2 pr-4">Status</th>
                                    <th className="py-2 pr-4">Updated</th>
                                    <th className="py-2 text-right">Open</th>
                                </tr>
                            </thead>
                            <tbody>
                                {items.map((p) => (
                                    <tr key={p.id} className="border-b border-white/10">
                                        <td className="py-3 pr-4">
                                            <div className="font-medium">{p.name}</div>
                                            <div className="text-xs text-zinc-500">{p.slug}</div>
                                        </td>
                                        <td className="py-3 pr-4">{p.org}</td>
                                        <td className="py-3 pr-4">{p.visibility}</td>
                                        <td className="py-3 pr-4">{p.status}</td>
                                        <td className="py-3 pr-4">{p.updatedAt}</td>
                                        <td className="py-3 text-right">
                                            <Button asChild size="sm" variant="outline">
                                                <Link href={`/projects/${p.id}`}>Open</Link>
                                            </Button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </Card>
            )}
        </div>
    );
}

