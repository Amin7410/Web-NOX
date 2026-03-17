import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../../_components/UiBits";

export default function OrganizationRolesPage({ params }: { params: { orgId: string } }) {
    const roles = [
        { id: "r_1", name: "Admin", level: 10, permissions: ["project:read", "project:write", "member:manage"] },
        { id: "r_2", name: "Member", level: 1, permissions: ["project:read"] },
    ];

    return (
        <div className="space-y-6">
            <PageHeader
                title="Roles"
                subtitle="Define what members can do in this organization."
                actions={<Button variant="outline">New role</Button>}
            />

            <Card title="Create/Edit role (mock panel)" description="Replace with a modal or side panel in implementation.">
                <div className="grid gap-3 md:grid-cols-3">
                    <div className="space-y-1 md:col-span-1">
                        <label className="text-sm text-zinc-200">Name</label>
                        <Input placeholder="Admin" />
                    </div>
                    <div className="space-y-1 md:col-span-1">
                        <label className="text-sm text-zinc-200">Level</label>
                        <Input placeholder="10" />
                    </div>
                    <div className="space-y-1 md:col-span-3">
                        <label className="text-sm text-zinc-200">Permissions</label>
                        <Input placeholder="project:read, project:write, member:manage" />
                    </div>
                </div>
                <div className="mt-3 flex justify-end">
                    <Button>Save</Button>
                </div>
                <div className="mt-2 text-xs text-zinc-500">Org: {params.orgId}</div>
            </Card>

            <Card title="Roles list">
                <div className="overflow-x-auto">
                    <table className="w-full text-sm">
                        <thead className="text-left text-zinc-400">
                            <tr className="border-b border-white/10">
                                <th className="py-2 pr-4">Name</th>
                                <th className="py-2 pr-4">Level</th>
                                <th className="py-2 pr-4">Permissions</th>
                                <th className="py-2 text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {roles.map((r) => (
                                <tr key={r.id} className="border-b border-white/10">
                                    <td className="py-3 pr-4 font-medium">{r.name}</td>
                                    <td className="py-3 pr-4">{r.level}</td>
                                    <td className="py-3 pr-4 text-zinc-300">
                                        {r.permissions.join(", ")}
                                    </td>
                                    <td className="py-3 text-right">
                                        <div className="inline-flex gap-2">
                                            <Button size="sm" variant="outline">
                                                Edit
                                            </Button>
                                            <Button size="sm" variant="destructive">
                                                Delete
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

