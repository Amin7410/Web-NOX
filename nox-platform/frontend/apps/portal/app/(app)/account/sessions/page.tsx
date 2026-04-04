'use client';

import { Button } from "@nox/ui";
import { Card, PageHeader, Alert } from "../../../_components/UiBits";
import { useState, useEffect } from "react";

interface Session {
    id: string;
    deviceName: string;
    ipAddress: string;
    lastActive: string;
    createdAt: string;
    isCurrent: boolean;
}

export default function AccountSessionsPage() {
    const [sessions, setSessions] = useState<Session[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const [signingOut, setSigningOut] = useState<string | null>(null);

    useEffect(() => {
        const fetchSessions = async () => {
            try {
                const response = await fetch('/api/v1/auth/sessions', {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });

                if (response.ok) {
                    const data = await response.json();
                    setSessions(data.data || []);
                } else {
                    setError('Failed to load sessions');
                }
            } catch (err) {
                console.error('Failed to fetch sessions:', err);
                setError('An error occurred while loading sessions');
            } finally {
                setLoading(false);
            }
        };

        fetchSessions();
    }, []);

    const handleSignOut = async (sessionId: string) => {
        if (!confirm('Are you sure you want to sign out from this device?')) {
            return;
        }

        setSigningOut(sessionId);
        setError(null);

        try {
            const response = await fetch(`/api/v1/auth/sessions/${sessionId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Failed to sign out');
            }

            setSessions(sessions.filter(s => s.id !== sessionId));
            setSuccess(true);
            setTimeout(() => setSuccess(false), 5000);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setSigningOut(null);
        }
    };

    const handleSignOutAll = async () => {
        if (!confirm('Are you sure you want to sign out from all other devices? You will remain signed in on this device.')) {
            return;
        }

        setSigningOut('all');
        setError(null);

        try {
            const response = await fetch('/api/v1/auth/sessions/signout-all', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Failed to sign out from all devices');
            }

            // Keep only current session
            setSessions(sessions.filter(s => s.isCurrent));
            setSuccess(true);
            setTimeout(() => setSuccess(false), 5000);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setSigningOut(null);
        }
    };

    const formatLastActive = (lastActive: string) => {
        const date = new Date(lastActive);
        const now = new Date();
        const seconds = Math.floor((now.getTime() - date.getTime()) / 1000);
        
        if (seconds < 60) return 'Just now';
        if (seconds < 3600) return `${Math.floor(seconds / 60)} minutes ago`;
        if (seconds < 86400) return `${Math.floor(seconds / 3600)} hours ago`;
        if (seconds < 604800) return `${Math.floor(seconds / 86400)} days ago`;
        
        return date.toLocaleDateString();
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString();
    };

    if (loading) {
        return (
            <div className="space-y-6">
                <PageHeader
                    title="Sessions"
                    subtitle="Review and manage your active sign-in sessions."
                />
                <div className="text-zinc-400">Loading sessions...</div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <PageHeader
                title="Sessions"
                subtitle="Review and manage your active sign-in sessions."
                actions={<Button variant="outline" onClick={handleSignOutAll} disabled={signingOut === 'all'}>
                    {signingOut === 'all' ? 'Signing out...' : 'Sign out from all other devices'}
                </Button>}
            />

            {error && (
                <Alert type="error" title="Error" message={error} />
            )}
            
            {success && (
                <Alert type="success" title="Success" message="Session terminated successfully" />
            )}

            <Card title="Active sessions" description={`You have ${sessions.length} active session${sessions.length !== 1 ? 's' : ''}`}>
                {sessions.length === 0 ? (
                    <div className="text-zinc-400">No active sessions found.</div>
                ) : (
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
                                {sessions.map((session) => (
                                    <tr key={session.id} className="border-b border-white/10">
                                        <td className="py-3 pr-4">
                                            {session.deviceName}
                                            {session.isCurrent && <span className="text-zinc-400"> (current)</span>}
                                        </td>
                                        <td className="py-3 pr-4">{session.ipAddress}</td>
                                        <td className="py-3 pr-4">{formatLastActive(session.lastActive)}</td>
                                        <td className="py-3 pr-4">{formatDate(session.createdAt)}</td>
                                        <td className="py-3 text-right">
                                            <Button 
                                                size="sm" 
                                                variant="outline" 
                                                disabled={session.isCurrent || signingOut === session.id}
                                                onClick={() => handleSignOut(session.id)}
                                            >
                                                {signingOut === session.id ? 'Signing out...' : 'Sign out'}
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

