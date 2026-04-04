"use client";

import Link from "next/link";
import { useState } from "react";
import { User, Mail, Lock, ShieldCheck, ArrowRight, Sparkles } from "lucide-react";
import { Button } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";
import { AuthInput, Logo, PasswordStrength, PrimaryButton } from "../../../_components/auth";

export default function RegisterPage() {
    const [loading, setLoading] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        password: '',
        confirmPassword: '',
    });
    const [errors, setErrors] = useState<Record<string, string>>({});
    const [apiError, setApiError] = useState<string | null>(null);

    const handleChange = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [field]: e.target.value });
        if (errors[field]) {
            const newErrors = { ...errors };
            delete newErrors[field];
            setErrors(newErrors);
        }
    };

    const validateForm = () => {
        const newErrors: Record<string, string> = {};

        if (!formData.fullName.trim()) {
            newErrors.fullName = 'Full name is required';
        }

        if (!formData.email.trim()) {
            newErrors.email = 'Email is required';
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
            newErrors.email = 'Please enter a valid email address';
        }

        if (!formData.password) {
            newErrors.password = 'Password is required';
        } else if (formData.password.length < 8) {
            newErrors.password = 'Password must be at least 8 characters';
        }

        if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Passwords do not match';
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        if (!validateForm()) return;

        setLoading(true);
        setApiError(null);

        try {
            const res = await fetch("/api/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ 
                    email: formData.email, 
                    fullName: formData.fullName, 
                    password: formData.password 
                }),
            });

            const data = await res.json();
            if (!res.ok) {
                const errorMsg = data.error?.message || data.message || "Failed to register";
                setApiError(typeof errorMsg === 'string' ? errorMsg : JSON.stringify(errorMsg));
            } else {
                setSubmitted(true);
            }
        } catch (err: any) {
            setApiError(err.message || "An unexpected error occurred");
        } finally {
            setLoading(false);
        }
    };

    if (submitted) {
        return (
            <div className="flex flex-col gap-6 text-center animate-in fade-in zoom-in duration-500">
                <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-green-100 dark:bg-green-900/30 text-green-600 dark:text-green-400">
                    <ShieldCheck className="w-8 h-8" />
                </div>
                <div className="flex flex-col gap-2">
                    <h1 className="text-2xl font-black tracking-tight text-zinc-900 dark:text-zinc-100">Verify your email</h1>
                    <p className="text-zinc-500 dark:text-zinc-400 font-medium leading-relaxed px-4">
                        We&apos;ve sent a verification link with an OTP to your email. Please check your inbox.
                    </p>
                </div>
                <div className="text-xs p-4 bg-zinc-50 dark:bg-muted/50 rounded-xl border border-border flex flex-col gap-2 text-left">
                    <div className="font-bold flex items-center gap-1.5 text-zinc-900 dark:text-zinc-100">
                        <Sparkles className="w-3.5 h-3.5" />
                        Developer Tip
                    </div>
                    <p className="text-zinc-500 dark:text-zinc-400">
                        You can view sent emails in MailHog at: 
                        <a href="http://localhost:8025" target="_blank" className="text-primary font-bold hover:underline block mt-1">
                            http://localhost:8025
                        </a>
                    </p>
                </div>
                <Button asChild className="w-full mt-2">
                    <Link href={`/auth/verify-email?email=${formData.email}`}>
                        Continue to verify
                    </Link>
                </Button>
            </div>
        );
    }

    return (
        <div className="flex flex-col gap-8">
            <Logo />
            
            <div className="flex flex-col gap-2">
                <h2 className="text-3xl font-black tracking-tight text-zinc-900 dark:text-zinc-100">Create account</h2>
                <p className="text-zinc-500 dark:text-zinc-400 font-medium leading-relaxed">Get started with NOX Platform today</p>
            </div>

            {apiError ? (
                <Alert tone="danger" title="Registration error" description={apiError} />
            ) : null}

            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
                <AuthInput
                    label="Full name"
                    icon={User}
                    placeholder="John Doe"
                    value={formData.fullName}
                    onChange={handleChange('fullName')}
                    error={errors.fullName}
                    autoComplete="name"
                />

                <AuthInput
                    label="Email address"
                    type="email"
                    icon={Mail}
                    placeholder="you@example.com"
                    value={formData.email}
                    onChange={handleChange('email')}
                    error={errors.email}
                    autoComplete="email"
                />

                <div className="flex flex-col gap-1">
                    <AuthInput
                        label="Password"
                        type="password"
                        icon={Lock}
                        placeholder="Minimum 8 characters"
                        value={formData.password}
                        onChange={handleChange('password')}
                        error={errors.password}
                        autoComplete="new-password"
                    />
                    <PasswordStrength password={formData.password} />
                </div>

                <AuthInput
                    label="Confirm password"
                    type="password"
                    icon={Lock}
                    placeholder="Repeat your password"
                    value={formData.confirmPassword}
                    onChange={handleChange('confirmPassword')}
                    error={errors.confirmPassword}
                    autoComplete="new-password"
                />

                <div className="pt-2">
                    <p className="text-xs text-muted-foreground mb-4 leading-relaxed">
                        Password must contain at least 8 characters, including uppercase, lowercase, numbers, and special characters.
                    </p>
                    
                    <PrimaryButton type="submit" loading={loading} className="gap-2">
                        <span>Create account</span>
                        <ArrowRight className="w-4 h-4" />
                    </PrimaryButton>
                </div>
            </form>

            <div className="text-center text-sm border-t border-border pt-6">
                <span className="text-muted-foreground">Already have an account? </span>
                <Link href="/auth/login" className="font-bold text-primary hover:underline">
                    Sign in
                </Link>
            </div>
        </div>
    );
}
