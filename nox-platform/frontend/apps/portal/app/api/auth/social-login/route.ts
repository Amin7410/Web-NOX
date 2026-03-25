import { NextResponse } from 'next/server';
import { proxyToBackend } from '@/lib/api';
import { cookies } from 'next/headers';

export async function POST(request: Request) {
    try {
        const body = await request.json();
        const res = await proxyToBackend('/api/v1/auth/social-login', {
            method: 'POST',
            body: JSON.stringify(body),
        });

        const data = await res.json();
        
        if (res.ok && data.data && data.data.token) {
            const cookieStore = cookies();
            cookieStore.set('accessToken', data.data.token, {
                httpOnly: true,
                secure: process.env.NODE_ENV === 'production',
                sameSite: 'lax',
                path: '/',
                maxAge: 5 * 60,
            });
            
            if (data.data.refreshToken) {
                cookieStore.set('refreshToken', data.data.refreshToken, {
                    httpOnly: true,
                    secure: process.env.NODE_ENV === 'production',
                    sameSite: 'lax',
                    path: '/',
                    maxAge: 7 * 24 * 60 * 60,
                });
            }
            delete data.data.token;
            delete data.data.refreshToken;
        }

        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
