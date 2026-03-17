import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";

export default function AccountProfilePage() {
    return (
        <div className="space-y-6">
            <PageHeader title="Profile" subtitle="Manage your personal information." />

            <Card title="Personal information" description="Update your name and review account details.">
                <div className="grid gap-4 md:grid-cols-2">
                    <div className="space-y-1">
                        <label className="text-sm text-zinc-200">Full name</label>
                        <Input defaultValue="John Doe" />
                    </div>

                    <div className="space-y-1">
                        <label className="text-sm text-zinc-200">Email</label>
                        <Input defaultValue="user@example.com" readOnly />
                        <div className="text-xs text-zinc-500">Verified</div>
                    </div>

                    <div className="space-y-1 md:col-span-2">
                        <label className="text-sm text-zinc-200">Account status</label>
                        <div className="text-sm text-zinc-300">Active</div>
                    </div>
                </div>

                <div className="mt-5 flex items-center justify-end gap-2">
                    <Button variant="outline">Cancel</Button>
                    <Button>Save changes</Button>
                </div>
            </Card>
        </div>
    );
}

