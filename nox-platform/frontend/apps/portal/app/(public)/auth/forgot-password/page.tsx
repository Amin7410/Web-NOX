"use client";

import Link from "next/link";
import { useState } from "react";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";
import { api } from "../../../_lib/api";

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState("");
    const [loading, setLoading] = useState(false);
    const [sent, setSent] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            await api.post("/auth/forgot-password", { email });
            setSent(true);
        } catch (err: any) {
            setError(err.message || "Something went wrong. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
            <div className="space-y-2">
                <h1 className="text-3xl font-bold tracking-tight text-white">Forgot password?</h1>
                <p className="text-zinc-400">
                    Enter your email and we&apos;ll send you a reset code.
                </p>
            </div>

            {sent ? (
                <div className="animate-in zoom-in-95 duration-200">
                    <Alert
                        tone="success"
                        title="Reset code sent"
                        description="If an account with this email exists, we've sent a reset code. Check your inbox or the backend terminal log."
                    />
                    <div className="mt-6">
                        <Link
                            href="/auth/reset-password"
                            className="inline-flex w-full h-11 items-center justify-center rounded-lg bg-blue-600 hover:bg-blue-500 text-white font-semibold shadow-lg shadow-blue-500/20 transition-all active:scale-[0.98]"
                        >
                            Enter reset code →
                        </Link>
                    </div>
                </div>
            ) : (
                <>
                    {error && (
                        <div className="animate-in zoom-in-95 duration-200">
                            <Alert tone="danger" title="Request failed" description={error} />
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

                        <Button
                            type="submit"
                            disabled={loading}
                            className="w-full h-11 bg-blue-600 hover:bg-blue-500 text-white font-semibold shadow-lg shadow-blue-500/20 transition-all active:scale-[0.98]"
                        >
                            {loading ? (
                                <div className="flex items-center gap-2 justify-center">
                                    <div className="h-4 w-4 animate-spin rounded-full border-2 border-white/20 border-t-white" />
                                    <span>Sending...</span>
                                </div>
                            ) : (
                                "Send reset code"
                            )}
                        </Button>
                    </form>
                </>
            )}

            <div className="pt-4 text-center">
                <Link href="/auth/login" className="text-sm text-zinc-500 hover:text-blue-400 transition-colors">
                    ← Back to sign in
                </Link>
            </div>
        </div>
    );
}
