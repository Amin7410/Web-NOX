import type { Metadata } from "next";
import "./globals.css";
import { Header } from "./Header";

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
            <body className="antialiased">
                <Header />
                <main className="min-h-screen bg-gray-50">
                    {children}
                </main>
            </body>
        </html>
    );
}
