import { NextResponse } from 'next/server';
import { proxyToBackend } from '@/lib/api';

export async function POST() {
    try {
        const res = await proxyToBackend('/api/v1/auth/mfa/setup', {
            method: 'POST',
        });

        const data = await res.json();
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
