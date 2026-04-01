'use client';

import { useEffect, useState } from "react";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../../_components/UiBits";
import { Loader2, UserPlus, Trash2 } from "lucide-react";

export default function OrganizationMembersPage({ params }: { params: { orgId: string } }) {
    const [members, setMembers] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [inviting, setInviting] = useState(false);
    const [inviteForm, setInviteForm] = useState({ email: "", roleName: "Member" });

    const fetchMembers = async () => {
        try {
            const res = await fetch(`/api/orgs/${params.orgId}/members`);
            const data = await res.json();
            if (data.data?.content) {
                setMembers(data.data.content);
            }
        } catch (error) {
            console.error("Failed to fetch members:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchMembers();
    }, [params.orgId]);

    const handleInvite = async () => {
        if (!inviteForm.email) return;
        setInviting(true);
        try {
            const res = await fetch(`/api/orgs/${params.orgId}/members/invite`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(inviteForm),
            });
            if (res.ok) {
                alert("Invitation sent!");
                setInviteForm({ email: "", roleName: "Member" });
                fetchMembers();
            } else {
                const err = await res.json();
                alert(`Error: ${err.message || "Failed to invite"}`);
            }
        } catch (error) {
            alert("Network error");
        } finally {
            setInviting(false);
        }
    };

    const handleRemove = async (userId: string) => {
        if (!confirm("Are you sure you want to remove this member?")) return;
        try {
            const res = await fetch(`/api/orgs/${params.orgId}/members?userId=${userId}`, {
                method: "DELETE",
            });
            if (res.ok) {
                setMembers(members.filter(m => m.userId !== userId));
            } else {
                alert("Failed to remove member");
            }
        } catch (error) {
            alert("Network error");
        }
    };

    return (
        <div className="space-y-6">
            <PageHeader
                title="Members"
                subtitle="Manage who can access this organization."
            />

            <Card title="Invite member" description="Send an email invitation to join your organization.">
                <div className="grid gap-3 md:grid-cols-2">
                    <div className="space-y-1">
                        <label className="text-sm text-zinc-200">Email</label>
                        <Input 
                            value={inviteForm.email}
                            onChange={(e) => setInviteForm({...inviteForm, email: e.target.value})}
                            placeholder="person@example.com" 
                        />
                    </div>
                    <div className="space-y-1">
                        <label className="text-sm text-zinc-200">Role</label>
                        <Input 
                            value={inviteForm.roleName}
                            onChange={(e) => setInviteForm({...inviteForm, roleName: e.target.value})}
                            placeholder="Admin / Member" 
                        />
                    </div>
                </div>
                <div className="mt-3 flex justify-end">
                    <Button onClick={handleInvite} disabled={inviting || !inviteForm.email}>
                        {inviting ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <UserPlus className="mr-2 h-4 w-4" />}
                        Send invite
                    </Button>
                </div>
            </Card>

            <Card title="Members list">
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
                                    <th className="py-2 pr-4">Email</th>
                                    <th className="py-2 pr-4">Role</th>
                                    <th className="py-2 pr-4">Joined</th>
                                    <th className="py-2 text-right">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {members.length === 0 ? (
                                    <tr>
                                        <td colSpan={5} className="py-10 text-center text-zinc-500">No members found.</td>
                                    </tr>
                                ) : members.map((m) => (
                                    <tr key={m.id} className="border-b border-white/10">
                                        <td className="py-3 pr-4">{m.fullName || "Pending"}</td>
                                        <td className="py-3 pr-4 text-zinc-300">{m.email}</td>
                                        <td className="py-3 pr-4">
                                            <span className="inline-flex items-center rounded-md bg-zinc-800 px-2 py-1 text-xs font-medium text-zinc-400">
                                                {m.role?.name || "Member"}
                                            </span>
                                        </td>
                                        <td className="py-3 pr-4 text-zinc-500">
                                            {m.joinedAt ? new Date(m.joinedAt).toLocaleDateString() : "Invited"}
                                        </td>
                                        <td className="py-3 text-right">
                                            <Button 
                                                size="sm" 
                                                variant="destructive"
                                                onClick={() => handleRemove(m.userId)}
                                            >
                                                <Trash2 className="h-4 w-4" />
                                            </Button>
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
