import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../../_components/UiBits";

export default function ProjectSettingsPage({ params }: { params: { projectId: string } }) {
    return (
        <div className="space-y-6">
            <PageHeader title="Project settings" subtitle={`Project ID: ${params.projectId}`} />

            <div className="max-w-2xl space-y-6">
                <Card title="General" description="Update your project's basic information.">
                    <form className="space-y-3">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Project name</label>
                            <Input defaultValue="Payment Engine" />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Slug</label>
                            <Input defaultValue="payment-engine" />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Description</label>
                            <textarea
                                className="min-h-24 w-full rounded-md border border-zinc-800 bg-zinc-950/50 px-3 py-2 text-sm text-zinc-100 placeholder:text-zinc-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/30"
                                defaultValue="Core payment processing logic"
                            />
                        </div>
                        <div className="flex justify-end pt-2">
                            <Button type="submit">Save general settings</Button>
                        </div>
                    </form>
                </Card>

                <Card title="Access & visibility" description="Control who can view this project and its state.">
                    <form className="grid gap-3 md:grid-cols-2">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Visibility</label>
                            <Input defaultValue="Organization" />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Status</label>
                            <Input defaultValue="Active" />
                        </div>
                        <div className="md:col-span-2 flex justify-end pt-2">
                            <Button type="submit">Save access settings</Button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
}

