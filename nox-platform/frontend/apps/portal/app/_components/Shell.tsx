"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";

type NavItem = {
    href: string;
    label: string;
};

const nav: NavItem[] = [
    { href: "/projects", label: "Projects" },
    { href: "/organizations", label: "Organizations" },
    { href: "/account/profile", label: "Account" },
];

function NavLink({ href, label }: NavItem) {
    const pathname = usePathname();
    const active = pathname === href || pathname.startsWith(`${href}/`);

    return (
        <Link
            href={href}
            className={[
                "flex items-center rounded-lg px-3 py-2 text-sm transition-all duration-200",
                active 
                    ? "bg-white text-[#4F46E5] shadow-sm ring-1 ring-gray-200 font-semibold" 
                    : "text-gray-600 hover:bg-white hover:text-gray-900 hover:shadow-sm",
            ].join(" ")}
        >
            {label}
        </Link>
    );
}

export function AppShell({ children }: { children: ReactNode }) {
    return (
        <div className="min-h-screen">
            <div className="mx-auto grid max-w-[1440px] grid-cols-1 gap-8 px-6 py-8 md:grid-cols-[240px_1fr] lg:px-8">
                <aside className="md:sticky md:top-24 md:self-start">
                    <div className="flex flex-col gap-6">
                        <div>
                            <h3 className="mb-3 px-3 text-[11px] font-bold uppercase tracking-widest text-gray-400">
                                Management
                            </h3>
                            <nav className="flex flex-col gap-1.5">
                                {nav.map((item) => (
                                    <NavLink key={item.href} {...item} />
                                ))}
                            </nav>
                        </div>
                    </div>
                </aside>

                <main className="min-w-0 bg-transparent">
                    {children}
                </main>
            </div>
        </div>
    );
}

export function PublicShell({ children }: { children: ReactNode }) {
    return (
        <div className="relative flex min-h-screen items-center justify-center px-4 py-20 overflow-hidden bg-slate-50/50 dark:bg-zinc-950">
            {/* Ambient Background Decorative Elements */}
            <div className="absolute top-0 -left-4 w-72 h-72 bg-blue-500 rounded-full mix-blend-multiply filter blur-[128px] opacity-10 animate-blob"></div>
            <div className="absolute top-0 -right-4 w-72 h-72 bg-purple-500 rounded-full mix-blend-multiply filter blur-[128px] opacity-10 animate-blob animation-delay-2000"></div>
            <div className="absolute -bottom-8 left-20 w-72 h-72 bg-indigo-500 rounded-full mix-blend-multiply filter blur-[128px] opacity-10 animate-blob animation-delay-4000"></div>
            
            <div className="relative w-full max-w-md">
                <div className="overflow-hidden rounded-3xl border border-border bg-card p-8 shadow-[0_20px_50px_-20px_rgba(0,0,0,0.1)] dark:shadow-[0_20px_50px_-20px_rgba(0,0,0,0.5)]">
                    {children}
                </div>
                
                <div className="mt-8 text-center text-sm text-muted-foreground">
                    &copy; {new Date().getFullYear()} NOX Platform. All rights reserved.
                </div>
            </div>
        </div>
    );
}
