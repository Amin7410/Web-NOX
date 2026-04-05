import { apiClient } from './apiClient';
import { InvaderInstance } from '../types/studio';

export interface CreateBlockRequest {
    id?: string;
    parentBlockId?: string;
    originAssetId?: string;
    type: string;
    name: string;
    config?: Record<string, any>;
    visual?: Record<string, any>;
}

export interface AttachInvaderRequest {
    invaderAssetId: string;
    appliedVersion?: string;
    configSnapshot?: Record<string, any>;
}

export interface CreateRelationRequest {
    sourceBlockId: string;
    targetBlockId: string;
    type: string;
    rules?: Record<string, any>;
    visual?: Record<string, any>;
}

export interface UpdateRelationRequest {
    type?: string;
    rules?: Record<string, any>;
    visual?: Record<string, any>;
}

export const StudioApi = {
    // ----------------- BLOCKS -----------------
    getWorkspaceBlocks: async (workspaceId: string) => {
        const response = await apiClient.get(`/workspaces/${workspaceId}/blocks`);
        return response.data;
    },

    createBlock: async (workspaceId: string, data: CreateBlockRequest) => {
        const response = await apiClient.post(`/workspaces/${workspaceId}/blocks`, data);
        return response.data;
    },

    updateBlock: async (workspaceId: string, blockId: string, data: Partial<CreateBlockRequest>) => {
        const response = await apiClient.patch(`/workspaces/${workspaceId}/blocks/${blockId}`, data);
        return response.data;
    },

    deleteBlock: async (workspaceId: string, blockId: string) => {
        await apiClient.delete(`/workspaces/${workspaceId}/blocks/${blockId}`);
    },

    // ----------------- INVADERS -----------------
    getBlockInvaders: async (blockId: string) => {
        const response = await apiClient.get(`/blocks/${blockId}/invaders`);
        return response.data;
    },

    attachInvader: async (blockId: string, data: AttachInvaderRequest) => {
        const response = await apiClient.post(`/blocks/${blockId}/invaders`, data);
        return response.data; // Trả về BlockInvaderUsageResponse
    },

    detachInvader: async (blockId: string, usageId: string) => {
        // Tham số blockId trên param backend có catch nhưng không xài cũng được,
        // Backend đang là DELETE /api/blocks/{blockId}/invaders/{usageId}
        await apiClient.delete(`/blocks/${blockId}/invaders/${usageId}`);
    },

    // ----------------- RELATIONS (EDGES) -----------------
    getWorkspaceRelations: async (workspaceId: string) => {
        const response = await apiClient.get(`/workspaces/${workspaceId}/relations`);
        return response.data;
    },

    createRelation: async (workspaceId: string, data: CreateRelationRequest) => {
        const response = await apiClient.post(`/workspaces/${workspaceId}/relations`, data);
        return response.data;
    },

    updateRelation: async (workspaceId: string, relationId: string, data: UpdateRelationRequest) => {
        const response = await apiClient.patch(`/workspaces/${workspaceId}/relations/${relationId}`, data);
        return response.data;
    },

    deleteRelation: async (workspaceId: string, relationId: string) => {
        await apiClient.delete(`/workspaces/${workspaceId}/relations/${relationId}`);
    },

    // ----------------- SNAPSHOTS & SYNC -----------------
    syncState: async (workspaceId: string) => {
        const response = await apiClient.post(`/workspaces/${workspaceId}/sync`, {});
        return response.data;
    }
};
