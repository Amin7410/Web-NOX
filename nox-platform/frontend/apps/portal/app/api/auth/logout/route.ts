import { NextResponse } from 'next/server';
import { proxyToBackend } from '@/lib/api';
import { cookies } from 'next/headers';

export async function POST() {
    try {
        const cookieStore = cookies();
        const refreshTokenCookie = cookieStore.get('refreshToken');
        
        if (refreshTokenCookie && refreshTokenCookie.value) {
            await proxyToBackend('/api/v1/auth/logout', {
                method: 'POST',
                body: JSON.stringify({ refreshToken: refreshTokenCookie.value }),
            });
        }

        // Clear tokens from cookies
        cookieStore.delete('accessToken');
        cookieStore.delete('refreshToken');

        return NextResponse.json({ message: 'Logged out successfully' }, { status: 200 });
    } catch (error) {
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
