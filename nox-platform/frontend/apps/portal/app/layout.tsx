import type { Metadata } from "next";
import "./globals.css";
import { Header } from "./Header";
import { ThemeProvider } from "./ThemeProvider";

export const metadata: Metadata = {
    title: "NOX Portal",
    description: "Manage your projects and collaborate with your team",
};

export default function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en">
            <body className="antialiased bg-[rgb(var(--background))] text-[rgb(var(--foreground))]">
                <ThemeProvider>
                    <Header />
                    <main className="min-h-screen bg-[rgb(var(--background))]">
                        {children}
                    </main>
                </ThemeProvider>
            </body>
        </html>
    );
}
