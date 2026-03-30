import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
    title: "NOX Portal",
    description: "Multi-tenant Portal for NOX Platform",
};

export default function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en" className="dark">
            <body className="bg-zinc-950 text-white selection:bg-blue-500/30 antialiased">
                {children}
            </body>
        </html>
    );
}
