"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Folder, Building2, User, Star } from "lucide-react";
import type { ReactNode } from "react";

type NavItem = {
    href: string;
    label: string;
    icon: React.ReactNode;
};

const nav: NavItem[] = [
    { href: "/projects", label: "Projects", icon: <Folder className="h-4 w-4" /> },
    { href: "/organizations", label: "Organizations", icon: <Building2 className="h-4 w-4" /> },
    { href: "/account/profile", label: "Account", icon: <User className="h-4 w-4" /> },
];

function NavLink({ href, label, icon }: NavItem) {
    const pathname = usePathname();
    const active = pathname === href || pathname.startsWith(`${href}/`);

    return (
        <Link
            href={href}
            className={[
                "flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-all duration-200",
                active 
                    ? "sidebar-link active bg-[rgb(var(--accent))] text-[rgb(var(--accent-foreground))] shadow-sm ring-1 ring-[color:var(--card-border)] font-semibold" 
                    : "sidebar-link text-[rgb(var(--text-main))] hover:bg-[rgb(var(--surface))] hover:text-[rgb(var(--text-main))] hover:shadow-sm",
            ].join(" ")}
        >
            {icon}
            {label}
        </Link>
    );
}

export function AppShell({ children }: { children: ReactNode }) {
    return (
        <div className="min-h-screen">
            <div className="mx-auto grid max-w-[1440px] grid-cols-1 gap-8 px-6 py-8 md:grid-cols-[160px_1fr] lg:px-8">
                <aside className="md:sticky md:top-24 md:self-start">
                    <div className="flex flex-col gap-6">
                        <div>
                            <h3 className="mb-3 px-3 text-[11px] font-bold uppercase tracking-widest text-[rgb(var(--muted-foreground))]">
                                Management
                            </h3>
                            <nav className="flex flex-col gap-1.5">
                                {nav.map((item) => (
                                    <NavLink key={item.href} {...item} />
                                ))}
                            </nav>
                        </div>
                        
                        <div className="border-t border-[rgb(var(--border))] pt-4">
                            <h3 className="mb-3 px-3 text-[11px] font-bold uppercase tracking-widest text-[rgb(var(--muted-foreground))]">
                                Starred
                            </h3>
                            <div className="flex flex-col gap-1.5">
                                <Link
                                    href="/projects/1"
                                    className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm text-[rgb(var(--text-main))] hover:bg-[rgb(var(--surface))] transition-all"
                                >
                                    <Star className="h-4 w-4 text-[#38BDF8]" />
                                    <span className="truncate text-xs">Website Redesign</span>
                                </Link>
                            </div>
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
        <div className="flex min-h-screen items-center justify-center px-4 py-12">
            <div className="w-full max-w-md rounded-2xl border border-[rgb(var(--border))] bg-[rgb(var(--surface))] p-6 shadow-[0_0_30px_-20px_rgba(0,0,0,0.08)]">
                {children}
            </div>
        </div>
    );
}

