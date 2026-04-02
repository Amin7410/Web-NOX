import { NextResponse } from 'next/server';
import { proxyToBackend } from '@/lib/api';

export async function GET() {
    try {
        const res = await proxyToBackend('/api/v1/auth/me', {
            method: 'GET',
        });

        const data = await res.json().catch(() => ({}));
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
