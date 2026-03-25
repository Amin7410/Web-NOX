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
        <div className="flex min-h-screen items-center justify-center px-4 py-12">
            <div className="w-full max-w-md rounded-2xl border border-white/10 bg-white/5 p-6 shadow-[0_0_30px_-20px_rgba(0,0,0,0.8)]">
                {children}
            </div>
        </div>
    );
}

