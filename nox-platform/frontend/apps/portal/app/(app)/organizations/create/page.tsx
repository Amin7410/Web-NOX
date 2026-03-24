import Link from "next/link";
import { Button, Input } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";

export default function CreateOrganizationPage() {
    return (
        <div className="space-y-6">
            <PageHeader title="Create organization" subtitle="Set up a new team for your projects." />

            <div className="max-w-xl">
                <Card title="Organization details">
                    <form className="space-y-3">
                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Organization name</label>
                            <Input placeholder="Acme Inc" required />
                        </div>

                        <div className="space-y-1">
                            <label className="text-sm text-zinc-200">Slug</label>
                            <Input placeholder="acme" required />
                            <div className="text-xs text-zinc-500">
                                Used in URLs and integrations. Lowercase, no spaces.
                            </div>
                        </div>

                        <div className="flex items-center justify-end gap-2 pt-2">
                            <Button asChild variant="outline">
                                <Link href="/organizations">Cancel</Link>
                            </Button>
                            <Button type="submit">Create organization</Button>
                        </div>
                    </form>
                </Card>
            </div>
        </div>
    );
}

