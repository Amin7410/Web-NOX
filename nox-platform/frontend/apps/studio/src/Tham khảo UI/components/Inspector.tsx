import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import * as Lucide from 'lucide-react';

const Icons = Lucide as any;
const { X, Trash2, Settings2, Zap, LayoutPanelTop, Database, Activity, Globe, Code, Save, ChevronDown, ChevronUp } = Icons;

interface InspectorProps {
    type: 'node' | 'edge';
    selectedObject: any;
    onClose: () => void;
    onUpdate: (id: string, data: any) => void;
}

export default function Inspector({ type, selectedObject, onClose, onUpdate }: InspectorProps) {
    const data = selectedObject.data;
    const [expandedInvader, setExpandedInvader] = useState<number | null>(null);

    const InvaderIcon = ({ type }: { type: string }) => {
        switch (type) {
            case 'TRIGGER': return <Zap size={10} className="text-red-400" />;
            case 'LOGIC': return <Activity size={10} className="text-blue-400" />;
            case 'OUTPUT': return <Database size={10} className="text-emerald-400" />;
            default: return <Icons.Box size={10} />;
        }
    };

    const updateInvaderSettings = (idx: number, settings: any) => {
        const newInvaders = [...(data.invaders || [])];
        const current = typeof newInvaders[idx] === 'string' ? { type: newInvaders[idx], settings: {} } : newInvaders[idx];
        newInvaders[idx] = { ...current, settings: { ...current.settings, ...settings } };
        onUpdate(selectedObject.id, { ...data, invaders: newInvaders });
    };

    return (
        <div className="w-80 h-full border-l border-zinc-800 bg-zinc-950 flex flex-col shadow-2xl overflow-hidden">
            {/* Header */}
            <div className="p-4 border-b border-zinc-800/50 flex items-center justify-between bg-zinc-900/20">
                <div className="flex items-center gap-3">
                    <div className="w-6 h-6 rounded-sm bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center">
                        <Settings2 size={12} className="text-indigo-400" />
                    </div>
                    <h2 className="text-[10px] font-black uppercase tracking-[0.2em] text-zinc-400">Inspector</h2>
                </div>
                <button onClick={onClose} className="p-1.5 hover:bg-zinc-800 rounded-sm text-zinc-600 hover:text-white transition-all">
                    <X size={16} />
                </button>
            </div>

            <div className="flex-1 overflow-y-auto p-5 space-y-8 custom-scrollbar">
                {/* General Info */}
                <section>
                    <div className="flex items-center justify-between mb-4">
                        <h3 className="text-[9px] font-black uppercase tracking-widest text-zinc-600">Identification</h3>
                        <span className="text-[8px] bg-zinc-900 px-1.5 py-0.5 rounded border border-zinc-800 text-zinc-500 font-mono italic">
                            {type.toUpperCase()}#{selectedObject.id.slice(-4)}
                        </span>
                    </div>
                    <div className="space-y-4">
                        <div>
                            <label className="text-[9px] font-bold text-zinc-700 uppercase mb-2 block tracking-tighter">Display Label</label>
                            <input
                                type="text" 
                                value={data?.label || ''}
                                onChange={(e) => onUpdate(selectedObject.id, { ...data, label: e.target.value })}
                                className="w-full bg-zinc-900/50 border border-zinc-800/80 p-2.5 text-[11px] text-zinc-200 focus:outline-none focus:border-indigo-500/50 transition-colors rounded-sm"
                            />
                        </div>
                    </div>
                </section>

                {/* Invader Configuration */}
                {type === 'node' && selectedObject.type === 'baseBlock' && (
                    <section>
                        <div className="flex items-center justify-between mb-4">
                            <h3 className="text-[9px] font-black uppercase tracking-widest text-zinc-600">Module Stack</h3>
                            <span className="text-[8px] text-indigo-400 font-bold">{(data?.invaders || []).length} ACTIVE</span>
                        </div>
                        
                        <div className="space-y-2">
                            {(!data?.invaders || data.invaders.length === 0) ? (
                                <div className="p-8 border border-dashed border-zinc-800/50 rounded-sm text-center bg-zinc-900/10">
                                    <Icons.Layers size={20} className="mx-auto text-zinc-800 mb-2 opacity-20" />
                                    <p className="text-[9px] text-zinc-700 font-bold uppercase tracking-widest leading-relaxed">
                                        Empty Socket<br/>
                                        <span className="lowercase font-normal opacity-40">Drop a module to begin</span>
                                    </p>
                                </div>
                            ) : (
                                <div className="space-y-2">
                                    {data.invaders.map((inv: any, idx: number) => {
                                        const invType = typeof inv === 'string' ? inv : inv.type;
                                        const invSettings = typeof inv === 'string' ? {} : inv.settings || {};
                                        const isExpanded = expandedInvader === idx;

                                        return (
                                            <div key={idx} className={`border rounded-sm transition-all ${isExpanded ? 'border-zinc-700 bg-zinc-900/30' : 'border-zinc-800/50 hover:border-zinc-700'}`}>
                                                {/* Module Header */}
                                                <div 
                                                    className="flex items-center justify-between p-2.5 cursor-pointer group"
                                                    onClick={() => setExpandedInvader(isExpanded ? null : idx)}
                                                >
                                                    <div className="flex items-center gap-3">
                                                        <div className="p-1.5 bg-zinc-900 rounded-sm border border-zinc-800 shadow-inner group-hover:border-zinc-700">
                                                            <InvaderIcon type={invType} />
                                                        </div>
                                                        <span className="text-[10px] font-black text-zinc-400 group-hover:text-zinc-200 tracking-wide">{invType}</span>
                                                    </div>
                                                    <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                                        <button 
                                                            onClick={(e) => {
                                                                e.stopPropagation();
                                                                const newInvArr = [...data.invaders];
                                                                newInvArr.splice(idx, 1);
                                                                onUpdate(selectedObject.id, { ...data, invaders: newInvArr });
                                                            }}
                                                            className="p-1 text-zinc-600 hover:text-red-500"
                                                        >
                                                            <Trash2 size={12} />
                                                        </button>
                                                        {isExpanded ? <ChevronUp size={12} className="text-zinc-600" /> : <ChevronDown size={12} className="text-zinc-600" />}
                                                    </div>
                                                </div>

                                                {/* Module Settings Form */}
                                                <AnimatePresence>
                                                    {isExpanded && (
                                                        <motion.div
                                                            initial={{ height: 0, opacity: 0 }}
                                                            animate={{ height: 'auto', opacity: 1 }}
                                                            exit={{ height: 0, opacity: 0 }}
                                                            className="overflow-hidden border-t border-zinc-800/50"
                                                        >
                                                            <div className="p-3 space-y-4 bg-zinc-950/40">
                                                                {invType === 'TRIGGER' && (
                                                                    <>
                                                                        <div className="space-y-1.5">
                                                                            <label className="text-[8px] font-black text-zinc-600 uppercase flex items-center gap-1.5 italic">
                                                                                <Globe size={8} /> Endpoint URL
                                                                            </label>
                                                                            <input 
                                                                                type="text" 
                                                                                placeholder="https://api.nox.com/webhook"
                                                                                value={invSettings.url || ''}
                                                                                onChange={(e) => updateInvaderSettings(idx, { url: e.target.value })}
                                                                                className="w-full bg-zinc-900 border border-zinc-800 p-2 text-[10px] text-zinc-400 focus:outline-none focus:border-indigo-500/50 font-mono"
                                                                            />
                                                                        </div>
                                                                        <div className="grid grid-cols-2 gap-2">
                                                                            <div className="space-y-1.5">
                                                                                <label className="text-[8px] font-black text-zinc-600 uppercase italic">Method</label>
                                                                                <select 
                                                                                    value={invSettings.method || 'POST'}
                                                                                    onChange={(e) => updateInvaderSettings(idx, { method: e.target.value })}
                                                                                    className="w-full bg-zinc-900 border border-zinc-800 p-2 text-[10px] text-zinc-400 focus:outline-none"
                                                                                >
                                                                                    <option>GET</option>
                                                                                    <option>POST</option>
                                                                                    <option>PUT</option>
                                                                                </select>
                                                                            </div>
                                                                            <div className="space-y-1.5">
                                                                                <label className="text-[8px] font-black text-zinc-600 uppercase italic">Timeout</label>
                                                                                <input 
                                                                                    type="number" 
                                                                                    placeholder="5000"
                                                                                    value={invSettings.timeout || ''}
                                                                                    onChange={(e) => updateInvaderSettings(idx, { timeout: e.target.value })}
                                                                                    className="w-full bg-zinc-900 border border-zinc-800 p-2 text-[10px] text-zinc-400 focus:outline-none"
                                                                                />
                                                                            </div>
                                                                        </div>
                                                                    </>
                                                                )}

                                                                {invType === 'LOGIC' && (
                                                                    <div className="space-y-1.5">
                                                                        <label className="text-[8px] font-black text-zinc-600 uppercase flex items-center gap-1.5 italic">
                                                                            <Code size={8} /> Script Logic
                                                                        </label>
                                                                        <textarea 
                                                                            placeholder="// define logic here..."
                                                                            value={invSettings.code || ''}
                                                                            onChange={(e) => updateInvaderSettings(idx, { code: e.target.value })}
                                                                            className="w-full h-24 bg-zinc-900 border border-zinc-800 p-2 text-[10px] text-indigo-400 focus:outline-none font-mono resize-none custom-scrollbar"
                                                                        />
                                                                    </div>
                                                                )}

                                                                <button className="w-full py-1.5 bg-zinc-800 hover:bg-zinc-700 border border-zinc-700 rounded-sm text-[8px] font-black uppercase tracking-widest text-zinc-400 flex items-center justify-center gap-2 transition-all">
                                                                    <Save size={10} /> Sync Module
                                                                </button>
                                                            </div>
                                                        </motion.div>
                                                    )}
                                                </AnimatePresence>
                                            </div>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    </section>
                )}
                {/* Edge Configuration */}
                {type === 'edge' && (
                    <section>
                        <div className="flex items-center justify-between mb-4">
                            <h3 className="text-[9px] font-black uppercase tracking-widest text-zinc-600">Relation Style</h3>
                        </div>

                        <div className="space-y-6">
                            {/* Color Picker */}
                            <div>
                                <label className="text-[9px] font-bold text-zinc-700 uppercase mb-3 block tracking-tighter">Connection Color</label>
                                <div className="flex flex-wrap gap-2">
                                    {['#ffffff', '#6366f1', '#f43f5e', '#10b981', '#f59e0b', '#a855f7'].map((color) => (
                                        <button
                                            key={color}
                                            onClick={() => onUpdate(selectedObject.id, { ...data, color })}
                                            className={`
                                                w-6 h-6 rounded-full border-2 transition-all
                                                ${data.color === color ? 'border-white scale-110 shadow-[0_0_10px_rgba(255,255,255,0.3)]' : 'border-transparent hover:scale-105'}
                                            `}
                                            style={{ backgroundColor: color }}
                                        />
                                    ))}
                                </div>
                            </div>

                            {/* Shape Selection */}
                            <div>
                                <label className="text-[9px] font-bold text-zinc-700 uppercase mb-3 block tracking-tighter">Path Geometry</label>
                                <div className="grid grid-cols-3 gap-2">
                                    {[
                                        { id: 'bezier', label: 'Bezier' },
                                        { id: 'straight', label: 'Straight' },
                                        { id: 'step', label: 'Step' }
                                    ].map((shape) => (
                                        <button
                                            key={shape.id}
                                            onClick={() => onUpdate(selectedObject.id, { ...data, shape: shape.id })}
                                            className={`
                                                py-2 text-[9px] font-black uppercase tracking-widest rounded-sm border transition-all
                                                ${data.shape === shape.id 
                                                    ? 'bg-indigo-500/20 border-indigo-500 text-indigo-400' 
                                                    : 'bg-zinc-900 border-zinc-800 text-zinc-600 hover:border-zinc-700 hover:text-zinc-400'}
                                            `}
                                        >
                                            {shape.label}
                                        </button>
                                    ))}
                                </div>
                            </div>

                            {/* Animation Toggle */}
                            <div className="pt-2">
                                <button
                                    onClick={() => onUpdate(selectedObject.id, { ...data, animating: !data.animating })}
                                    className={`
                                        w-full py-2.5 rounded-sm border flex items-center justify-center gap-3 transition-all
                                        ${data.animating 
                                            ? 'bg-emerald-500/10 border-emerald-500/50 text-emerald-400 shadow-[0_0_15px_rgba(16,185,129,0.1)]' 
                                            : 'bg-zinc-900 border-zinc-800 text-zinc-600 hover:border-zinc-700 hover:text-zinc-400'}
                                    `}
                                >
                                    <div className={`w-1.5 h-1.5 rounded-full ${data.animating ? 'bg-emerald-400 animate-pulse' : 'bg-zinc-700'}`} />
                                    <span className="text-[9px] font-black uppercase tracking-widest">
                                        {data.animating ? 'Flow Active' : 'Enable Flow'}
                                    </span>
                                </button>
                            </div>
                        </div>
                    </section>
                )}
            </div>
            
            {/* Action Footer */}
            <div className="p-4 bg-zinc-900/50 border-t border-zinc-800/50">
                <p className="text-[8px] text-zinc-600 font-bold uppercase tracking-widest text-center">Nox Interaction System v0.42</p>
            </div>
        </div>
    );
}
