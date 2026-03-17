import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";

export default function LoginPage() {
    const loading = false as boolean;
    const error: string | null = null;

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

            <form className="space-y-3">
                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Email</label>
                    <Input type="email" placeholder="you@example.com" required />
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Password</label>
                    <Input type="password" placeholder="••••••••" required />
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

