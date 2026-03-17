import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";

export default function ResetPasswordPage() {
    let loading = false as boolean;
    let success = false as boolean;
    let tokenValid = true as boolean;

    if (!tokenValid) {
        return (
            <div className="space-y-4">
                <div className="text-xl font-semibold">Reset link is invalid or expired</div>
                <p className="text-sm text-zinc-400">
                    Please request a new reset link to continue.
                </p>
                <Button asChild className="w-full">
                    <Link href="/auth/forgot-password">Request new reset link</Link>
                </Button>
            </div>
        );
    }

    return (
        <div className="space-y-5">
            <div>
                <div className="text-xl font-semibold">Reset password</div>
                <div className="mt-1 text-sm text-zinc-400">
                    Choose a new password for your account.
                </div>
            </div>

            {success ? (
                <Alert tone="success" title="Password updated" description="You can now sign in." />
            ) : null}

            <form className="space-y-3">
                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">New password</label>
                    <Input type="password" placeholder="New password" required />
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Confirm new password</label>
                    <Input type="password" placeholder="Repeat new password" required />
                </div>

                <Button type="submit" disabled={loading} className="w-full">
                    {loading ? "Resetting..." : "Reset password"}
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

