import { NextResponse } from 'next/server';
import { proxyToBackend } from '@/lib/api';

export async function GET(request: Request) {
    try {
        const { searchParams } = new URL(request.url);
        const res = await proxyToBackend(`/api/v1/projects?${searchParams.toString()}`);

        const data = await res.json().catch(() => ({}));
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}

export async function POST(request: Request) {
    try {
        const body = await request.json();
        const res = await proxyToBackend('/api/v1/projects', {
            method: 'POST',
            body: JSON.stringify(body),
        });

        const data = await res.json().catch(() => ({}));
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
