import Link from "next/link";
import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../../../_components/UiBits";

export default function WorkspacesPage({ params }: { params: { projectId: string } }) {
    const workspaces = [
        { id: "w_1", name: "Main workspace", type: "MAIN", createdAt: "2026-03-16" },
        { id: "w_2", name: "Experiment A", type: "EXPERIMENT", createdAt: "2026-03-16" },
    ];

    return (
        <div className="space-y-6">
            <PageHeader
                title="Workspaces"
                subtitle="Different environments inside this project."
                actions={
                    <Button asChild>
                        <Link href={`/projects/${params.projectId}/workspaces/create`}>New workspace</Link>
                    </Button>
                }
            />

            {workspaces.length === 0 ? (
                <Card title="No workspaces yet" description="Create a workspace to start designing in Studio.">
                    <Button asChild>
                        <Link href={`/projects/${params.projectId}/workspaces/create`}>Create workspace</Link>
                    </Button>
                </Card>
            ) : (
                <Card title="Workspaces">
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead className="text-left text-zinc-400">
                                <tr className="border-b border-white/10">
                                    <th className="py-2 pr-4">Name</th>
                                    <th className="py-2 pr-4">Type</th>
                                    <th className="py-2 pr-4">Created</th>
                                    <th className="py-2 text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {workspaces.map((w) => (
                                    <tr key={w.id} className="border-b border-white/10">
                                        <td className="py-3 pr-4">
                                            <div className="font-medium">{w.name}</div>
                                        </td>
                                        <td className="py-3 pr-4">{w.type}</td>
                                        <td className="py-3 pr-4">{w.createdAt}</td>
                                        <td className="py-3 text-right">
                                            <div className="inline-flex gap-2">
                                                <Button asChild size="sm" variant="outline">
                                                    <Link href={`/projects/${params.projectId}/workspaces/${w.id}`}>
                                                        Open
                                                    </Link>
                                                </Button>
                                                <Button asChild size="sm">
                                                    <a
                                                        href={`http://localhost:5173/?projectId=${params.projectId}&workspaceId=${w.id}`}
                                                        target="_blank"
                                                        rel="noreferrer"
                                                    >
                                                        Open in Studio
                                                    </a>
                                                </Button>
                                            </div>
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

