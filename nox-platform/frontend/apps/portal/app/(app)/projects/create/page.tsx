import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";

export default function CreateProjectPage() {
    return (
        <div className="space-y-6">
            <PageHeader title="Create project" subtitle="Create a new project in an organization." />

            <div className="max-w-2xl space-y-6">
                <Card title="Project details">
                    <form className="space-y-3">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Organization</label>
                            <Input placeholder="Select organization..." />
                        </div>

                        <div className="grid gap-3 md:grid-cols-2">
                            <div className="space-y-1">
                                <label className="text-sm text-zinc-200">Project name</label>
                                <Input placeholder="Payment Engine" required />
                            </div>
                            <div className="space-y-1">
                                <label className="text-sm text-zinc-200">Slug</label>
                                <Input placeholder="payment-engine" required />
                            </div>
                        </div>

                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Description</label>
                            <textarea
                                className="min-h-24 w-full rounded-md border border-zinc-800 bg-zinc-950/50 px-3 py-2 text-sm text-zinc-100 placeholder:text-zinc-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/30"
                                placeholder="Optional description..."
                            />
                        </div>

                        <div className="grid gap-3 md:grid-cols-2">
                            <div className="space-y-1">
                                <label className="text-sm text-zinc-200">Visibility</label>
                                <Input placeholder="Private / Organization / Public" />
                            </div>
                            <div className="space-y-1">
                                <label className="text-sm text-zinc-200">Status</label>
                                <Input placeholder="Active / Draft / Archived" />
                            </div>
                        </div>

                        <div className="flex items-center justify-end gap-2 pt-2">
                            <Button asChild variant="outline">
                                <Link href="/projects">Cancel</Link>
                            </Button>
                            <Button type="submit">Create project</Button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
}

