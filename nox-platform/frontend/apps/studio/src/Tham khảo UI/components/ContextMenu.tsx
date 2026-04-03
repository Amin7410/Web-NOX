import { motion, AnimatePresence } from 'framer-motion';
import { Settings, Trash2, Copy, Palette, Share2 } from 'lucide-react';

interface ContextMenuProps {
    visible: boolean;
    x: number;
    y: number;
    onClose: () => void;
    onAction: (action: string) => void;
    type: 'node' | 'edge' | 'canvas';
}

export default function ContextMenu({ visible, x, y, onClose, onAction, type }: ContextMenuProps) {
    if (!visible) return null;

    return (
        <AnimatePresence>
            <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.9 }}
                className="fixed z-[1000] w-48 bg-zinc-900/90 backdrop-blur-xl border border-zinc-800 shadow-2xl rounded-sm p-1.5 overflow-hidden"
                style={{ top: y, left: x }}
                onClick={(e) => e.stopPropagation()}
            >
                <div className="px-2 py-1.5 border-b border-zinc-800/50 mb-1">
                    <span className="text-[10px] font-black uppercase tracking-widest text-zinc-600">
                        {type} Menu
                    </span>
                </div>

                <div className="flex flex-col gap-0.5">
                    {type !== 'canvas' && (
                        <>
                            <button 
                                onClick={() => onAction('settings')}
                                className="flex items-center gap-3 px-2 py-1.5 rounded-sm hover:bg-zinc-800 text-zinc-400 hover:text-slate-200 transition-all text-xs group"
                            >
                                <Settings size={14} className="group-hover:rotate-45 transition-transform" />
                                <span>Cấu hình chi tiết</span>
                            </button>
                            <button 
                                onClick={() => onAction('copy')}
                                className="flex items-center gap-3 px-2 py-1.5 rounded-sm hover:bg-zinc-800 text-zinc-400 hover:text-slate-200 transition-all text-xs"
                            >
                                <Copy size={14} />
                                <span>Nhân bản</span>
                            </button>
                            <button 
                                onClick={() => onAction('style')}
                                className="flex items-center gap-3 px-2 py-1.5 rounded-sm hover:bg-zinc-800 text-zinc-400 hover:text-slate-200 transition-all text-xs"
                            >
                                <Palette size={14} />
                                <span>Đổi màu sắc</span>
                            </button>
                        </>
                    )}

                    {type === 'canvas' && (
                        <button 
                            onClick={() => onAction('router')}
                            className="flex items-center gap-3 px-2 py-1.5 rounded-sm hover:bg-zinc-800 text-zinc-400 hover:text-slate-200 transition-all text-xs"
                        >
                            <Share2 size={14} />
                            <span>Tạo Nút giao (Router)</span>
                        </button>
                    )}

                    <div className="h-px bg-zinc-800/50 my-1" />
                    
                    <button 
                        onClick={() => onAction('delete')}
                        className="flex items-center gap-3 px-2 py-1.5 rounded-sm hover:bg-red-500/10 text-zinc-500 hover:text-red-400 transition-all text-xs"
                    >
                        <Trash2 size={14} />
                        <span>Xóa vật thể</span>
                    </button>
                </div>
            </motion.div>

            {/* Backdrop to close menu */}
            <div 
                className="fixed inset-0 z-[999]" 
                onMouseDown={onClose}
                onContextMenu={(e) => { e.preventDefault(); onClose(); }}
            />
        </AnimatePresence>
    );
}
