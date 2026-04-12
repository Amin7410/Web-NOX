import React, { useState, useMemo } from 'react';
import { Search, MapPin, Hash, Palette, Share2, CornerDownRight, Circle, Check } from 'lucide-react';
import { useStudio } from '../../context/StudioContext';
import { NoxNodeType } from '../../types/studio';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

interface ContextMenuAction {
  label: string;
  onClick: () => void;
  variant?: 'default' | 'danger' | 'primary';
  icon?: any;
}

interface ContextMenuProps {
  top: number;
  left: number;
  actions: ContextMenuAction[];
  onClose: () => void;
  nodeId?: string; 
  edgeId?: string; // New: Support for individual edge styling
}

export const ContextMenu = ({ top, left, actions, onClose, nodeId, edgeId }: ContextMenuProps) => {
  const { nodes, setNodes, edges, teleportToNode, updateEdgeStyle, updateNodeOutputStyle } = useStudio();
  const [searchTerm, setSearchTerm] = useState('');

  const targetNode = useMemo(() => nodes.find(n => n.id === nodeId), [nodes, nodeId]);
  const targetEdge = useMemo(() => edges.find(e => e.id === edgeId), [edges, edgeId]);
  const isTerminal = targetNode?.type === 'noxInputTerminal' || targetNode?.type === 'noxOutputTerminal';
  const bridgeId = targetNode?.data.terminalConfig?.bridgeId;

  const currentStyle = targetEdge?.style || (targetNode ? edges.find(e => e.source === nodeId)?.style : undefined);
  const isDashed = targetEdge?.style?.strokeDasharray === '5,5' || (targetNode ? edges.some(e => e.source === nodeId && e.style?.strokeDasharray === '5,5') : false);

  const colors = [
    { label: 'Indigo', value: 'rgba(99, 102, 241, 0.6)' },
    { label: 'Emerald', value: 'rgba(16, 185, 129, 0.6)' },
    { label: 'Amber', value: 'rgba(245, 158, 11, 0.6)' },
    { label: 'Rose', value: 'rgba(239, 68, 68, 0.6)' },
    { label: 'Cyan', value: 'rgba(6, 182, 212, 0.6)' },
    { label: 'Purple', value: 'rgba(168, 85, 247, 0.6)' },
  ];

  const handleStyleChange = (color?: string, dashed?: boolean) => {
    if (edgeId) {
      updateEdgeStyle(edgeId, { color, dashed });
    } else if (nodeId) {
      updateNodeOutputStyle(nodeId, { color, dashed });
    }
  };

  const connections = useMemo(() => {
    if (!isTerminal || !bridgeId) return [];
    const partnerType = targetNode.type === 'noxInputTerminal' ? 'noxOutputTerminal' : 'noxInputTerminal';
    return nodes
      .filter(n => n.id !== nodeId && n.type === partnerType && n.data.terminalConfig?.bridgeId === bridgeId)
      .filter(n => n.data.label.toLowerCase().includes(searchTerm.toLowerCase()));
  }, [nodes, nodeId, isTerminal, bridgeId, searchTerm, targetNode?.type]);

  return (
    <div 
      style={{ top, left }}
      className="fixed z-[1000] min-w-[240px] bg-zinc-950 border border-white/10 shadow-2xl rounded-xl py-2 overflow-hidden animate-in fade-in zoom-in duration-200 backdrop-blur-xl"
      onMouseLeave={onClose}
      onClick={(e) => e.stopPropagation()}
    >
      {/* 1. STYLE PALETTE SECTION (New Requirement) */}
      {(nodeId || edgeId) && (
        <div className="px-4 py-3 border-b border-white/10">
          <div className="flex items-center gap-2 mb-3">
            <Palette size={12} className="text-zinc-500" />
            <span className="text-[10px] font-bold text-zinc-400 uppercase tracking-widest">Architectural Palette</span>
          </div>
          
          <div className="flex flex-wrap gap-2 mb-3">
            {colors.map((c) => (
              <button
                key={c.value}
                onClick={() => handleStyleChange(c.value)}
                className={cn(
                  "w-5 h-5 rounded-full border-2 transition-all hover:scale-110 flex items-center justify-center",
                  (currentStyle?.stroke === c.value) ? "border-white" : "border-transparent"
                )}
                style={{ backgroundColor: c.value }}
              >
                {currentStyle?.stroke === c.value && <Check size={8} className="text-white" />}
              </button>
            ))}
          </div>

          <button 
            onClick={() => handleStyleChange(undefined, !isDashed)}
            className={cn(
              "w-full flex items-center justify-between px-3 py-1.5 rounded bg-zinc-900/50 border transition-all text-[10px] font-bold tracking-tight",
              isDashed ? "border-indigo-500/50 text-indigo-400" : "border-white/5 text-zinc-500"
            )}
          >
            <div className="flex items-center gap-2">
              <Share2 size={12} />
              <span>Dashed Line Mode</span>
            </div>
            <div className={cn("w-2 h-2 rounded-full", isDashed ? "bg-indigo-500 animate-pulse" : "bg-zinc-800")} />
          </button>
        </div>
      )}

      {/* 2. TERMINAL INSPECTOR */}
      {isTerminal && (
        <div className="px-4 py-3 border-b border-white/10 bg-indigo-500/5">
          <div className="relative group mb-2">
            <Hash size={12} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-zinc-600 group-focus-within:text-indigo-400" />
            <input 
              type="text"
              placeholder="Internal Bridge ID..."
              className="w-full bg-zinc-900/50 border border-white/5 rounded-md py-1.5 pl-8 pr-3 text-[10px] text-zinc-200 font-mono focus:outline-none focus:border-indigo-500/50 transition-all uppercase"
              value={bridgeId || ''}
              onChange={(e) => {
                setNodes(nds => nds.map(n => n.id === nodeId ? { ...n, data: { ...n.data, terminalConfig: { ...n.data.terminalConfig!, bridgeId: e.target.value.toUpperCase() } } } : n));
              }}
              autoFocus
            />
          </div>
          <div className="relative group">
            <Search size={12} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-zinc-600" />
            <input 
              type="text"
              placeholder="Filter connections..."
              className="w-full bg-zinc-900/50 border border-white/5 rounded-md py-1.5 pl-8 pr-3 text-[10px] text-zinc-300 focus:outline-none focus:border-indigo-500/50 transition-all disabled:opacity-30"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
        </div>
      )}

      {/* 3. CONNECTION LIST */}
      {isTerminal && bridgeId && (
        <div className="max-h-[180px] overflow-y-auto custom-scrollbar border-b border-white/5">
          {connections.map((conn) => (
            <div key={conn.id} className="group flex items-center justify-between px-3 py-2 border-b border-white/5 last:border-0 hover:bg-white/5">
              <div className="flex flex-col truncate">
                <span className="text-[10px] font-bold text-indigo-300 truncate">{conn.data.label}</span>
                <span className="text-[8px] text-zinc-500 truncate uppercase tracking-tighter">at: {conn.data.parentId || 'MAIN'}</span>
              </div>
              <button 
                onClick={() => { teleportToNode(conn.id); onClose(); }}
                className="px-2 py-1 rounded bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 hover:bg-indigo-500/20 transition-all text-[9px] font-bold uppercase"
              >
                Jump <MapPin size={10} className="inline ml-1" />
              </button>
            </div>
          ))}
        </div>
      )}

      {/* 4. STANDARD ACTIONS */}
      <div className="py-1">
        {actions.map((action, index) => {
          const Icon = action.icon;
          return (
            <button key={index} onClick={(e) => { e.stopPropagation(); action.onClick(); onClose(); }} className={cn("w-full px-4 py-2 text-left text-xs transition-all flex items-center justify-between hover:bg-white/5", action.variant === 'danger' ? 'text-rose-500 font-bold' : (action.variant === 'primary' ? 'text-indigo-400 font-bold' : 'text-zinc-400'))}>
              <div className="flex items-center gap-2">
                {Icon && <Icon size={12} className="opacity-60" />}
                {!Icon && <div className={cn("h-1.5 w-1.5 rounded-full", action.variant === 'primary' ? 'bg-indigo-500' : (action.variant === 'danger' ? 'bg-rose-500' : 'bg-zinc-700'))} />}
                {action.label}
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
};
