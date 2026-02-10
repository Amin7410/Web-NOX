import { Button } from "@nox/ui";

export default function Home() {
    return (
        <main style={{ padding: "20px", fontFamily: "sans-serif" }}>
            <h1>Welcome to NOX Portal</h1>
            <p>This is a Next.js 14 application using a shared UI library.</p>
            <div style={{ marginTop: "20px" }}>
                <Button>Click me (From @nox/ui)</Button>
            </div>
        </main>
    );
}
