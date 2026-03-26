import { NextResponse } from 'next/server';
import { proxyToBackend } from '@/lib/api';

export async function POST(request: Request, { params }: { params: { id: string } }) {
    try {
        const body = await request.json();
        // Forwarding to the org-members invite route based on the research report
        const res = await proxyToBackend('/api/v1/org-members/invite', {
            method: 'POST',
            body: JSON.stringify({
                orgId: params.id,
                ...body,
            }),
        });

        const data = await res.json().catch(() => ({}));
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
