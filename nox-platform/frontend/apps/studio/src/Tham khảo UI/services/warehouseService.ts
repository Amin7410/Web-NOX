// ─────────────────────────────────────────────
//  NOX Studio — Warehouse Service (Block Templates)
//  API calls for BlockTemplate management
// ─────────────────────────────────────────────
import { api } from './api';

export interface BlockTemplate {
    id: string;
    name: string;
    description?: string;
    thumbnailUrl?: string;
    structureData: Record<string, any>;
    version?: string;
}

export interface WarehouseInfo {
    id: string;
    name: string;
    ownerType: string;
    isSystem: boolean;
}

export const warehouseApiService = {
    // Create a new Warehouse (needed once)
    create: (name: string, ownerType = 'USER', isSystem = false) =>
        api.post<WarehouseInfo>('/v1/warehouses', { name, ownerType, isSystem }),

    // Get my warehouses
    getByOwner: (ownerId: string) =>
        api.get<WarehouseInfo[]>(`/v1/warehouses/owner/${ownerId}`),
};

export const blockTemplateService = {
    // List all block templates in a warehouse
    list: (warehouseId: string) =>
        api.get<BlockTemplate[]>(`/v1/warehouses/${warehouseId}/templates/blocks`),

    // Create a new block template
    create: (warehouseId: string, data: {
        name: string;
        description?: string;
        structureData: Record<string, any>;
        version?: string;
    }) => api.post<BlockTemplate>(`/v1/warehouses/${warehouseId}/templates/blocks`, {
        name: data.name,
        description: data.description ?? '',
        structureData: data.structureData,
        version: data.version ?? '1.0.0',
    }),

    // Delete a template
    delete: (warehouseId: string, templateId: string) =>
        api.delete<void>(`/v1/warehouses/${warehouseId}/templates/blocks/${templateId}`),
};
