import { NextResponse } from 'next/server';
import { cookies } from 'next/headers';

export async function POST(request: Request) {
    try {
        const body = await request.json();
        const { orgId } = body;

        if (!orgId) {
            return NextResponse.json({ error: 'Org ID is required' }, { status: 400 });
        }

        const cookieStore = cookies();
        
        cookieStore.set('nox_org_id', orgId, {
            path: '/',
            maxAge: 60 * 60 * 24 * 30, // 30 days
            httpOnly: false, 
            secure: process.env.NODE_ENV === 'production',
            sameSite: 'lax',
        });

        console.log(`Portal Auth: Selected Organization: ${orgId}`);
        return NextResponse.json({ success: true, orgId });
    } catch (error) {
        console.error('Portal Select Org Error:', error);
        return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
    }
}
