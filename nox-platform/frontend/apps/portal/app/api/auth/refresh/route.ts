import { NextResponse } from 'next/server';
import { proxyToBackend } from '@/lib/api';
import { cookies } from 'next/headers';

export async function POST() {
    try {
        const cookieStore = cookies();
        const refreshTokenCookie = cookieStore.get('refreshToken');
        
        if (!refreshTokenCookie || !refreshTokenCookie.value) {
            return NextResponse.json({ error: 'No refresh token' }, { status: 401 });
        }

        const res = await proxyToBackend('/api/v1/auth/refresh', {
            method: 'POST',
            body: JSON.stringify({ refreshToken: refreshTokenCookie.value }),
        });

        const data = await res.json();
        
        if (res.ok && data.data) {
            if (data.data.token) {
                cookieStore.set('accessToken', data.data.token, {
                    httpOnly: true,
                    secure: process.env.NODE_ENV === 'production',
                    sameSite: 'lax',
                    path: '/',
                    maxAge: 5 * 60,
                });
            }
            if (data.data.refreshToken) {
                cookieStore.set('refreshToken', data.data.refreshToken, {
                    httpOnly: true,
                    secure: process.env.NODE_ENV === 'production',
                    sameSite: 'lax',
                    path: '/',
                    maxAge: 7 * 24 * 60 * 60,
                });
            }
            // Remove tokens from the response 
            delete data.data.token;
            delete data.data.refreshToken;
        }

        return NextResponse.json(data, { status: res.status });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
