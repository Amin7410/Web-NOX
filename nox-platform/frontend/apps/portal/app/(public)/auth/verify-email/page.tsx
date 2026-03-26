"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";

export default function VerifyEmailPage() {
    const searchParams = useSearchParams();
    const emailParam = searchParams.get("email");
    const otpCodeParam = searchParams.get("otpCode") || searchParams.get("code");

    const [state, setState] = useState<"idle" | "loading" | "success" | "error">("idle");
    const [errorMessage, setErrorMessage] = useState("");

    const [email, setEmail] = useState(emailParam || "");
    const [otpCode, setOtpCode] = useState(otpCodeParam || "");

    useEffect(() => {
        // Auto-verify if both exist in URL
        if (emailParam && otpCodeParam && state === "idle") {
            verify(emailParam, otpCodeParam);
        }
    }, [emailParam, otpCodeParam]);

    const verify = async (targetEmail: string, targetOtp: string) => {
        setState("loading");
        try {
            const res = await fetch("/api/auth/verify-email", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: targetEmail, otpCode: targetOtp }),
            });

            if (res.ok) {
                setState("success");
            } else {
                const data = await res.json().catch(() => ({}));
                setState("error");
                setErrorMessage(data.message || data.error || "Verification failed");
            }
        } catch (err: any) {
            setState("error");
            setErrorMessage(err.message || "An unexpected error occurred");
        }
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (email && otpCode) {
            verify(email, otpCode);
        }
    };

    return (
        <div className="space-y-5">
            <div className="text-xl font-semibold">Email verification</div>

            {state === "loading" && (
                <Alert tone="neutral" title="Verifying your email..." description="Please wait." />
            )}

            {state === "success" && (
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
            )}

            {state === "idle" || state === "error" ? (
                <>
                    {state === "error" && (
                        <Alert
                            tone="danger"
                            title="Verification failed"
                            description={errorMessage || "Invalid or expired OTP. Please try again."}
                        />
                    )}
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Email</label>
                            <Input 
                                type="email" 
                                required 
                                value={email} 
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="you@example.com"
                                readOnly={!!emailParam}
                            />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Verification Code (OTP)</label>
                            <Input 
                                type="text" 
                                required 
                                value={otpCode} 
                                onChange={(e) => setOtpCode(e.target.value)}
                                placeholder="Enter 6-digit code"
                            />
                        </div>
                        <Button type="submit" disabled={state === "loading"} className="w-full">
                            Verify Email
                        </Button>
                    </form>
                    <div className="text-center mt-2">
                        <Button asChild variant="outline" className="w-full">
                            <Link href="/auth/login">Back to sign in</Link>
                        </Button>
                    </div>
                </>
            ) : null}
        </div>
    );
}

