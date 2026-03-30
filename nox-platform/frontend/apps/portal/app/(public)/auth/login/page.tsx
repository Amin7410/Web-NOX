"use client";

import Link from "next/link";
import { useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";
import { api } from "../../../_lib/api";

interface AuthResponse {
    userId: string;
    email: string;
    token: string;
    refreshToken: string;
    mfaRequired: boolean;
    mfaToken: string | null;
}

function LoginForm() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const verified = searchParams.get("verified") === "true";
    const reset = searchParams.get("reset") === "true";

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [mfaCode, setMfaCode] = useState("");
    const [mfaToken, setMfaToken] = useState<string | null>(null);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const result = await api.post<AuthResponse>("/auth/login", { email, password });

            if (result.mfaRequired) {
                // Switch to MFA screen
                setMfaToken(result.mfaToken);
                setLoading(false);
                return;
            }

            // Save tokens and redirect
            localStorage.setItem("access_token", result.token);
            localStorage.setItem("refresh_token", result.refreshToken);
            router.push("/projects");
        } catch (err: any) {
            setError(err.message || "Sign in failed. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    const handleMfa = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            const result = await api.post<AuthResponse>("/auth/mfa/verify", {
                mfaToken,
                code: mfaCode,
            });

            localStorage.setItem("access_token", result.token);
            localStorage.setItem("refresh_token", result.refreshToken);
            router.push("/projects");
        } catch (err: any) {
            setError(err.message || "Invalid verification code.");
        } finally {
            setLoading(false);
        }
    };

    // ─── MFA Screen ────────────────────────────────────────────────────────────
    if (mfaToken) {
        return (
            <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
                <div className="space-y-2">
                    <div className="mb-6 flex h-16 w-16 items-center justify-center rounded-full bg-blue-500/10 text-blue-500 ring-1 ring-blue-500/20">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="h-8 w-8">
                            <path strokeLinecap="round" strokeLinejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
                        </svg>
                    </div>
                    <h1 className="text-3xl font-bold tracking-tight text-white">Two-factor auth</h1>
                    <p className="text-zinc-400">Enter the 6-digit code from your authenticator app.</p>
                </div>

                {error && (
                    <div className="animate-in zoom-in-95 duration-200">
                        <Alert tone="danger" title="Verification failed" description={error} />
                    </div>
                )}

                <form onSubmit={handleMfa} className="space-y-6">
                    <div className="space-y-1.5">
                        <label className="text-xs font-medium text-zinc-400 ml-1">Authentication Code</label>
                        <Input
                            type="text"
                            placeholder="000000"
                            maxLength={6}
                            required
                            value={mfaCode}
                            onChange={(e) => setMfaCode(e.target.value)}
                            className="bg-zinc-950/50 border-white/5 focus:border-blue-500/50 h-12 text-center text-xl tracking-[0.5em] font-bold"
                        />
                    </div>

                    <Button
                        type="submit"
                        disabled={loading || mfaCode.length < 6}
                        className="w-full h-11 bg-blue-600 hover:bg-blue-500 text-white font-semibold shadow-lg shadow-blue-500/20 transition-all active:scale-[0.98]"
                    >
                        {loading ? "Verifying..." : "Verify"}
                    </Button>
                </form>

                <div className="text-center">
                    <button type="button" onClick={() => setMfaToken(null)} className="text-sm text-zinc-500 hover:text-blue-400 transition-colors">
                        ← Back to sign in
                    </button>
                </div>
            </div>
        );
    }

    // ─── Login Screen ───────────────────────────────────────────────────────────
    return (
        <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
            <div className="space-y-2">
                <h1 className="text-3xl font-bold tracking-tight text-white">Welcome back</h1>
                <p className="text-zinc-400">Sign in to continue to NOX Portal.</p>
            </div>

            {verified && (
                <div className="animate-in zoom-in-95 duration-200">
                    <Alert tone="success" title="Email verified!" description="Your account is ready. Sign in to get started." />
                </div>
            )}

            {reset && (
                <div className="animate-in zoom-in-95 duration-200">
                    <Alert tone="success" title="Password reset!" description="Your password has been updated. Sign in with your new password." />
                </div>
            )}

            {error && (
                <div className="animate-in zoom-in-95 duration-200">
                    <Alert tone="danger" title="Sign in failed" description={error} />
                </div>
            )}

            <form onSubmit={handleLogin} className="space-y-5">
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
                    <div className="flex items-center justify-between ml-1">
                        <label className="text-xs font-medium text-zinc-400">Password</label>
                        <Link href="/auth/forgot-password" className="text-xs text-blue-400 hover:text-blue-300 transition-colors">
                            Forgot password?
                        </Link>
                    </div>
                    <Input
                        type="password"
                        placeholder="••••••••"
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="bg-zinc-950/50 border-white/5 focus:border-blue-500/50 focus:ring-blue-500/20 h-11"
                    />
                </div>

                <div className="pt-2">
                    <Button
                        type="submit"
                        disabled={loading}
                        className="w-full h-11 bg-blue-600 hover:bg-blue-500 text-white font-semibold shadow-lg shadow-blue-500/20 transition-all active:scale-[0.98]"
                    >
                        {loading ? (
                            <div className="flex items-center gap-2 justify-center">
                                <div className="h-4 w-4 animate-spin rounded-full border-2 border-white/20 border-t-white" />
                                <span>Signing in...</span>
                            </div>
                        ) : (
                            "Sign in"
                        )}
                    </Button>
                </div>
            </form>

            <div className="pt-4 text-center">
                <p className="text-sm text-zinc-500">
                    Don&apos;t have an account?{" "}
                    <Link href="/auth/register" className="font-medium text-blue-400 transition-colors hover:text-blue-300">
                        Sign up
                    </Link>
                </p>
            </div>
        </div>
    );
}

export default function LoginPage() {
    return (
        <Suspense>
            <LoginForm />
        </Suspense>
    );
}
