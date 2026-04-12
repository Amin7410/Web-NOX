'use client';

import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";
import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

export default function LoginPage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    // Dọn dẹp session cũ khi vào trang login để tránh xung đột tenant
    useEffect(() => {
        document.cookie = "nox_org_id=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
        // Ta không xóa accessToken ở đây nếu nó là HttpOnly, nhưng ta xóa ở cấp độ client nếu có thể
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password }),
            });

            const data = await res.json();

            if (res.ok) {
                // Redirect to organizations or dashboard after successful login
                router.push("/organizations");
                router.refresh();
            } else {
                setError(data.message || "Invalid email or password");
            }
        } catch (err) {
            setError("An error occurred during sign in. Please try again.");
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

            <form className="space-y-3" onSubmit={handleSubmit}>
                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Email</label>
                    <Input 
                        type="email" 
                        placeholder="you@example.com" 
                        required 
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Password</label>
                    <Input 
                        type="password" 
                        placeholder="••••••••" 
                        required 
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
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

