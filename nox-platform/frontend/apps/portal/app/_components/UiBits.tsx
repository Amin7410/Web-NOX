"use client";

import type { ReactNode } from "react";

export function PageHeader({
    title,
    subtitle,
    actions,
}: {
    title: string;
    subtitle?: string;
    actions?: ReactNode;
}) {
    return (
        <div className="flex flex-col gap-3 border-b border-gray-200 dark:border-white/10 pb-4 md:flex-row md:items-end md:justify-between">
            <div>
                <h1 className="text-2xl font-bold tracking-tight text-gray-900 dark:text-zinc-100">{title}</h1>
                {subtitle ? <p className="mt-1 text-sm text-gray-500 dark:text-zinc-400">{subtitle}</p> : null}
            </div>
            {actions ? <div className="flex items-center gap-2">{actions}</div> : null}
        </div>
    );
}

export function Card({
    title,
    description,
    children,
    footer,
}: {
    title?: string;
    description?: string;
    children: ReactNode;
    footer?: ReactNode;
}) {
    return (
        <section className="rounded-2xl border border-gray-200 bg-white dark:border-white/10 dark:bg-white/5 p-5 shadow-sm">
            {title ? (
                <header className="mb-4">
                    <div className="text-sm font-bold text-gray-900 dark:text-zinc-100 uppercase tracking-wider">{title}</div>
                    {description ? <div className="mt-1 text-sm text-gray-500 dark:text-zinc-400">{description}</div> : null}
                </header>
            ) : null}
            <div>{children}</div>
            {footer ? <footer className="mt-4 border-t border-gray-200 dark:border-white/10 pt-4">{footer}</footer> : null}
        </section>
    );
}

export function Alert({
    tone = "neutral",
    title,
    description,
}: {
    tone?: "neutral" | "danger" | "success";
    title: string;
    description?: string;
}) {
    const tones = {
        neutral: "border-white/10 bg-white/5 text-zinc-100",
        danger: "border-red-500/20 bg-red-500/10 text-red-200",
        success: "border-emerald-500/20 bg-emerald-500/10 text-emerald-100",
    };

    return (
        <div className={["rounded-xl border p-3", tones[tone]].join(" ")}>
            <div className="text-sm font-medium">{title}</div>
            {description ? <div className="mt-1 text-sm opacity-90">{description}</div> : null}
        </div>
    );
}

