import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";

export default function AccountSessionsPage() {
    return (
        <div className="space-y-6">
            <PageHeader
                title="Sessions"
                subtitle="Review and manage your active sign-in sessions."
                actions={<Button variant="outline">Sign out from all other devices</Button>}
            />

            <Card title="Active sessions" description="This is placeholder data until API integration is added.">
                <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                        <thead className="text-left text-zinc-400">
                            <tr className="border-b border-white/10">
                                <th className="py-2 pr-4">Device</th>
                                <th className="py-2 pr-4">IP</th>
                                <th className="py-2 pr-4">Last active</th>
                                <th className="py-2 pr-4">Created</th>
                                <th className="py-2 text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr className="border-b border-white/10">
                                <td className="py-3 pr-4">Chrome on Windows (current)</td>
                                <td className="py-3 pr-4">192.168.1.10</td>
                                <td className="py-3 pr-4">Just now</td>
                                <td className="py-3 pr-4">2026-03-16</td>
                                <td className="py-3 text-right">
                                    <Button size="sm" variant="outline" disabled>
                                        Sign out
                                    </Button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </Card>
        </div>
    );
}

