import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../../_components/UiBits";

export default function OrganizationSettingsPage({ params }: { params: { orgId: string } }) {
    return (
        <div className="space-y-6">
            <PageHeader title="Organization settings" subtitle={`Organization ID: ${params.orgId}`} />

            <div className="max-w-2xl space-y-6">
                <Card title="General" description="Update your organization's basic information.">
                    <form className="space-y-3">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Organization name</label>
                            <Input defaultValue="Acme Inc" />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Slug</label>
                            <Input defaultValue="acme" />
                        </div>
                        <div className="flex justify-end pt-2">
                            <Button type="submit">Save changes</Button>
                        </div>
                    </form>
                </Card>

                <Card
                    title="Advanced"
                    description="Optional settings can be exposed from organization.settings (JSONB) later."
                >
                    <div className="text-sm text-zinc-400">No advanced settings configured yet.</div>
                </Card>
            </div>
        </div>
    );
}

