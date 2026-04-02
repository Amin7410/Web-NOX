import { NextResponse } from 'next/server';
import { proxyToBackend } from '@/lib/api';

export async function GET() {
    try {
        const res = await proxyToBackend('/api/v1/orgs', {
            method: 'GET',
        });

        const data = await res.json().catch(() => ({}));
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}

export async function POST(request: Request) {
    try {
        const body = await request.json();
        const res = await proxyToBackend('/api/v1/orgs', {
            method: 'POST',
            body: JSON.stringify(body),
        });

        const data = await res.json().catch(() => ({}));
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
