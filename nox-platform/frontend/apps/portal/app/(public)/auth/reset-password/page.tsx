"use client";

import Link from "next/link";
import { useState } from "react";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";
import { Lock, ShieldCheck, ArrowLeft, ArrowRight, Loader2, CheckCircle2, AlertCircle } from "lucide-react";

export default function ResetPasswordPage() {
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [tokenValid, setTokenValid] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        // Simulate API call
        setTimeout(() => {
            setSuccess(true);
            setLoading(false);
        }, 1500);
    };

    if (!tokenValid) {
        return (
            <div className="flex flex-col gap-6 text-center animate-in fade-in zoom-in duration-300">
                <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400">
                    <AlertCircle className="w-8 h-8" />
                </div>
                <div className="flex flex-col gap-2">
                    <h1 className="text-2xl font-black tracking-tight text-foreground">Link expired</h1>
                    <p className="text-sm text-muted-foreground leading-relaxed">
                        The password reset link is invalid or has expired. Please request a new one.
                    </p>
                </div>
                <Button asChild className="w-full mt-2">
                    <Link href="/auth/forgot-password">
                        Request new link
                    </Link>
                </Button>
            </div>
        );
    }

    if (success) {
        return (
            <div className="flex flex-col gap-6 text-center animate-in fade-in zoom-in duration-300">
                <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400">
                    <CheckCircle2 className="w-8 h-8" />
                </div>
                <div className="flex flex-col gap-2">
                    <h1 className="text-2xl font-black tracking-tight text-foreground">Password reset</h1>
                    <p className="text-sm text-muted-foreground">
                        Your password has been successfully updated. You can now sign in with your new credentials.
                    </p>
                </div>
                <Button asChild className="w-full mt-2">
                    <Link href="/auth/login">
                        Sign in
                    </Link>
                </Button>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-8">
            <div className="flex flex-col gap-2">
                <h1 className="text-3xl font-black tracking-tight text-foreground">New password</h1>
                <p className="text-muted-foreground">
                    Please choose a strong password you haven&apos;t used before.
                </p>
            </div>

            {error ? <Alert tone="danger" title="Update failed" description={error} /> : null}

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <div className="flex flex-col gap-2">
                    <label className="text-sm font-semibold text-foreground/70 flex items-center gap-2">
                        <Lock className="w-3.5 h-3.5" />
                        New Password
                    </label>
                    <Input type="password" placeholder="••••••••" required autoComplete="new-password" />
                </div>

                <div className="flex flex-col gap-2">
                    <label className="text-sm font-semibold text-foreground/70 flex items-center gap-2">
                        <ShieldCheck className="w-3.5 h-3.5" />
                        Confirm Password
                    </label>
                    <Input type="password" placeholder="••••••••" required autoComplete="new-password" />
                </div>

                <Button type="submit" disabled={loading} className="w-full mt-4 gap-2">
                    {loading ? (
                        <>
                            <Loader2 className="w-4 h-4 animate-spin" />
                            <span>Updating...</span>
                        </>
                    ) : (
                        <>
                            <span>Reset password</span>
                            <ArrowRight className="w-4 h-4" />
                        </>
                    )}
                </Button>
            </form>

            <div className="text-center text-sm border-t border-border pt-6 mt-2">
                <Link href="/auth/login" className="font-bold text-muted-foreground hover:text-foreground inline-flex items-center gap-2">
                    <ArrowLeft className="w-4 h-4" />
                    Back to sign in
                </Link>
            </div>
        </div>
    );
}

