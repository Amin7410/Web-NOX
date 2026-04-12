import { cookies } from 'next/headers';
import { NextResponse } from 'next/server';

export async function GET() {
    const cookieStore = cookies();
    const token = cookieStore.get('accessToken')?.value || '';
    const orgId = cookieStore.get('nox_org_id')?.value || '';
    
    return NextResponse.json({ token, orgId });
}
