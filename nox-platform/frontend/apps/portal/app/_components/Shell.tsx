"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { Button } from "@nox/ui";

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
                "rounded-lg px-3 py-2 text-sm transition",
                active ? "bg-white/10 text-white" : "text-zinc-300 hover:bg-white/5 hover:text-white",
            ].join(" ")}
        >
            {label}
        </Link>
    );
}

export function AppShell({ children }: { children: ReactNode }) {
    return (
        <div className="min-h-screen">
            <header className="sticky top-0 z-10 border-b border-white/10 bg-zinc-950/80 backdrop-blur">
                <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
                    <div className="flex items-center gap-3">
                        <div className="h-8 w-8 rounded-lg bg-blue-600/20 ring-1 ring-blue-500/30" />
                        <div className="leading-tight">
                            <div className="text-sm font-semibold">NOX Portal</div>
                            <div className="text-xs text-zinc-400">Customer dashboard</div>
                        </div>
                    </div>

                    <div className="flex items-center gap-2">
                        <Button variant="outline" size="sm">
                            Sign out
                        </Button>
                    </div>
                </div>
            </header>

            <div className="mx-auto grid max-w-6xl grid-cols-1 gap-6 px-4 py-6 md:grid-cols-[220px_1fr]">
                <aside className="md:sticky md:top-[72px] md:self-start">
                    <nav className="flex flex-col gap-1 rounded-xl border border-white/10 bg-white/5 p-2">
                        {nav.map((item) => (
                            <NavLink key={item.href} {...item} />
                        ))}
                    </nav>
                </aside>

                <main className="min-w-0">{children}</main>
            </div>
        </div>
    );
}

export function PublicShell({ children }: { children: ReactNode }) {
    return (
        <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-zinc-950 px-4 py-12 selection:bg-blue-500/30">
            {/* Background Effects */}
            <div className="absolute inset-0 z-0">
                <div className="absolute -left-[10%] -top-[10%] h-[40%] w-[40%] rounded-full bg-blue-600/20 blur-[120px]" />
                <div className="absolute -right-[10%] -bottom-[10%] h-[40%] w-[40%] rounded-full bg-purple-600/10 blur-[120px]" />
            </div>

            <div className="relative z-10 w-full max-w-md">
                <div className="mb-8 flex justify-center">
                    <div className="flex items-center gap-3">
                        <div className="h-10 w-10 rounded-xl bg-gradient-to-br from-blue-500 to-blue-700 p-0.5 shadow-lg shadow-blue-500/20">
                            <div className="h-full w-full rounded-[10px] bg-zinc-950 flex items-center justify-center">
                                <div className="h-5 w-5 rounded-sm bg-blue-500" />
                            </div>
                        </div>
                        <div className="text-xl font-bold tracking-tight text-white">NOX</div>
                    </div>
                </div>

                <div className="rounded-2xl border border-white/10 bg-zinc-900/50 p-8 shadow-2xl backdrop-blur-xl ring-1 ring-white/5">
                    {children}
                </div>

                <div className="mt-8 text-center text-xs text-zinc-500">
                    &copy; {new Date().getFullYear()} NOX Platform. All rights reserved.
                </div>
            </div>
        </div>
    );
}


