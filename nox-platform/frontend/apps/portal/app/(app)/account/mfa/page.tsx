import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";

export default function AccountMfaPage() {
    const enabled = false;

    return (
        <div className="space-y-6">
            <PageHeader title="Multi-factor authentication" subtitle="Add an extra layer of security to your account." />

            <Card title="Status">
                <div className="text-sm">
                    MFA is currently:{" "}
                    <span className={enabled ? "text-emerald-300" : "text-zinc-300"}>
                        {enabled ? "Enabled" : "Disabled"}
                    </span>
                </div>
            </Card>

            {!enabled ? (
                <Card
                    title="Enable MFA"
                    description="Scan the QR code with an authenticator app, then enter the 6-digit code."
                >
                    <div className="grid gap-4 md:grid-cols-2">
                        <div className="rounded-xl border border-white/10 bg-black/20 p-4 text-sm text-zinc-400">
                            QR code placeholder
                        </div>
                        <div className="space-y-3">
                            <Button variant="outline">Set up authenticator app</Button>
                            <div className="space-y-1">
                                <label className="text-sm text-zinc-200">6-digit verification code</label>
                                <Input placeholder="123456" />
                            </div>
                            <Button>Verify &amp; enable</Button>
                        </div>
                    </div>
                </Card>
            ) : (
                <>
                    <Card title="Backup codes" description="Store these codes in a safe place. Each code can be used once.">
                        <div className="grid gap-2 text-sm md:grid-cols-2">
                            <div className="rounded-lg border border-white/10 bg-black/20 px-3 py-2">ABCD-****-1234</div>
                            <div className="rounded-lg border border-white/10 bg-black/20 px-3 py-2">EFGH-****-5678</div>
                        </div>
                        <div className="mt-4 flex items-center gap-2">
                            <Button variant="outline">Show backup codes</Button>
                            <Button variant="outline">Generate new backup codes</Button>
                        </div>
                    </Card>

                    <Card title="Disable MFA" description="Your account will be protected by password only.">
                        <Button variant="destructive">Disable MFA</Button>
                    </Card>
                </>
            )}
        </div>
    );
}

