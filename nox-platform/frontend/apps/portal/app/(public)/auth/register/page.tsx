import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";

export default function RegisterPage() {
    const loading = false as boolean;
    const submitted = false;
    const error: string | null = null;

    if (submitted) {
        return (
            <div className="space-y-4">
                <div className="text-xl font-semibold">Verify your email</div>
                <p className="text-sm text-zinc-400">
                    We&apos;ve sent a verification link to your email. Please check your inbox.
                </p>
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

            <form className="space-y-3">
                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Full name</label>
                    <Input type="text" placeholder="John Doe" required />
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Email</label>
                    <Input type="email" placeholder="you@example.com" required />
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Password</label>
                    <Input type="password" placeholder="Minimum 8 characters" required />
                    <div className="text-xs text-zinc-500">
                        Use at least 8 characters. For best security, include uppercase, lowercase, and numbers.
                    </div>
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Confirm password</label>
                    <Input type="password" placeholder="Repeat your password" required />
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

