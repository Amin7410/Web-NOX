"use client";

import Link from "next/link";
import { useState } from "react";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";
import { Mail, ArrowLeft, ArrowRight } from "lucide-react";

export default function ForgotPasswordPage() {
    const [loading, setLoading] = useState(false);
    const [sent, setSent] = useState(false);
    const [email, setEmail] = useState("");
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        // This would call the real API in a real implementation
        // For now, we simulate a successful send
        setTimeout(() => {
            setSent(true);
            setLoading(false);
        }, 1500);
    };

    if (sent) {
        return (
            <div className="flex flex-col gap-6 text-center">
                <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400">
                    <Mail className="w-8 h-8" />
                </div>
                <div className="flex flex-col gap-2">
                    <h1 className="text-2xl font-black tracking-tight text-foreground">Check your inbox</h1>
                    <p className="text-sm text-muted-foreground leading-relaxed">
                        If an account with <span className="font-semibold text-foreground">{email}</span> exists, 
                        we have sent a password reset link.
                    </p>
                </div>
                <Button asChild className="w-full mt-2">
                    <Link href="/auth/login">
                        <ArrowLeft className="w-4 h-4 mr-2" />
                        Back to sign in
                    </Link>
                </Button>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-8">
            <div className="flex flex-col gap-2">
                <h1 className="text-3xl font-black tracking-tight text-foreground">Forgot password?</h1>
                <p className="text-muted-foreground">
                    No worries, we&apos;ll send you instructions to reset your password.
                </p>
            </div>

            {error ? <Alert tone="danger" title="Sending failed" description={error} /> : null}

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <div className="flex flex-col gap-2">
                    <label className="text-sm font-semibold text-foreground/70 flex items-center gap-2">
                        <Mail className="w-3.5 h-3.5" />
                        Email Address
                    </label>
                    <Input 
                        type="email" 
                        placeholder="you@example.com" 
                        required 
                        autoComplete="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                </div>

                <Button type="submit" disabled={loading} className="w-full mt-4 gap-2">
                    {loading ? "Sending..." : (
                        <>
                            <span>Send reset link</span>
                            <ArrowRight className="w-4 h-4" />
                        </>
                    )}
                </Button>
            </form>

            <div className="text-center text-sm border-t border-border pt-6">
                <Link href="/auth/login" className="font-bold text-muted-foreground hover:text-foreground inline-flex items-center gap-2">
                    <ArrowLeft className="w-4 h-4" />
                    Back to sign in
                </Link>
            </div>
        </div>
    );
}

