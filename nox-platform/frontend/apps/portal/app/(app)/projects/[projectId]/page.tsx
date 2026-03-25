import Link from "next/link";
import { Button } from "@nox/ui";
import { Card, PageHeader } from "../../../_components/UiBits";

export default function ProjectOverviewPage({ params }: { params: { projectId: string } }) {
  const project = {
    id: params.projectId,
    name: "Payment Engine",
    slug: "payment-engine",
    orgId: "org_1",
    orgName: "Acme Inc",
    visibility: "ORG",
    status: "ACTIVE",
    updatedAt: "2026-03-16",
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title={project.name}
        subtitle={`${project.orgName} · ${project.visibility} · ${project.status}`}
        actions={
          <>
            <Button asChild variant="outline">
              <Link href={`/projects/${project.id}/settings`}>Settings</Link>
            </Button>
            <Button asChild>
              <a href={`http://localhost:5173/?projectId=${project.id}`} target="_blank" rel="noreferrer">
                Open in Studio
              </a>
            </Button>
          </>
        }
      />

      <div className="grid gap-4 md:grid-cols-3">
        <Card title="Organization">
          <div className="text-sm text-zinc-300">{project.orgName}</div>
          <div className="mt-3">
            <Button asChild size="sm" variant="outline">
              <Link href={`/organizations/${project.orgId}`}>View organization</Link>
            </Button>
          </div>
        </Card>
        <Card title="Project slug">
          <div className="font-mono text-sm text-zinc-300">{project.slug}</div>
        </Card>
        <Card title="Last updated">
          <div className="text-sm text-zinc-300">{project.updatedAt}</div>
        </Card>
      </div>

      <Card title="Workspaces" description="Manage environments inside this project.">
        <div className="flex flex-wrap gap-2">
          <Button asChild variant="outline">
            <Link href={`/projects/${project.id}/workspaces`}>View workspaces</Link>
          </Button>
          <Button asChild>
            <Link href={`/projects/${project.id}/workspaces/create`}>New workspace</Link>
          </Button>
        </div>
      </Card>
    </div>
  );
}

