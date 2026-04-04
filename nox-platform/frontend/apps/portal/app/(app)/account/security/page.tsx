'use client';

import Link from "next/link";
import { useState } from "react";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader, Alert } from "../../../_components/UiBits";

export default function AccountSecurityPage() {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const [formData, setFormData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleChangePassword = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setSuccess(false);

        // Validation
        if (!formData.currentPassword || !formData.newPassword || !formData.confirmPassword) {
            setError('All fields are required');
            return;
        }

        if (formData.newPassword !== formData.confirmPassword) {
            setError('New passwords do not match');
            return;
        }

        if (formData.newPassword.length < 8) {
            setError('New password must be at least 8 characters');
            return;
        }

        setLoading(true);

        try {
            const response = await fetch('/api/v1/auth/change-password', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    currentPassword: formData.currentPassword,
                    newPassword: formData.newPassword,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to change password');
            }

            setSuccess(true);
            setFormData({
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
            });
            setTimeout(() => setSuccess(false), 5000);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-6">
            <PageHeader title="Security" subtitle="Change your password and review security status." />

            {error && (
                <Alert type="error" title="Error" message={error} />
            )}
            
            {success && (
                <Alert type="success" title="Success" message="Password changed successfully" />
            )}

            <div className="grid gap-6 md:grid-cols-2">
                <Card title="Change password" description="Use a strong password you haven't used elsewhere.">
                    <form onSubmit={handleChangePassword} className="space-y-3">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Current password</label>
                            <Input 
                                type="password" 
                                placeholder="Current password" 
                                name="currentPassword"
                                value={formData.currentPassword}
                                onChange={handleInputChange}
                                disabled={loading}
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">New password</label>
                            <Input 
                                type="password" 
                                placeholder="New password" 
                                name="newPassword"
                                value={formData.newPassword}
                                onChange={handleInputChange}
                                disabled={loading}
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Confirm new password</label>
                            <Input 
                                type="password" 
                                placeholder="Confirm new password" 
                                name="confirmPassword"
                                value={formData.confirmPassword}
                                onChange={handleInputChange}
                                disabled={loading}
                            />
                        </div>

                        <div className="mt-5 flex items-center justify-end">
                            <Button type="submit" disabled={loading}>
                                {loading ? 'Changing...' : 'Change password'}
                            </Button>
                        </div>
                    </form>
                </Card>

                <Card title="Security overview" description="Quick summary of your account security.">
                    <div className="space-y-3 text-sm">
                        <div className="flex items-center justify-between">
                            <div className="text-zinc-400">Email</div>
                            <div>Verified</div>
                        </div>
                        <div className="flex items-center justify-between">
                            <div className="text-zinc-400">Multi-factor authentication</div>
                            <Link className="text-blue-400 hover:text-blue-300" href="/account/mfa">
                                Manage
                            </Link>
                        </div>
                        <div className="flex items-center justify-between">
                            <div className="text-zinc-400">Last login</div>
                            <div>—</div>
                        </div>
                    </div>
                </Card>
            </div>
        </div>
    );
}

