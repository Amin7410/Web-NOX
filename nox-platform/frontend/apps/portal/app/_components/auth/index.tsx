"use client";

import React from "react";
import { LucideIcon, Check } from "lucide-react";
import { Button, Input } from "@nox/ui";

// --- Logo Component ---
export function Logo() {
    return (
        <div className="flex items-center gap-2 mb-8">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600 dark:bg-blue-500 text-white shadow-lg shadow-blue-500/20">
                <span className="text-xl font-bold italic">N</span>
            </div>
            <span className="text-2xl font-black tracking-tighter text-zinc-900 dark:text-zinc-100 uppercase">nox</span>
        </div>
    );
}

// --- AuthInput Component ---
interface AuthInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label: string;
    icon?: LucideIcon;
    error?: string;
}

export const AuthInput = React.forwardRef<HTMLInputElement, AuthInputProps>(
    ({ label, icon: Icon, error, className, size: _size, ...props }, ref) => {
        return (
            <div className="flex flex-col gap-2">
                <label className="text-sm font-semibold text-zinc-700 dark:text-zinc-300 flex items-center gap-2">
                    {Icon && <Icon className="w-3.5 h-3.5" />}
                    {label}
                </label>
                <Input 
                    ref={ref}
                    error={!!error}
                    className={className}
                    {...props}
                />
                {error && <p className="text-xs text-destructive font-medium">{error}</p>}
            </div>
        );
    }
);
AuthInput.displayName = "AuthInput";

// --- PrimaryButton Component ---
export function PrimaryButton({ children, loading, disabled, ...props }: any) {
    return (
        <Button 
            className="w-full" 
            disabled={loading || disabled} 
            {...props}
        >
            {loading ? "Please wait..." : children}
        </Button>
    );
}

// --- PasswordStrength Component ---
export function PasswordStrength({ password }: { password?: string }) {
    if (!password) return null;

    const checks = [
        { label: "At least 8 characters", met: password.length >= 8 },
        { label: "Contains uppercase", met: /[A-Z]/.test(password) },
        { label: "Contains numbers", met: /[0-9]/.test(password) },
        { label: "Contains special characters", met: /[^A-Za-z0-9]/.test(password) },
    ];

    const metCount = checks.filter(c => c.met).length;
    const strengthColor = 
        metCount <= 1 ? "bg-red-500" : 
        metCount <= 3 ? "bg-yellow-500" : 
        "bg-green-500";

    return (
        <div className="mt-3 flex flex-col gap-2">
            <div className="h-1.5 w-full bg-border rounded-full overflow-hidden">
                <div 
                    className={`h-full ${strengthColor} transition-all duration-500`} 
                    style={{ width: `${(metCount / 4) * 100}%` }}
                />
            </div>
            <div className="grid grid-cols-2 gap-x-4 gap-y-1">
                {checks.map((check, i) => (
                    <div key={i} className="flex items-center gap-1.5 text-[10px]">
                        {check.met ? (
                            <Check className="w-3 h-3 text-green-500" />
                        ) : (
                            <div className="w-3 h-3 rounded-full border border-border" />
                        )}
                        <span className={check.met ? "text-foreground" : "text-muted-foreground"}>
                            {check.label}
                        </span>
                    </div>
                ))}
            </div>
        </div>
    );
}

// --- InputOTP Components (Simplified) ---
export function InputOTP({ value, onChange, maxLength, children }: any) {
    return (
        <div className="relative">
            <input
                type="text"
                value={value}
                onChange={(e) => {
                    const val = e.target.value.replace(/[^0-9]/g, "").slice(0, maxLength);
                    onChange(val);
                }}
                className="absolute inset-0 opacity-0 cursor-default"
                maxLength={maxLength}
                autoFocus
            />
            <div className="flex gap-2">
                {React.Children.map(children, (child: any) => 
                    React.cloneElement(child, { otpValue: value })
                )}
            </div>
        </div>
    );
}

export function InputOTPGroup({ children, otpValue }: any) {
    return (
        <div className="flex gap-2">
            {React.Children.map(children, (child, index) => 
                React.cloneElement(child, { otpValue, index })
            )}
        </div>
    );
}

export function InputOTPSlot({ index, otpValue, className }: any) {
    const char = otpValue?.[index] || "";
    const isFocused = otpValue?.length === index;

    return (
        <div 
            className={`
                w-12 h-14 flex items-center justify-center text-xl font-bold rounded-2xl border-2 transition-all duration-200 bg-zinc-50 dark:bg-zinc-900/50 text-zinc-900 dark:text-zinc-100
                ${isFocused ? "border-primary ring-4 ring-primary/10" : "border-input"}
                ${className}
            `}
        >
            {char}
            {isFocused && (
                <div className="absolute w-px h-6 bg-primary animate-pulse" />
            )}
        </div>
    );
}
