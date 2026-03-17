import Link from "next/link";
import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../../../../_components/UiBits";

export default function WorkspaceOverviewPage({
    params,
}: {
    params: { projectId: string; workspaceId: string };
}) {
    const workspace = {
        id: params.workspaceId,
        name: "Main workspace",
        type: "MAIN",
        createdAt: "2026-03-16",
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title={workspace.name}
                subtitle={`Type: ${workspace.type}`}
                actions={
                    <>
                        <Button asChild variant="outline">
                            <Link href={`/projects/${params.projectId}/workspaces`}>Back to workspaces</Link>
                        </Button>
                        <Button asChild>
                            <a
                                href={`http://localhost:5173/?projectId=${params.projectId}&workspaceId=${workspace.id}`}
                                target="_blank"
                                rel="noreferrer"
                            >
                                Open in Studio
                            </a>
                        </Button>
                    </>
                }
            />

            <div className="grid gap-4 md:grid-cols-2">
                <Card title="Details">
                    <div className="space-y-2 text-sm">
                        <div className="flex items-center justify-between">
                            <div className="text-zinc-400">Project</div>
                            <div className="font-mono">{params.projectId}</div>
                        </div>
                        <div className="flex items-center justify-between">
                            <div className="text-zinc-400">Workspace ID</div>
                            <div className="font-mono">{workspace.id}</div>
                        </div>
                        <div className="flex items-center justify-between">
                            <div className="text-zinc-400">Created</div>
                            <div>{workspace.createdAt}</div>
                        </div>
                    </div>
                </Card>

                <Card title="Snapshots (future)" description="Wire this section to CoreSnapshot APIs later.">
                    <div className="text-sm text-zinc-400">No snapshots configured yet.</div>
                </Card>
            </div>
        </div>
    );
}

