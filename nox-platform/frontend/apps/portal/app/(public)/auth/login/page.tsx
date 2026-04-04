'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Mail, Lock, ArrowRight } from 'lucide-react';
import { Alert } from '../../../_components/UiBits';
import { AuthInput, Logo, PrimaryButton } from '../../../_components/auth';

export default function LoginPage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [keepSignedIn, setKeepSignedIn] = useState(false);
    const [formData, setFormData] = useState({
        email: '',
        password: '',
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

        if (!formData.email.trim()) {
            newErrors.email = 'Email is required';
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
            newErrors.email = 'Please enter a valid email address';
        }

        if (!formData.password) {
            newErrors.password = 'Password is required';
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
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    email: formData.email, 
                    password: formData.password 
                }),
            });

            const data = await res.json();

            if (res.ok) {
                router.push("/organizations");
                router.refresh();
            } else {
                setApiError(data.message || "Invalid email or password");
            }
        } catch (err) {
            setApiError("An error occurred during sign in. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="flex flex-col gap-8">
            <Logo />
            
            <div className="flex flex-col gap-2">
                <h2 className="text-3xl font-black tracking-tight text-zinc-900 dark:text-zinc-100">Welcome back</h2>
                <p className="text-zinc-500 dark:text-zinc-400 font-medium">Sign in to your account to continue</p>
            </div>

            {apiError ? (
                <Alert tone="danger" title="Authentication Error" description={apiError} />
            ) : null}

            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
                <AuthInput
                    label="Email address"
                    type="email"
                    icon={Mail}
                    placeholder="you@example.com"
                    value={formData.email}
                    onChange={handleChange('email')}
                    error={errors.email}
                    autoFocus
                />

                <AuthInput
                    label="Password"
                    type="password"
                    icon={Lock}
                    placeholder="••••••••"
                    value={formData.password}
                    onChange={handleChange('password')}
                    error={errors.password}
                />

                <div className="flex items-center justify-between">
                    <label className="flex items-center gap-2 cursor-pointer group">
                        <div className="relative">
                            <input
                                type="checkbox"
                                checked={keepSignedIn}
                                onChange={(e) => setKeepSignedIn(e.target.checked)}
                                className="w-5 h-5 rounded-lg border-2 border-input appearance-none cursor-pointer
                                         checked:bg-primary checked:border-transparent transition-all duration-200
                                         group-hover:border-primary/50"
                            />
                            {keepSignedIn && (
                                <svg
                                    className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-3 h-3 text-white pointer-events-none"
                                    fill="none"
                                    viewBox="0 0 24 24"
                                    stroke="currentColor"
                                    strokeWidth={3}
                                >
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                                </svg>
                            )}
                        </div>
                        <span className="text-sm font-medium text-muted-foreground select-none">Keep me signed in</span>
                    </label>

                    <Link 
                        href="/auth/forgot-password" 
                        className="text-sm text-primary font-semibold hover:underline transition-colors"
                    >
                        Forgot password?
                    </Link>
                </div>

                <div className="pt-2">
                    <PrimaryButton type="submit" loading={loading} className="gap-2">
                        <span>Sign in</span>
                        <ArrowRight className="w-4 h-4" />
                    </PrimaryButton>
                </div>
            </form>

            <div className="text-center text-sm border-t border-border pt-6">
                <span className="text-muted-foreground">Don&apos;t have an account? </span>
                <Link href="/auth/register" className="font-bold text-primary hover:underline group">
                    Create account
                    <ArrowRight className="inline-block w-3.5 h-3.5 ml-1 transition-transform group-hover:translate-x-1" />
                </Link>
            </div>
        </div>
    );
}

