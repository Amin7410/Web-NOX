import React, { memo } from 'react';
import { Handle, Position, NodeProps } from 'reactflow';
import { Box, Zap, Database, Globe, Layers, Cpu } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { useStudio } from '../../../context/StudioContext';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export type NoxNodeType = 'undefined' | 'logic' | 'data' | 'ui' | 'system';

export interface NoxNodeData {
  label: string;
  type: NoxNodeType;
  invaders?: string[];
  isDefined?: boolean;
}

const IconMap = {
  undefined: Box,
  logic: Zap,
  data: Database,
  ui: Globe,
  layers: Layers,
  system: Cpu
};

export const NoxNode = memo(({ id, data, selected }: NodeProps<NoxNodeData>) => {
  const { isConnectMode, toggleRightSidebar } = useStudio();
  const Icon = IconMap[data.type as keyof typeof IconMap] || Box;
  const isUndefined = data.type === 'undefined';

  const handleClass = cn(
    "!w-3 !h-3 !bg-zinc-950 !border-2 !border-indigo-500/40 !transition-all duration-300 hover:!border-indigo-400 hover:!scale-125 z-10",
    isConnectMode ? "opacity-100 scale-100" : "opacity-0 scale-0 pointer-events-none"
  );

  return (
    <div 
      className={cn(
        "group relative min-w-[200px] rounded-lg bg-zinc-900 shadow-2xl transition-all duration-300",
        selected 
          ? "ring-2 ring-indigo-500 shadow-[0_0_20px_rgba(99,102,241,0.3)]" 
          : "border border-white/20",
        isUndefined && "border-dashed opacity-80"
      )}
    >
      {/* Refined Ergonomic Soul Hub (Offset from corner) */}
      <div className="absolute -top-[20px] left-3 flex h-[20px] items-center rounded-t-md bg-zinc-900 border-t border-l border-r border-white/20 shadow-lg z-0 overflow-hidden">
        {/* Tactile Chip Button */}
        <button 
          onClick={(e) => {
            e.stopPropagation();
            toggleRightSidebar(true, id);
          }}
          className="flex items-center justify-center px-2 h-full hover:bg-purple-500/10 transition-colors group/soulbtn"
          title="Access Soul Control"
        >
          <Cpu size={10} className="text-zinc-600 group-hover/soulbtn:text-purple-400 transition-all" />
        </button>
      </div>

      {/* Header */}
      <div className={cn(
        "flex items-center gap-2 px-3 py-2 border-b border-white/10 rounded-t-lg",
        selected ? "bg-indigo-500/10" : "bg-white/5"
      )}>
        <div className={cn(
          "p-1 rounded bg-zinc-800 border border-white/10",
          !isUndefined && "text-indigo-400 border-indigo-500/30"
        )}>
          <Icon size={14} />
        </div>
        <div className="flex flex-col">
          <span className="text-[10px] font-bold text-zinc-500 uppercase tracking-widest leading-none mb-1">
            {data.type}
          </span>
          <span className="text-xs font-semibold text-zinc-100 truncate max-w-[120px]">
            {data.label}
          </span>
        </div>
      </div>

      {/* Content / Architectural Specification */}
      <div className="p-4 space-y-3">
        <div className="space-y-2 opacity-60">
           <div className="flex justify-between items-center text-[8px] font-mono tracking-widest uppercase">
              <span className="text-zinc-500 italic">ARCHITECT_ID</span>
              <span className="text-zinc-300">B-0{Math.floor(Math.random() * 99 + 1)}</span>
           </div>
           <div className="flex justify-between items-center text-[8px] font-mono tracking-widest uppercase">
              <span className="text-zinc-500 italic">MODULE_TYPE</span>
              <span className="text-zinc-300">{data.type}</span>
           </div>
           <div className="flex justify-between items-center text-[8px] font-mono tracking-widest uppercase">
              <span className="text-zinc-500 italic">SYSTEM_STATUS</span>
              <span className="text-emerald-500/70">READY</span>
           </div>
        </div>

        {/* Minimalist Soul Bank Visual (Placeholder) */}
        <div className="mt-4 pt-3 border-t border-white/5">
           <div className="h-10 rounded border border-dashed border-white/10 flex items-center justify-center bg-black/10">
              <span className="text-[7px] text-zinc-700 uppercase tracking-[0.3em] font-bold italic">
                Architectural Core
              </span>
           </div>
        </div>
      </div>

      {/* Connection Handles (Technically Styled) */}
      <Handle
        type="target"
        position={Position.Left}
        className={cn(handleClass, "!rounded-sm shadow-[0_0_10px_rgba(99,102,241,0.2)]")}
        isConnectable={isConnectMode}
      />
      <Handle
        type="source"
        position={Position.Right}
        className={cn(handleClass, "!rounded-sm shadow-[0_0_10px_rgba(99,102,241,0.2)]")}
        isConnectable={isConnectMode}
      />

      {/* Crosshair Visual Decoration (Visible only when connecting) */}
      {isConnectMode && (
        <>
          <div className="absolute top-1/2 left-0 -translate-x-1/2 -translate-y-1/2 w-4 h-[1px] bg-indigo-500/20 pointer-events-none" />
          <div className="absolute top-1/2 right-0 translate-x-1/2 -translate-y-1/2 w-4 h-[1px] bg-indigo-500/20 pointer-events-none" />
        </>
      )}

      {/* Footer / ID Tag */}
      <div className="px-3 py-1 flex justify-end">
        <span className="text-[8px] font-mono text-zinc-700 uppercase">Architect_ID: B-001</span>
      </div>
    </div>
  );
});

NoxNode.displayName = 'NoxNode';
