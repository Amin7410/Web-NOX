"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button, Input } from "@nox/ui";
import { Alert } from "../../../_components/UiBits";
import { AuthApi } from "../../../_lib/api-real";

export default function RegisterPage() {
    const router = useRouter();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [submitted, setSubmitted] = useState(false);

    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        confirmPassword: "",
    });

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        // Validation
        if (formData.password !== formData.confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        if (formData.password.length < 8) {
            setError("Password must be at least 8 characters");
            return;
        }

        setLoading(true);

        try {
            await AuthApi.register({
                email: formData.email,
                password: formData.password,
                firstName: formData.firstName,
                lastName: formData.lastName,
            });
            // Redirect to login immediately after successful registration
            router.push("/auth/login");
        } catch (err: any) {
            setError(err.message || "Registration failed");
            setLoading(false);
        }
    };

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

            <form onSubmit={handleSubmit} className="space-y-3">
                <div className="grid grid-cols-2 gap-3">
                    <div className="space-y-1">
                        <label className="text-sm text-zinc-200">First name</label>
                        <Input 
                            type="text" 
                            name="firstName"
                            placeholder="John" 
                            value={formData.firstName}
                            onChange={handleInputChange}
                            required 
                        />
                    </div>
                    <div className="space-y-1">
                        <label className="text-sm text-zinc-200">Last name</label>
                        <Input 
                            type="text" 
                            name="lastName"
                            placeholder="Doe" 
                            value={formData.lastName}
                            onChange={handleInputChange}
                            required 
                        />
                    </div>
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Email</label>
                    <Input 
                        type="email" 
                        name="email"
                        placeholder="you@example.com" 
                        value={formData.email}
                        onChange={handleInputChange}
                        required 
                    />
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Password</label>
                    <Input 
                        type="password" 
                        name="password"
                        placeholder="Minimum 8 characters" 
                        value={formData.password}
                        onChange={handleInputChange}
                        required 
                    />
                    <div className="text-xs text-zinc-500">
                        Use at least 8 characters. For best security, include uppercase, lowercase, and numbers.
                    </div>
                </div>

                <div className="space-y-1">
                    <label className="text-sm text-zinc-200">Confirm password</label>
                    <Input 
                        type="password" 
                        name="confirmPassword"
                        placeholder="Repeat your password" 
                        value={formData.confirmPassword}
                        onChange={handleInputChange}
                        required 
                    />
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

