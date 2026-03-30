"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";
import { api } from "../../../_lib/api";

export default function RegisterPage() {
    const router = useRouter();

    // Form States
    const [formData, setFormData] = useState({
        email: "",
        password: "",
        confirmPassword: "",
        fullName: "",
    });
    const [otp, setOtp] = useState("");

    // UI States
    const [loading, setLoading] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleRegister = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (formData.password !== formData.confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        setLoading(true);
        try {
            await api.post("/auth/register", {
                email: formData.email,
                password: formData.password,
                fullName: formData.fullName,
            });
            setSubmitted(true);
        } catch (err: any) {
            setError(err.message || "Registration failed. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    const handleVerify = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setLoading(true);

        try {
            await api.post("/auth/verify-email", {
                email: formData.email,
                otpCode: otp,
            });
            router.push("/auth/login?verified=true");
        } catch (err: any) {
            setError(err.message || "Verification failed. Please check your code.");
        } finally {
            setLoading(false);
        }
    };

    if (submitted) {
        return (
            <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
                <div className="space-y-2">
                    <div className="mb-6 flex h-16 w-16 items-center justify-center rounded-full bg-blue-500/10 text-blue-500 ring-1 ring-blue-500/20">
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            fill="none"
                            viewBox="0 0 24 24"
                            strokeWidth={1.5}
                            stroke="currentColor"
                            className="h-8 w-8"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"
                            />
                        </svg>
                    </div>
                    <h1 className="text-3xl font-bold tracking-tight text-white">Verify email</h1>
                    <p className="text-zinc-400">
                        We&apos;ve sent a 6-digit code to <span className="text-blue-400 font-medium">{formData.email}</span>.
                        Check your inbox or the backend terminal log for the code.
                    </p>
                </div>

                {error ? (
                    <div className="animate-in zoom-in-95 duration-200">
                        <Alert tone="danger" title="Verification failed" description={error} />
                    </div>
                ) : null}

                <form onSubmit={handleVerify} className="space-y-6">
                    <div className="space-y-1.5">
                        <label className="text-xs font-medium text-zinc-400 ml-1">Verification Code</label>
                        <Input
                            type="text"
                            placeholder="000000"
                            maxLength={6}
                            required
                            value={otp}
                            onChange={(e) => setOtp(e.target.value)}
                            className="bg-zinc-950/50 border-white/5 focus:border-blue-500/50 focus:ring-blue-500/20 h-12 text-center text-xl tracking-[0.5em] font-bold"
                        />
                    </div>

                    <Button
                        type="submit"
                        disabled={loading || otp.length < 6}
                        className="w-full h-11 bg-blue-600 hover:bg-blue-500 text-white font-semibold shadow-lg shadow-blue-500/20 transition-all active:scale-[0.98]"
                    >
                        {loading ? "Verifying..." : "Confirm code"}
                    </Button>
                </form>

                <div className="pt-4 text-center">
                    <button
                        type="button"
                        onClick={() => setSubmitted(false)}
                        className="text-sm text-zinc-500 hover:text-blue-400 transition-colors"
                    >
                        Entered wrong email? Go back
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-700">
            <div className="space-y-2">
                <div className="inline-flex items-center gap-2 rounded-full border border-blue-500/20 bg-blue-500/10 px-3 py-1 text-[10px] font-bold uppercase tracking-wider text-blue-400 ring-1 ring-inset ring-blue-500/20">
                    Join 10,000+ users
                </div>
                <h1 className="text-3xl font-bold tracking-tight text-white">Create account</h1>
                <p className="text-zinc-400">
                    Experience the future of organization and project management.
                </p>
            </div>

            {error ? (
                <div className="animate-in zoom-in-95 duration-200">
                    <Alert tone="danger" title="Registration failed" description={error} />
                </div>
            ) : null}

            <form onSubmit={handleRegister} className="space-y-5">
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <div className="space-y-1.5">
                        <label className="text-xs font-medium text-zinc-400 ml-1">Full name</label>
                        <Input
                            type="text"
                            placeholder="John Doe"
                            required
                            value={formData.fullName}
                            onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                            className="bg-zinc-950/50 border-white/5 focus:border-blue-500/50 focus:ring-blue-500/20 h-11"
                        />
                    </div>
                    <div className="space-y-1.5">
                        <label className="text-xs font-medium text-zinc-400 ml-1">Email address</label>
                        <Input
                            type="email"
                            placeholder="you@example.com"
                            required
                            value={formData.email}
                            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                            className="bg-zinc-950/50 border-white/5 focus:border-blue-500/50 focus:ring-blue-500/20 h-11"
                        />
                    </div>
                </div>

                <div className="space-y-1.5">
                    <label className="text-xs font-medium text-zinc-400 ml-1">Password</label>
                    <Input
                        type="password"
                        placeholder="••••••••"
                        required
                        value={formData.password}
                        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        className="bg-zinc-950/50 border-white/5 focus:border-blue-500/50 focus:ring-blue-500/20 h-11"
                    />
                    <p className="mt-1 text-[10px] text-zinc-500 leading-relaxed">
                        At least 8 characters. For best security, include uppercase, lowercase, and numbers.
                    </p>
                </div>

                <div className="space-y-1.5">
                    <label className="text-xs font-medium text-zinc-400 ml-1">Confirm password</label>
                    <Input
                        type="password"
                        placeholder="••••••••"
                        required
                        value={formData.confirmPassword}
                        onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
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
                                <span>Creating account...</span>
                            </div>
                        ) : (
                            "Get started for free"
                        )}
                    </Button>
                </div>
            </form>

            <div className="pt-4 text-center">
                <p className="text-sm text-zinc-500">
                    Already have an account?{" "}
                    <Link
                        href="/auth/login"
                        className="font-medium text-blue-400 transition-colors hover:text-blue-300"
                    >
                        Sign in
                    </Link>
                </p>
            </div>
        </div>
    );
}
