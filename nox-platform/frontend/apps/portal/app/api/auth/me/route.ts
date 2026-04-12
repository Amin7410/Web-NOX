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
export async function PUT(request: Request) {
    try {
        const body = await request.json();
        
        const res = await proxyToBackend('/api/v1/auth/me', {
            method: 'PUT',
            body: JSON.stringify(body),
        });
        
        const data = await res.json().catch(() => ({}));
        return NextResponse.json(data, { status: res.status });
    } catch (err) {
        return NextResponse.json(
            { message: "Failed to update profile", error: String(err) },
            { status: 400 }
        );
    }
}
