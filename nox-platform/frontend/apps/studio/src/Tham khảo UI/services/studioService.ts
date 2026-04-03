// ─────────────────────────────────────────────
//  NOX Studio — Studio Service
//  Maps backend DTOs ↔ ReactFlow Node/Edge data
// ─────────────────────────────────────────────
import { Node, Edge } from 'reactflow';
import { api } from './api';

// ── Types (mirrors backend DTOs) ──────────────

export interface CoreBlockResponse {
    id: string;
    workspaceId: string;
    parentBlockId: string | null;
    originAssetId: string | null;
    type: string;
    name: string;
    config: Record<string, any>;
    visual: Record<string, any>;
    createdById: string;
    updatedAt: string;
    deletedAt: string | null;
}

export interface CoreRelationResponse {
    id: string;
    workspaceId: string;
    sourceBlockId: string;
    targetBlockId: string;
    type: string;
    rules: Record<string, any>;
    visual: Record<string, any>;
    deletedAt: string | null;
}

export interface WorkspaceResponse {
    id: string;
    projectId: string;
    name: string;
    slug: string;
    type: string;
    status: string;
}

// ── Conversion Utilities ──────────────────────

/**
 * CoreBlockResponse (backend) → ReactFlow Node
 */
export function blockToNode(block: CoreBlockResponse): Node {
    return {
        id: block.id,
        type: 'baseBlock',
        position: {
            x: (block.visual?.positionX as number) ?? Math.random() * 600 + 100,
            y: (block.visual?.positionY as number) ?? Math.random() * 300 + 100,
        },
        data: {
            label: block.name,
            description: (block.config?.description as string) ?? '',
            invaders: (block.config?.invaders as any[]) ?? [],
            visual: block.visual ?? {},
            backendId: block.id, // Keep track of real DB id
            parentBlockId: block.parentBlockId,
        },
    };
}

/**
 * CoreRelationResponse (backend) → ReactFlow Edge
 */
export function relationToEdge(relation: CoreRelationResponse): Edge {
    return {
        id: relation.id,
        source: relation.sourceBlockId,
        target: relation.targetBlockId,
        type: 'noxRelation',
        sourceHandle: 'source',
        targetHandle: 'target',
        data: {
            label: (relation.rules?.label as string) ?? 'Route',
            shape: (relation.visual?.shape as string) ?? 'step',
            color: (relation.visual?.color as string) ?? '#ffffff',
            animating: (relation.visual?.animating as boolean) ?? false,
            backendId: relation.id,
        },
    };
}

// ── Workspace ─────────────────────────────────

export const workspaceService = {
    getAll: () => api.get<WorkspaceResponse[]>('/v1/workspaces'),
    getById: (id: string) => api.get<WorkspaceResponse>(`/v1/workspaces/${id}`),
};

// ── Blocks ────────────────────────────────────

export const blockService = {
    list: (workspaceId: string) =>
        api.get<CoreBlockResponse[]>(`/workspaces/${workspaceId}/blocks`),

    create: (workspaceId: string, data: {
        name: string;
        type: string;
        parentBlockId?: string;
        config?: Record<string, any>;
        visual?: Record<string, any>;
    }) => api.post<CoreBlockResponse>(`/workspaces/${workspaceId}/blocks`, {
        name: data.name,
        type: data.type,
        parentBlockId: data.parentBlockId ?? null,
        config: data.config ?? {},
        visual: data.visual ?? {},
    }),

    update: (workspaceId: string, blockId: string, data: {
        name?: string;
        config?: Record<string, any>;
        visual?: Record<string, any>;
    }) => api.patch<CoreBlockResponse>(`/workspaces/${workspaceId}/blocks/${blockId}`, data),

    delete: (workspaceId: string, blockId: string) =>
        api.delete<void>(`/workspaces/${workspaceId}/blocks/${blockId}`),
};

// ── Relations ─────────────────────────────────

export const relationService = {
    list: (workspaceId: string) =>
        api.get<CoreRelationResponse[]>(`/workspaces/${workspaceId}/relations`),

    create: (workspaceId: string, data: {
        sourceBlockId: string;
        targetBlockId: string;
        type?: string;
        rules?: Record<string, any>;
        visual?: Record<string, any>;
    }) => api.post<CoreRelationResponse>(`/workspaces/${workspaceId}/relations`, {
        sourceBlockId: data.sourceBlockId,
        targetBlockId: data.targetBlockId,
        type: data.type ?? 'DEFAULT',
        rules: data.rules ?? {},
        visual: data.visual ?? {},
    }),

    update: (workspaceId: string, relationId: string, data: {
        rules?: Record<string, any>;
        visual?: Record<string, any>;
    }) => api.patch<CoreRelationResponse>(`/workspaces/${workspaceId}/relations/${relationId}`, data),

    delete: (workspaceId: string, relationId: string) =>
        api.delete<void>(`/workspaces/${workspaceId}/relations/${relationId}`),
};
