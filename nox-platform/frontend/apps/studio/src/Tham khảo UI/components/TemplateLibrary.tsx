import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import * as Lucide from 'lucide-react';
import { blockTemplateService, warehouseApiService, BlockTemplate } from '../services/warehouseService';
import { blockService } from '../services/studioService';

const Icons = Lucide as any;

interface TemplateLibraryProps {
    workspaceId: string | null;
    onBlockAdded: (node: any) => void;
    onClose: () => void;
}

export default function TemplateLibrary({ workspaceId, onBlockAdded, onClose }: TemplateLibraryProps) {
    const [warehouseId, setWarehouseId] = useState('');
    const [warehouseName, setWarehouseName] = useState('');
    const [templates, setTemplates] = useState<BlockTemplate[]>([]);
    const [loadState, setLoadState] = useState<'idle' | 'loading' | 'done' | 'error'>('idle');
    const [errorMsg, setErrorMsg] = useState('');

    // Create template form
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [newName, setNewName] = useState('');
    const [newDesc, setNewDesc] = useState('');
    const [creating, setCreating] = useState(false);

    const loadTemplates = async (wId: string) => {
        const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
        if (!uuidRegex.test(wId.trim())) {
            setErrorMsg('Warehouse ID phải là định dạng UUID hợp lệ.');
            setLoadState('error');
            return;
        }

        setLoadState('loading');
        setErrorMsg('');
        try {
            const list = await blockTemplateService.list(wId.trim());
            setTemplates(list);
            setLoadState('done');
        } catch (e: any) {
            setErrorMsg(e.message || 'Không thể tải danh sách template');
            setLoadState('error');
        }
    };

    const createWarehouse = async () => {
        if (!warehouseName.trim()) return;
        setLoadState('loading');
        setErrorMsg('');
        try {
            const w = await warehouseApiService.create(warehouseName.trim());
            setWarehouseId(w.id);
            await loadTemplates(w.id);
        } catch (e: any) {
            // Hiển thị lỗi chi tiết từ backend (ví dụ: WAREHOUSE_EXISTS)
            setErrorMsg(e.message || 'Lỗi khi tạo Warehouse');
            setLoadState('error');
        }
    };

    const connectWarehouse = async () => {
        if (!warehouseId.trim()) return;
        await loadTemplates(warehouseId.trim());
    };

    const createTemplate = async () => {
        if (!warehouseId || !newName.trim()) return;
        setCreating(true);
        try {
            const t = await blockTemplateService.create(warehouseId, {
                name: newName.trim(),
                description: newDesc.trim(),
                structureData: { type: 'BASE', label: newName.trim() },
                version: '1.0.0',
            });
            setTemplates(prev => [...prev, t]);
            setNewName('');
            setNewDesc('');
            setShowCreateForm(false);
        } catch (e: any) {
            setErrorMsg(e.message);
        } finally {
            setCreating(false);
        }
    };

    const placeOnCanvas = async (template: BlockTemplate) => {
        if (!workspaceId) {
            alert('Không có workspaceId. Thêm ?workspaceId=UUID vào URL.');
            return;
        }
        try {
            const saved = await blockService.create(workspaceId, {
                name: template.name,
                type: 'TEMPLATE',
                config: { description: template.description, templateId: template.id },
                visual: { positionX: 300 + Math.random() * 200, positionY: 200 + Math.random() * 100 },
            });
            onBlockAdded({
                id: saved.id,
                type: 'baseBlock',
                position: { x: saved.visual?.positionX ?? 300, y: saved.visual?.positionY ?? 200 },
                data: {
                    label: saved.name,
                    description: saved.config?.description ?? '',
                    invaders: [],
                    visual: saved.visual ?? {},
                    backendId: saved.id,
                },
            });
        } catch (e: any) {
            alert(`Lỗi khi thêm block: ${e.message}`);
        }
    };

    const deleteTemplate = async (templateId: string) => {
        if (!warehouseId) return;
        await blockTemplateService.delete(warehouseId, templateId);
        setTemplates(prev => prev.filter(t => t.id !== templateId));
    };

    return (
        <div className="fixed inset-0 z-[300] bg-black/60 backdrop-blur-sm flex items-center justify-center" onClick={onClose}>
            <motion.div
                initial={{ scale: 0.95, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                exit={{ scale: 0.95, opacity: 0 }}
                className="w-[560px] max-h-[80vh] bg-zinc-950 border border-zinc-800 rounded-sm shadow-2xl flex flex-col overflow-hidden"
                onClick={e => e.stopPropagation()}
            >
                {/* Header */}
                <div className="p-5 border-b border-zinc-800 flex items-center justify-between shrink-0">
                    <div className="flex items-center gap-3">
                        <div className="w-7 h-7 bg-indigo-500/10 border border-indigo-500/20 rounded-sm flex items-center justify-center">
                            <Icons.Archive size={14} className="text-indigo-400" />
                        </div>
                        <div>
                            <h2 className="text-[11px] font-black uppercase tracking-widest text-zinc-200">Template Library</h2>
                            <p className="text-[9px] text-zinc-600 mt-0.5">Warehouse — Block Templates</p>
                        </div>
                    </div>
                    <button onClick={onClose} className="p-1.5 hover:bg-zinc-800 rounded-sm text-zinc-600 hover:text-white">
                        <Icons.X size={16} />
                    </button>
                </div>

                {/* Warehouse Connect */}
                <div className="p-4 border-b border-zinc-800/50 space-y-3 shrink-0 bg-zinc-900/20">
                    <p className="text-[9px] font-black text-zinc-600 uppercase tracking-widest">Warehouse Connection</p>
                    <div className="flex gap-2">
                        <input
                            type="text"
                            placeholder="Nhập Warehouse ID (UUID)..."
                            value={warehouseId}
                            onChange={e => setWarehouseId(e.target.value)}
                            className="flex-1 bg-zinc-900 border border-zinc-800 px-3 py-2 text-[11px] text-zinc-300 focus:outline-none focus:border-indigo-500/50 font-mono"
                        />
                        <button
                            onClick={connectWarehouse}
                            disabled={loadState === 'loading'}
                            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white text-[10px] font-black uppercase tracking-widest disabled:opacity-50"
                        >
                            Load
                        </button>
                    </div>
                    <div className="flex gap-2 items-center">
                        <div className="flex-1 h-px bg-zinc-800" />
                        <span className="text-[9px] text-zinc-700 font-bold">Hoặc tạo mới</span>
                        <div className="flex-1 h-px bg-zinc-800" />
                    </div>
                    <div className="flex gap-2">
                        <input
                            type="text"
                            placeholder="Tên Warehouse mới..."
                            value={warehouseName}
                            onChange={e => setWarehouseName(e.target.value)}
                            className="flex-1 bg-zinc-900 border border-zinc-800 px-3 py-2 text-[11px] text-zinc-300 focus:outline-none focus:border-indigo-500/50"
                        />
                        <button
                            onClick={createWarehouse}
                            disabled={loadState === 'loading'}
                            className="px-4 py-2 bg-zinc-800 hover:bg-zinc-700 border border-zinc-700 text-zinc-300 text-[10px] font-black uppercase tracking-widest disabled:opacity-50"
                        >
                            Create
                        </button>
                    </div>
                </div>

                {/* Error */}
                {loadState === 'error' && (
                    <div className="px-4 py-2 bg-red-900/20 border-b border-red-800/30 flex items-center gap-2">
                        <Icons.AlertTriangle size={12} className="text-red-400 shrink-0" />
                        <p className="text-[10px] text-red-400">{errorMsg}</p>
                    </div>
                )}

                {/* Template List */}
                {loadState === 'done' && (
                    <div className="flex-1 overflow-y-auto p-4 space-y-2 custom-scrollbar">
                        <div className="flex items-center justify-between mb-4">
                            <p className="text-[9px] font-black text-zinc-600 uppercase tracking-widest">{templates.length} Templates</p>
                            <button
                                onClick={() => setShowCreateForm(true)}
                                className="flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600/20 border border-indigo-500/30 text-indigo-400 text-[9px] font-black uppercase tracking-widest rounded-sm hover:bg-indigo-600/30 transition-all"
                            >
                                <Icons.Plus size={11} /> Tạo Template
                            </button>
                        </div>

                        {/* Create Form */}
                        <AnimatePresence>
                            {showCreateForm && (
                                <motion.div
                                    initial={{ height: 0, opacity: 0 }}
                                    animate={{ height: 'auto', opacity: 1 }}
                                    exit={{ height: 0, opacity: 0 }}
                                    className="overflow-hidden border border-indigo-500/20 rounded-sm bg-indigo-500/5 mb-3"
                                >
                                    <div className="p-4 space-y-3">
                                        <p className="text-[9px] font-black text-indigo-400 uppercase tracking-widest">New Block Template</p>
                                        <input
                                            autoFocus
                                            type="text"
                                            placeholder="Tên block (vd: HTTP Trigger)..."
                                            value={newName}
                                            onChange={e => setNewName(e.target.value)}
                                            className="w-full bg-zinc-900 border border-zinc-800 px-3 py-2 text-[11px] text-zinc-300 focus:outline-none focus:border-indigo-500/50"
                                            onKeyDown={e => e.key === 'Enter' && createTemplate()}
                                        />
                                        <input
                                            type="text"
                                            placeholder="Mô tả ngắn (tuỳ chọn)..."
                                            value={newDesc}
                                            onChange={e => setNewDesc(e.target.value)}
                                            className="w-full bg-zinc-900 border border-zinc-800 px-3 py-2 text-[11px] text-zinc-300 focus:outline-none focus:border-indigo-500/50"
                                        />
                                        <div className="flex gap-2 justify-end">
                                            <button onClick={() => setShowCreateForm(false)} className="px-4 py-1.5 text-[9px] font-black uppercase text-zinc-600 hover:text-zinc-300">Cancel</button>
                                            <button onClick={createTemplate} disabled={creating} className="px-4 py-1.5 bg-indigo-600 text-white text-[9px] font-black uppercase tracking-widest disabled:opacity-50">
                                                {creating ? 'Saving...' : 'Save'}
                                            </button>
                                        </div>
                                    </div>
                                </motion.div>
                            )}
                        </AnimatePresence>

                        {templates.length === 0 && (
                            <div className="text-center py-10 text-zinc-700">
                                <Icons.Package size={32} className="mx-auto mb-3 opacity-20" />
                                <p className="text-[10px] font-bold uppercase tracking-widest">Chưa có template nào</p>
                                <p className="text-[9px] mt-1 opacity-50">Nhấn "Tạo Template" để bắt đầu</p>
                            </div>
                        )}

                        {templates.map(t => (
                            <div key={t.id} className="flex items-center gap-3 p-3 border border-zinc-800/50 rounded-sm group hover:border-zinc-700 bg-zinc-900/20 transition-all">
                                <div className="w-8 h-8 bg-zinc-900 border border-zinc-800 rounded-sm flex items-center justify-center shrink-0">
                                    <Icons.Box size={14} className="text-indigo-400" />
                                </div>
                                <div className="flex-1 min-w-0">
                                    <p className="text-[11px] font-black text-zinc-300 truncate">{t.name}</p>
                                    <p className="text-[9px] text-zinc-600 truncate">{t.description || 'Không có mô tả'}</p>
                                    <p className="text-[8px] text-zinc-700 font-mono mt-0.5">{t.id.slice(0, 8)}... · v{t.version}</p>
                                </div>
                                <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                    <button
                                        onClick={() => placeOnCanvas(t)}
                                        title="Thêm vào Canvas"
                                        className="p-2 hover:bg-indigo-600/20 text-indigo-400 rounded-sm transition-all"
                                    >
                                        <Icons.PlusCircle size={14} />
                                    </button>
                                    <button
                                        onClick={() => deleteTemplate(t.id)}
                                        title="Xoá template"
                                        className="p-2 hover:bg-red-600/10 text-zinc-600 hover:text-red-400 rounded-sm transition-all"
                                    >
                                        <Icons.Trash2 size={14} />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}

                {loadState === 'loading' && (
                    <div className="flex-1 flex items-center justify-center">
                        <Icons.Loader2 size={20} className="text-indigo-400 animate-spin" />
                    </div>
                )}

                {loadState === 'idle' && (
                    <div className="flex-1 flex flex-col items-center justify-center text-zinc-700 gap-2">
                        <Icons.Database size={28} className="opacity-20" />
                        <p className="text-[10px] font-bold uppercase tracking-widest">Nhập Warehouse ID để bắt đầu</p>
                    </div>
                )}
            </motion.div>
        </div>
    );
}
