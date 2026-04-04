'use client';

import { Button, Input } from "@nox/ui";
import { Card, PageHeader, Alert } from "../../../_components/UiBits";
import { useState, useEffect } from "react";

export default function AccountMfaPage() {
    const [enabled, setEnabled] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const [qrCode, setQrCode] = useState<string | null>(null);
    const [verificationCode, setVerificationCode] = useState('');
    const [verifying, setVerifying] = useState(false);
    const [backupCodes, setBackupCodes] = useState<string[]>([]);
    const [showBackupCodes, setShowBackupCodes] = useState(false);
    const [disabling, setDisabling] = useState(false);

    useEffect(() => {
        const fetchMfaStatus = async () => {
            try {
                const response = await fetch('/api/v1/auth/mfa/status', {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });

                if (response.ok) {
                    const data = await response.json();
                    setEnabled(data.data?.enabled || false);
                    if (data.data?.backupCodes) {
                        setBackupCodes(data.data.backupCodes);
                    }
                }
            } catch (err) {
                console.error('Failed to fetch MFA status:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchMfaStatus();
    }, []);

    const handleSetupMfa = async () => {
        try {
            setError(null);
            const response = await fetch('/api/v1/auth/mfa/setup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Failed to setup MFA');
            }

            const data = await response.json();
            setQrCode(data.data?.qrCode || null);
            setBackupCodes(data.data?.backupCodes || []);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        }
    };

    const handleVerifyAndEnable = async () => {
        if (!verificationCode || verificationCode.length !== 6) {
            setError('Please enter a valid 6-digit code');
            return;
        }

        setVerifying(true);
        setError(null);

        try {
            const response = await fetch('/api/v1/auth/mfa/enable', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    code: verificationCode,
                }),
            });

            if (!response.ok) {
                throw new Error('Invalid verification code');
            }

            const data = await response.json();
            setEnabled(true);
            setSuccess(true);
            setBackupCodes(data.data?.backupCodes || []);
            setQrCode(null);
            setVerificationCode('');
            setTimeout(() => setSuccess(false), 5000);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setVerifying(false);
        }
    };

    const handleDisableMfa = async () => {
        if (!confirm('Are you sure you want to disable MFA? This will reduce your account security.')) {
            return;
        }

        setDisabling(true);
        setError(null);

        try {
            const response = await fetch('/api/v1/auth/mfa/disable', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Failed to disable MFA');
            }

            setEnabled(false);
            setBackupCodes([]);
            setSuccess(true);
            setTimeout(() => setSuccess(false), 5000);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setDisabling(false);
        }
    };

    const handleRegenerateBackupCodes = async () => {
        try {
            const response = await fetch('/api/v1/auth/mfa/regenerate-backup-codes', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!response.ok) {
                throw new Error('Failed to regenerate backup codes');
            }

            const data = await response.json();
            setBackupCodes(data.data?.backupCodes || []);
            setSuccess(true);
            setTimeout(() => setSuccess(false), 5000);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        }
    };

    if (loading) {
        return (
            <div className="space-y-6">
                <PageHeader title="Multi-factor authentication" subtitle="Add an extra layer of security to your account." />
                <div className="text-zinc-400">Loading MFA settings...</div>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <PageHeader title="Multi-factor authentication" subtitle="Add an extra layer of security to your account." />

            {error && (
                <Alert type="error" title="Error" message={error} />
            )}
            
            {success && (
                <Alert type="success" title="Success" message={enabled ? "MFA enabled successfully" : "MFA disabled successfully"} />
            )}

            <Card title="Status">
                <div className="text-sm">
                    MFA is currently:{" "}
                    <span className={enabled ? "text-emerald-300" : "text-zinc-300"}>
                        {enabled ? "Enabled" : "Disabled"}
                    </span>
                </div>
            </Card>

            {!enabled && !qrCode ? (
                <Card
                    title="Enable MFA"
                    description="Add an authenticator app to your account for enhanced security."
                >
                    <Button onClick={handleSetupMfa}>Set up authenticator app</Button>
                </Card>
            ) : !enabled && qrCode ? (
                <Card
                    title="Enable MFA"
                    description="Scan the QR code with an authenticator app, then enter the 6-digit code."
                >
                    <div className="grid gap-4 md:grid-cols-2">
                        <div className="rounded-xl border border-white/10 bg-black/20 p-4">
                            {qrCode ? (
                                <img src={`data:image/png;base64,${qrCode}`} alt="MFA QR Code" className="w-full" />
                            ) : (
                                <div className="text-sm text-zinc-400 h-48 flex items-center justify-center">QR code placeholder</div>
                            )}
                        </div>
                        <div className="space-y-3">
                            <div className="space-y-1">
                                <label className="text-sm text-zinc-200">6-digit verification code</label>
                                <Input 
                                    placeholder="123456" 
                                    maxLength={6}
                                    value={verificationCode}
                                    onChange={(e) => setVerificationCode(e.target.value.replace(/[^0-9]/g, ''))}
                                    disabled={verifying}
                                />
                            </div>
                            <Button 
                                onClick={handleVerifyAndEnable}
                                disabled={verifying || verificationCode.length !== 6}
                            >
                                {verifying ? 'Verifying...' : 'Verify & enable'}
                            </Button>
                        </div>
                    </div>
                </Card>
            ) : enabled ? (
                <>
                    <Card title="Backup codes" description="Store these codes in a safe place. Each code can be used once if you lose access to your authenticator.">
                        {showBackupCodes ? (
                            <div className="grid gap-2 text-sm md:grid-cols-2">
                                {backupCodes.map((code, idx) => (
                                    <div key={idx} className="rounded-lg border border-white/10 bg-black/20 px-3 py-2 font-mono">
                                        {code}
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="rounded-lg border border-white/10 bg-black/20 px-3 py-2 text-sm text-zinc-400">
                                Backup codes are hidden. Click "Show" to reveal them.
                            </div>
                        )}
                        <div className="mt-4 flex items-center gap-2">
                            <Button variant="outline" onClick={() => setShowBackupCodes(!showBackupCodes)}>
                                {showBackupCodes ? 'Hide backup codes' : 'Show backup codes'}
                            </Button>
                            <Button variant="outline" onClick={handleRegenerateBackupCodes}>
                                Generate new backup codes
                            </Button>
                        </div>
                    </Card>

                    <Card title="Disable MFA" description="Your account will be protected by password only.">
                        <Button variant="destructive" onClick={handleDisableMfa} disabled={disabling}>
                            {disabling ? 'Disabling...' : 'Disable MFA'}
                        </Button>
                    </Card>
                </>
            ) : null}
        </div>
    );
}

