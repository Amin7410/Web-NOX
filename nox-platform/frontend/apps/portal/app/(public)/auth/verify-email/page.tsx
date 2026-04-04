"use client";

import { useState, useEffect, Suspense } from 'react';
import Link from 'next/link';
import { useSearchParams, useRouter } from 'next/navigation';
import { ShieldCheck, Loader2, ArrowLeft } from 'lucide-react';
import { PrimaryButton, InputOTP, InputOTPGroup, InputOTPSlot, Logo } from '../../../_components/auth';

function VerifyEmailContent() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const emailParam = searchParams.get("email") || 'your@email.com';
    
    const [otp, setOtp] = useState('');
    const [loading, setLoading] = useState(false);
    const [resendTimer, setResendTimer] = useState(60);
    const [canResend, setCanResend] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    useEffect(() => {
        if (resendTimer > 0) {
            const timer = setTimeout(() => setResendTimer(resendTimer - 1), 1000);
            return () => clearTimeout(timer);
        } else {
            setCanResend(true);
        }
    }, [resendTimer]);

    const handleResend = async () => {
        if (!canResend) return;
        setCanResend(false);
        setResendTimer(60);
        
        // In a real app, this would call /api/auth/resend-otp
        alert('New verification code sent!');
    };

    const handleVerify = async (e: React.FormEvent) => {
        e.preventDefault();
        
        if (otp.length !== 6) {
            setError('Please enter the complete 6-digit code');
            return;
        }

        setLoading(true);
        setError('');
        
        try {
            const res = await fetch("/api/auth/verify-email", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: emailParam, otpCode: otp }),
            });

            if (res.ok) {
                setSuccess(true);
            } else {
                const data = await res.json().catch(() => ({}));
                setError(data.message || "Invalid or expired code. Please try again.");
            }
        } catch (err: any) {
            setError("An unexpected error occurred. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    if (success) {
        return (
            <div className="flex flex-col gap-6 text-center animate-in fade-in zoom-in duration-300">
                <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400 font-bold">
                    <ShieldCheck className="w-10 h-10" />
                </div>
                <div className="flex flex-col gap-2">
                    <h1 className="text-2xl font-black tracking-tight text-zinc-900 dark:text-zinc-100 uppercase">Verified</h1>
                    <p className="text-zinc-500 dark:text-zinc-400 font-medium">
                        Your email has been confirmed. You can now access your account.
                    </p>
                </div>
                <PrimaryButton onClick={() => router.push('/auth/login')}>
                    Sign in to Portal
                </PrimaryButton>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-8 text-center animate-in fade-in duration-500">
            <div className="flex justify-center">
                <Logo />
            </div>
            
            <div>
                <div className="w-16 h-16 mx-auto mb-6 rounded-2xl bg-blue-600/10 dark:bg-blue-500/10 flex items-center justify-center text-blue-600 dark:text-blue-400 shadow-inner">
                    <ShieldCheck className="w-8 h-8" />
                </div>
                <h2 className="text-3xl font-black tracking-tight text-zinc-900 dark:text-zinc-100 uppercase">Verify Identity</h2>
                <p className="text-zinc-500 dark:text-zinc-400 font-medium mt-2 leading-relaxed">
                    Enter the 6-digit code sent to
                </p>
                <p className="text-zinc-900 dark:text-zinc-100 font-bold mt-1">{emailParam}</p>
            </div>

            <form onSubmit={handleVerify} className="flex flex-col gap-8">
                <div className="flex flex-col gap-4">
                    <label className="text-sm font-bold text-muted-foreground uppercase tracking-widest">
                        Verification Code
                    </label>
                    <div className="flex justify-center">
                        <InputOTP
                            maxLength={6}
                            value={otp}
                            onChange={(value: string) => {
                                setOtp(value);
                                setError('');
                            }}
                        >
                            <InputOTPGroup>
                                <InputOTPSlot index={0} />
                                <InputOTPSlot index={1} />
                                <InputOTPSlot index={2} />
                                <InputOTPSlot index={3} />
                                <InputOTPSlot index={4} />
                                <InputOTPSlot index={5} />
                            </InputOTPGroup>
                        </InputOTP>
                    </div>
                    {error && (
                        <p className="text-sm text-destructive font-medium animate-bounce">{error}</p>
                    )}
                </div>

                <div className="text-center">
                    <p className="text-sm text-muted-foreground">Didn&apos;t receive the code?</p>
                    <button
                        type="button"
                        onClick={handleResend}
                        disabled={!canResend}
                        className={`text-sm font-bold mt-1 transition-colors ${
                            canResend
                                ? 'text-primary hover:text-primary/80 cursor-pointer'
                                : 'text-muted-foreground cursor-not-allowed'
                        }`}
                    >
                        {canResend ? 'Resend code' : `Resend in ${resendTimer}s`}
                    </button>
                </div>

                <PrimaryButton type="submit" loading={loading} disabled={otp.length !== 6}>
                    Verify identity
                </PrimaryButton>
            </form>

            <div className="border-t border-border pt-6">
                <Link 
                    href="/auth/login" 
                    className="text-sm font-bold text-muted-foreground hover:text-foreground transition-colors inline-flex items-center gap-2 group"
                >
                    <ArrowLeft className="w-4 h-4 transition-transform group-hover:-translate-x-1" />
                    Back to login
                </Link>
            </div>
        </div>
    );
}

export default function VerifyEmailPage() {
    return (
        <Suspense fallback={
            <div className="p-8 text-center flex flex-col items-center gap-4">
                <Loader2 className="w-10 h-10 animate-spin text-primary" />
                <p className="text-muted-foreground font-medium">Loading verification...</p>
            </div>
        }>
            <VerifyEmailContent />
        </Suspense>
    );
}
