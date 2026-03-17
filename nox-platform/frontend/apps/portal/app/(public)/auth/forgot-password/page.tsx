import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";

export default function ForgotPasswordPage() {
    let loading = false as boolean;
    let sent = false as boolean;
    const error: string | null = null;

    return (
        <div className="space-y-5">
            <div>
                <div className="text-xl font-semibold">Forgot password</div>
                <div className="mt-1 text-sm text-zinc-400">
                    Enter your email and we&apos;ll send you a link to reset your password.
                </div>
            </div>

            {sent ? (
                <Alert
                    tone="success"
                    title="Check your inbox"
                    description="If an account with this email exists, we have sent a reset link."
                />
            ) : null}

            {error ? <Alert tone="danger" title="Could not send email" description={error} /> : null}

            <form className="space-y-3">
                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Email</label>
                    <Input type="email" placeholder="you@example.com" required />
                </div>

                <Button type="submit" disabled={loading} className="w-full">
                    {loading ? "Sending..." : "Send reset link"}
                </Button>
            </form>

            <div className="text-sm text-zinc-400">
                <Link href="/auth/login" className="text-blue-400 hover:text-blue-300">
                    Back to sign in
                </Link>
            </div>
        </div>
    );
}

