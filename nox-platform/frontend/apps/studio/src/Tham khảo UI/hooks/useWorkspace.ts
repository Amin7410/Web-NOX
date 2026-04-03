// ─────────────────────────────────────────────
//  NOX Studio — useWorkspace Hook
//  Loads blocks & relations from backend
//  and converts them to ReactFlow format
// ─────────────────────────────────────────────
import { useState, useEffect, useCallback } from 'react';
import { Node, Edge } from 'reactflow';
import { blockService, relationService, blockToNode, relationToEdge } from '../services/studioService';

export type LoadStatus = 'idle' | 'loading' | 'success' | 'error';

interface UseWorkspaceReturn {
    nodes: Node[];
    edges: Edge[];
    status: LoadStatus;
    error: string | null;
    reload: () => void;
}

export function useWorkspace(workspaceId: string | null): UseWorkspaceReturn {
    const [nodes, setNodes] = useState<Node[]>([]);
    const [edges, setEdges] = useState<Edge[]>([]);
    const [status, setStatus] = useState<LoadStatus>('idle');
    const [error, setError] = useState<string | null>(null);

    const load = useCallback(async () => {
        if (!workspaceId) return;
        setStatus('loading');
        setError(null);
        try {
            const [blocks, relations] = await Promise.all([
                blockService.list(workspaceId),
                relationService.list(workspaceId),
            ]);
            setNodes(blocks.filter(b => !b.deletedAt).map(blockToNode));
            setEdges(relations.filter(r => !r.deletedAt).map(relationToEdge));
            setStatus('success');
        } catch (err: any) {
            setStatus('error');
            setError(err.message ?? 'Failed to load workspace');
            console.error('[useWorkspace] load error:', err);
        }
    }, [workspaceId]);

    useEffect(() => {
        load();
    }, [load]);

    return { nodes, edges, status, error, reload: load };
}
