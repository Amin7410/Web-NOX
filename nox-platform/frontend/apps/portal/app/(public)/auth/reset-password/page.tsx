"use client";

import Link from "next/link";
import { useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";
import { api } from "../../../_lib/api";

function ResetPasswordForm() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const prefillEmail = searchParams.get("email") || "";

    const [email, setEmail] = useState(prefillEmail);
    const [otpCode, setOtpCode] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (newPassword !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        setLoading(true);
        try {
            await api.post("/auth/reset-password", { email, otpCode, newPassword });
            router.push("/auth/login?reset=true");
        } catch (err: any) {
            setError(err.message || "Reset failed. The code may have expired.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
            <div className="space-y-2">
                <h1 className="text-3xl font-bold tracking-tight text-white">Reset password</h1>
                <p className="text-zinc-400">
                    Enter the 6-digit code from your email and choose a new password.
                </p>
            </div>

            {error && (
                <div className="animate-in zoom-in-95 duration-200">
                    <Alert tone="danger" title="Reset failed" description={error} />
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5">
                <div className="space-y-1.5">
                    <label className="text-xs font-medium text-zinc-400 ml-1">Email address</label>
                    <Input
                        type="email"
                        placeholder="you@example.com"
                        required
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="bg-zinc-950/50 border-white/5 focus:border-blue-500/50 focus:ring-blue-500/20 h-11"
                    />
                </div>

                <div className="space-y-1.5">
                    <label className="text-xs font-medium text-zinc-400 ml-1">Reset Code</label>
                    <Input
                        type="text"
                        placeholder="000000"
                        maxLength={6}
                        required
                        value={otpCode}
                        onChange={(e) => setOtpCode(e.target.value)}
                        className="bg-zinc-950/50 border-white/5 focus:border-blue-500/50 h-12 text-center text-xl tracking-[0.5em] font-bold"
                    />
                </div>

                <div className="space-y-1.5">
                    <label className="text-xs font-medium text-zinc-400 ml-1">New password</label>
                    <Input
                        type="password"
                        placeholder="••••••••"
                        required
                        value={newPassword}
                        onChange={(e) => setNewPassword(e.target.value)}
                        className="bg-zinc-950/50 border-white/5 focus:border-blue-500/50 focus:ring-blue-500/20 h-11"
                    />
                    <p className="text-[10px] text-zinc-500 ml-1">At least 8 characters.</p>
                </div>

                <div className="space-y-1.5">
                    <label className="text-xs font-medium text-zinc-400 ml-1">Confirm new password</label>
                    <Input
                        type="password"
                        placeholder="••••••••"
                        required
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        className="bg-zinc-950/50 border-white/5 focus:border-blue-500/50 focus:ring-blue-500/20 h-11"
                    />
                </div>

                <div className="pt-2">
                    <Button
                        type="submit"
                        disabled={loading || otpCode.length < 6}
                        className="w-full h-11 bg-blue-600 hover:bg-blue-500 text-white font-semibold shadow-lg shadow-blue-500/20 transition-all active:scale-[0.98]"
                    >
                        {loading ? (
                            <div className="flex items-center gap-2 justify-center">
                                <div className="h-4 w-4 animate-spin rounded-full border-2 border-white/20 border-t-white" />
                                <span>Resetting...</span>
                            </div>
                        ) : (
                            "Reset password"
                        )}
                    </Button>
                </div>
            </form>

            <div className="pt-4 text-center">
                <Link href="/auth/login" className="text-sm text-zinc-500 hover:text-blue-400 transition-colors">
                    ← Back to sign in
                </Link>
            </div>
        </div>
    );
}

export default function ResetPasswordPage() {
    return (
        <Suspense>
            <ResetPasswordForm />
        </Suspense>
    );
}
