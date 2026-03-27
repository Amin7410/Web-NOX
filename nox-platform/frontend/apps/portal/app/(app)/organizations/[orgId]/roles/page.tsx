'use client';

import { useEffect, useState } from "react";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../../_components/UiBits";
import { Loader2, ShieldPlus, Trash2, Edit2 } from "lucide-react";

export default function OrganizationRolesPage({ params }: { params: { orgId: string } }) {
    const [roles, setRoles] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [roleForm, setRoleForm] = useState({ name: "", permissions: "", level: 1 });

    const fetchRoles = async () => {
        try {
            const res = await fetch(`/api/orgs/${params.orgId}/roles`);
            const data = await res.json();
            if (data.data) {
                setRoles(data.data);
            }
        } catch (error) {
            console.error("Failed to fetch roles:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchRoles();
    }, [params.orgId]);

    const handleSave = async () => {
        if (!roleForm.name) return;
        setSaving(true);
        try {
            const permissionsArray = roleForm.permissions.split(",").map(p => p.trim()).filter(p => p);
            const res = await fetch(`/api/orgs/${params.orgId}/roles`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ ...roleForm, permissions: permissionsArray }),
            });
            if (res.ok) {
                setRoleForm({ name: "", permissions: "", level: 1 });
                fetchRoles();
            } else {
                alert("Failed to save role");
            }
        } catch (error) {
            alert("Network error");
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (roleName: string) => {
        if (!confirm(`Are you sure you want to delete the role "${roleName}"?`)) return;
        try {
            const res = await fetch(`/api/orgs/${params.orgId}/roles?roleName=${roleName}`, {
                method: "DELETE",
            });
            if (res.ok) {
                setRoles(roles.filter(r => r.name !== roleName));
            } else {
                alert("Failed to delete role");
            }
        } catch (error) {
            alert("Network error");
        }
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Roles"
                subtitle="Define what members can do in this organization."
            />

            <Card title="Create new role" description="Define a new set of permissions for members.">
                <div className="grid gap-3 md:grid-cols-3">
                    <div className="space-y-1 md:col-span-1">
                        <label className="text-sm text-zinc-200">Name</label>
                        <Input 
                            value={roleForm.name}
                            onChange={(e) => setRoleForm({...roleForm, name: e.target.value})}
                            placeholder="e.g. Developer" 
                        />
                    </div>
                    <div className="space-y-1 md:col-span-1">
                        <label className="text-sm text-zinc-200">Level</label>
                        <Input 
                            type="number"
                            value={roleForm.level}
                            onChange={(e) => setRoleForm({...roleForm, level: parseInt(e.target.value) || 1})}
                            placeholder="10" 
                        />
                    </div>
                    <div className="space-y-1 md:col-span-3">
                        <label className="text-sm text-zinc-200">Permissions (comma separated)</label>
                        <Input 
                            value={roleForm.permissions}
                            onChange={(e) => setRoleForm({...roleForm, permissions: e.target.value})}
                            placeholder="project:read, project:write" 
                        />
                    </div>
                </div>
                <div className="mt-3 flex justify-end">
                    <Button onClick={handleSave} disabled={saving || !roleForm.name}>
                        {saving ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <ShieldPlus className="mr-2 h-4 w-4" />}
                        Create role
                    </Button>
                </div>
            </Card>

            <Card title="Roles list">
                {loading ? (
                    <div className="py-10 flex justify-center">
                        <Loader2 className="h-8 w-8 animate-spin text-[#4F46E5]" />
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-sm">
                            <thead className="text-left text-zinc-400">
                                <tr className="border-b border-white/10">
                                    <th className="py-2 pr-4">Name</th>
                                    <th className="py-2 pr-4">Permissions</th>
                                    <th className="py-2 text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {roles.length === 0 ? (
                                    <tr>
                                        <td colSpan={3} className="py-10 text-center text-zinc-500">No roles defined.</td>
                                    </tr>
                                ) : roles.map((r) => (
                                    <tr key={r.id} className="border-b border-white/10">
                                        <td className="py-3 pr-4 font-medium">{r.name}</td>
                                        <td className="py-3 pr-4 text-zinc-300">
                                            <div className="flex flex-wrap gap-1">
                                                {r.permissions?.map((p: string) => (
                                                    <span key={p} className="text-[10px] bg-zinc-800 text-zinc-500 px-1.5 py-0.5 rounded border border-white/5">
                                                        {p}
                                                    </span>
                                                )) || "None"}
                                            </div>
                                        </td>
                                        <td className="py-3 text-right">
                                            <div className="inline-flex gap-2">
                                                <Button size="sm" variant="destructive" onClick={() => handleDelete(r.name)}>
                                                    <Trash2 className="h-4 w-4" />
                                                </Button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </Card>
        </div>
    );
}
