import { cookies } from 'next/headers';

const BACKEND_URL = process.env.BACKEND_URL || 'http://localhost:8081';

export async function proxyToBackend(endpoint: string, options: RequestInit = {}) {
    const cookieStore = cookies();
    const token = cookieStore.get('accessToken')?.value;

    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
        ...(options.headers as Record<string, string> || {}),
    };

    if (token && !headers['Authorization']) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const res = await fetch(`${BACKEND_URL}${endpoint}`, {
        ...options,
        headers,
    });

    return res;
}
