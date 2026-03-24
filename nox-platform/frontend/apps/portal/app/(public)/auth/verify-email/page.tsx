import Link from "next/link";
import { Button } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";

export default function VerifyEmailPage() {
    let state = "loading" as string;

    return (
        <div className="space-y-5">
            <div className="text-xl font-semibold">Email verification</div>

            {state === "loading" ? (
                <Alert tone="neutral" title="Verifying your email..." description="Please wait." />
            ) : null}

            {state === "success" ? (
                <>
                    <Alert
                        tone="success"
                        title="Your email has been verified"
                        description="You can now sign in to your account."
                    />
                    <Button asChild className="w-full">
                        <Link href="/auth/login">Go to sign in</Link>
                    </Button>
                </>
            ) : null}

            {state === "error" ? (
                <>
                    <Alert
                        tone="danger"
                        title="Verification link is invalid or expired"
                        description="Please request a new verification email or try again."
                    />
                    <Button asChild variant="outline" className="w-full">
                        <Link href="/auth/login">Back to sign in</Link>
                    </Button>
                </>
            ) : null}
        </div>
    );
}

