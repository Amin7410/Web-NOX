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
        <div className="flex min-h-screen items-center justify-center px-4 py-12">
            <div className="w-full max-w-md rounded-2xl border border-white/10 bg-white/5 p-6 shadow-[0_0_30px_-20px_rgba(0,0,0,0.8)]">
                {children}
            </div>
        </div>
    );
}

