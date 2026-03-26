import { NextResponse } from 'next/server';
import { proxyToBackend } from '@/lib/api';

export async function GET(_request: Request, { params }: { params: { id: string } }) {
    try {
        const res = await proxyToBackend(`/api/v1/orgs/${params.id}`);

        const data = await res.json().catch(() => ({}));
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}

export async function PUT(request: Request, { params }: { params: { id: string } }) {
    try {
        const body = await request.json();
        const res = await proxyToBackend(`/api/v1/orgs/${params.id}`, {
            method: 'PUT',
            body: JSON.stringify(body),
        });

        const data = await res.json().catch(() => ({}));
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}

export async function DELETE(_request: Request, { params }: { params: { id: string } }) {
    try {
        const res = await proxyToBackend(`/api/v1/orgs/${params.id}`, {
            method: 'DELETE',
        });

        if (res.status === 204) {
            return NextResponse.json({ message: 'Success' }, { status: 200 });
        }
        
        const data = await res.json().catch(() => ({}));
        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
