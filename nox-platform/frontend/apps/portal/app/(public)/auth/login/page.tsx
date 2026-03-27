"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";

export default function LoginPage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        const formData = new FormData(e.currentTarget);
        const email = formData.get("email") as string;
        const password = formData.get("password") as string;

        try {
            const res = await fetch("/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            const data = await res.json();
            
            if (!res.ok) {
                const errorMsg = data.error?.message || data.message || "Sign in failed";
                setError(typeof errorMsg === 'string' ? errorMsg : JSON.stringify(errorMsg));
            } else {
                // Check if MFA is required
                if (data.data && data.data.mfaRequired) {
                    // Redirect to MFA verification page with the mfaToken
                    router.push(`/auth/mfa/verify?token=${data.data.mfaToken}`);
                } else {
                    // Redirect to dashboard/organizations
                    router.push("/organizations");
                }
            }
        } catch (err: any) {
            setError(err.message || "An unexpected error occurred");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-5">
            <div>
                <div className="text-xl font-semibold">Sign in to NOX Portal</div>
                <div className="mt-1 text-sm text-zinc-400">
                    Access your projects and organizations.
                </div>
            </div>

            {error ? (
                <Alert tone="danger" title="Sign in failed" description={error} />
            ) : null}

            <form onSubmit={handleSubmit} className="space-y-3">
                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Email</label>
                    <Input name="email" type="email" placeholder="you@example.com" required />
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Password</label>
                    <Input name="password" type="password" placeholder="••••••••" required />
                </div>

                <div className="flex items-center justify-between pt-1">
                    <label className="flex items-center gap-2 text-sm text-zinc-300">
                        <input type="checkbox" className="accent-blue-600" />
                        Remember this device
                    </label>
                    <Link href="/auth/forgot-password" className="text-sm text-blue-400 hover:text-blue-300">
                        Forgot your password?
                    </Link>
                </div>

                <Button type="submit" disabled={loading} className="w-full">
                    {loading ? "Signing in..." : "Sign in"}
                </Button>
            </form>

            <div className="text-sm text-zinc-400">
                Don&apos;t have an account?{" "}
                <Link href="/auth/register" className="text-blue-400 hover:text-blue-300">
                    Sign up
                </Link>
            </div>
        </div>
    );
}

