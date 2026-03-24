import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";

export default function AccountSecurityPage() {
    return (
        <div className="space-y-6">
            <PageHeader title="Security" subtitle="Change your password and review security status." />

            <div className="grid gap-6 md:grid-cols-2">
                <Card title="Change password" description="Use a strong password you haven't used elsewhere.">
                    <div className="space-y-3">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Current password</label>
                            <Input type="password" placeholder="Current password" />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">New password</label>
                            <Input type="password" placeholder="New password" />
                        </div>
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Confirm new password</label>
                            <Input type="password" placeholder="Confirm new password" />
                        </div>
                    </div>

                    <div className="mt-5 flex items-center justify-end">
                        <Button>Change password</Button>
                    </div>
                </Card>

                <Card title="Security overview" description="Quick summary of your account security.">
                    <div className="space-y-3 text-sm">
                        <div className="flex items-center justify-between">
                            <div className="text-zinc-400">Email</div>
                            <div>Verified</div>
                        </div>
                        <div className="flex items-center justify-between">
                            <div className="text-zinc-400">Multi-factor authentication</div>
                            <Link className="text-blue-400 hover:text-blue-300" href="/account/mfa">
                                Manage
                            </Link>
                        </div>
                        <div className="flex items-center justify-between">
                            <div className="text-zinc-400">Last login</div>
                            <div>—</div>
                        </div>
                    </div>
                </Card>
            </div>
        </div>
    );
}

