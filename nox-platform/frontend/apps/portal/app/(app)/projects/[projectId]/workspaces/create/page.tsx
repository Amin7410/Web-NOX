import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../../../_components/UiBits";

export default function CreateWorkspacePage({ params }: { params: { projectId: string } }) {
    return (
        <div className="space-y-6">
            <PageHeader title="Create workspace" subtitle="Add a new environment inside this project." />

            <div className="max-w-xl">
                <Card title="Workspace details">
                    <form className="space-y-3">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Workspace name</label>
                            <Input placeholder="Main workspace" required />
                        </div>

                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Workspace type</label>
                            <Input placeholder="MAIN / SANDBOX / EXPERIMENT" />
                        </div>

                        <div className="flex items-center justify-end gap-2 pt-2">
                            <Button asChild variant="outline">
                                <Link href={`/projects/${params.projectId}/workspaces`}>Cancel</Link>
                            </Button>
                            <Button type="submit">Create workspace</Button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
}

