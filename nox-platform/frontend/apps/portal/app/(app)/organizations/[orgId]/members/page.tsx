import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../../_components/UiBits";

export default function OrganizationMembersPage({ params }: { params: { orgId: string } }) {
    const members = [
        {
            id: "m_1",
            fullName: "John Doe",
            email: "user@example.com",
            role: "Admin",
            joinedAt: "2026-03-16",
            invitedBy: "Alice",
        },
    ];

    return (
        <div className="space-y-6">
            <PageHeader
                title="Members"
                subtitle="Manage who can access this organization."
                actions={<Button variant="outline">Invite member</Button>}
            />

            <Card title="Invite member (mock modal)" description="Replace with a real modal during implementation.">
                <div className="grid gap-3 md:grid-cols-2">
                    <div className="space-y-1">
                        <label className="text-sm text-zinc-200">Email</label>
                        <Input placeholder="person@example.com" />
                    </div>
                    <div className="space-y-1">
                        <label className="text-sm text-zinc-200">Role</label>
                        <Input placeholder="Admin / Member" />
                    </div>
                </div>
                <div className="mt-3 flex justify-end">
                    <Button>Send invite</Button>
                </div>
                <div className="mt-2 text-xs text-zinc-500">Org: {params.orgId}</div>
            </Card>

            <Card title="Members list">
                <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                        <thead className="text-left text-zinc-400">
                            <tr className="border-b border-white/10">
                                <th className="py-2 pr-4">Name</th>
                                <th className="py-2 pr-4">Email</th>
                                <th className="py-2 pr-4">Role</th>
                                <th className="py-2 pr-4">Joined</th>
                                <th className="py-2 pr-4">Invited by</th>
                                <th className="py-2 text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {members.map((m) => (
                                <tr key={m.id} className="border-b border-white/10">
                                    <td className="py-3 pr-4">{m.fullName}</td>
                                    <td className="py-3 pr-4 text-zinc-300">{m.email}</td>
                                    <td className="py-3 pr-4">{m.role}</td>
                                    <td className="py-3 pr-4">{m.joinedAt}</td>
                                    <td className="py-3 pr-4">{m.invitedBy}</td>
                                    <td className="py-3 text-right">
                                        <div className="inline-flex gap-2">
                                            <Button size="sm" variant="outline">
                                                Change role
                                            </Button>
                                            <Button size="sm" variant="destructive">
                                                Remove
                                            </Button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </Card>
        </div>
    );
}

