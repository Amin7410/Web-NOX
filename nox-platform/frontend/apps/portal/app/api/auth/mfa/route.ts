import { NextResponse } from 'next/server';
import { proxyToBackend } from '@/lib/api';

export async function DELETE(request: Request) {
    try {
        const body = await request.json();
        const res = await proxyToBackend('/api/v1/auth/mfa', {
            method: 'DELETE',
            body: JSON.stringify(body),
        });

        // Some endpoints return no content (204)
        if (res.status === 204) {
            return NextResponse.json({ message: 'Success' }, { status: 200 });
        }
        
        const data = await res.json().catch(() => ({}));
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
