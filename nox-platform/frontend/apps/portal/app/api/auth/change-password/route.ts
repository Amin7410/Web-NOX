import { NextResponse } from 'next/server';
import { proxyToBackend } from '@/lib/api';

export async function POST(request: Request) {
    try {
        const body = await request.json();
        const res = await proxyToBackend('/api/v1/auth/change-password', {
            method: 'POST',
            body: JSON.stringify(body),
        });

        const data = await res.json();
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
