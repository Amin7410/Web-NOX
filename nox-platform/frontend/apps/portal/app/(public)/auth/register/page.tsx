"use client";

import Link from "next/link";
import { useState } from "react";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";

export default function RegisterPage() {
    const [loading, setLoading] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        const formData = new FormData(e.currentTarget);
        const email = formData.get("email") as string;
        const fullName = formData.get("fullName") as string;
        const password = formData.get("password") as string;
        const confirmPassword = formData.get("confirmPassword") as string;

        if (password !== confirmPassword) {
            setError("Passwords do not match");
            setLoading(false);
            return;
        }

        try {
            const res = await fetch("/api/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, fullName, password }),
            });

            const data = await res.json();
            if (!res.ok) {
                setError(data.message || data.error || "Failed to register");
            } else {
                setSubmitted(true);
            }
        } catch (err: any) {
            setError(err.message || "An unexpected error occurred");
        } finally {
            setLoading(false);
        }
    };

    if (submitted) {
        return (
            <div className="space-y-4">
                <div className="text-xl font-semibold">Verify your email</div>
                <p className="text-sm text-zinc-400">
                    We&apos;ve sent a verification link with an OTP to your email. Please check your inbox (or MailHog).
                </p>
                <div className="text-xs p-3 bg-zinc-800 rounded">
                    <strong>Tip:</strong> Open MailHog at <a href="http://localhost:8025" target="_blank" className="text-blue-400 underline">http://localhost:8025</a> to see the OTP.
                </div>
                <Button asChild className="w-full">
                    <Link href="/auth/login">Back to sign in</Link>
                </Button>
            </div>
        );
    }

    return (
        <div className="space-y-5">
            <div>
                <div className="text-xl font-semibold">Create your account</div>
                <div className="mt-1 text-sm text-zinc-400">
                    Start managing organizations and projects.
                </div>
            </div>

            {error ? (
                <Alert tone="danger" title="Registration failed" description={error} />
            ) : null}

            <form onSubmit={handleSubmit} className="space-y-3">
                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Full name</label>
                    <Input name="fullName" type="text" placeholder="John Doe" required />
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Email</label>
                    <Input name="email" type="email" placeholder="you@example.com" required />
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Password</label>
                    <Input name="password" type="password" placeholder="Minimum 8 characters" required />
                    <div className="text-xs text-zinc-500">
                        Use at least 8 characters. For best security, include uppercase, lowercase, and numbers.
                    </div>
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Confirm password</label>
                    <Input name="confirmPassword" type="password" placeholder="Repeat your password" required />
                </div>

                <Button type="submit" disabled={loading} className="w-full">
                    {loading ? "Creating account..." : "Create account"}
                </Button>
            </form>

            <div className="text-sm text-zinc-400">
                Already have an account?{" "}
                <Link href="/auth/login" className="text-blue-400 hover:text-blue-300">
                    Sign in
                </Link>
            </div>
        </div>
    );
}

