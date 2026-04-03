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

export const NoxNode = memo(({ data, selected }: NodeProps<NoxNodeData>) => {
  const { isConnectMode } = useStudio();
  const Icon = IconMap[data.type as keyof typeof IconMap] || Box;
  const isUndefined = data.type === 'undefined';

  const handleClass = cn(
    "!w-3 !h-3 !bg-zinc-950 !border-2 !border-indigo-500/40 !transition-all duration-300 hover:!border-indigo-400 hover:!scale-125 z-10",
    isConnectMode ? "opacity-100 scale-100" : "opacity-0 scale-0 pointer-events-none"
  );

  return (
    <div 
      className={cn(
        "group relative min-w-[180px] rounded-lg bg-zinc-900 shadow-2xl transition-all duration-300",
        selected 
          ? "ring-2 ring-indigo-500 shadow-[0_0_20px_rgba(99,102,241,0.3)]" 
          : "border border-white/20",
        isUndefined && "border-dashed opacity-80"
      )}
    >
      {/* Header */}
      <div className={cn(
        "flex items-center gap-2 px-3 py-2 border-b border-white/10",
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

      {/* Body / Invader Stack */}
      <div className="p-3">
        {data.invaders && data.invaders.length > 0 ? (
          <div className="flex flex-col gap-1.5">
            {data.invaders.map((invader, idx) => (
              <div 
                key={idx} 
                className="flex items-center gap-2 px-2 py-1.5 rounded bg-zinc-950 border border-white/5 group/invader hover:border-indigo-500/30 transition-colors"
              >
                <div className="h-1 w-1 rounded-full bg-indigo-500 shadow-[0_0_4px_rgba(99,102,241,0.5)]" />
                <span className="text-[10px] font-mono text-zinc-400 group-hover/invader:text-zinc-200 uppercase">
                  {invader}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <div className="py-4 border border-dashed border-white/5 rounded flex items-center justify-center">
             <span className="text-[9px] text-zinc-600 uppercase tracking-[0.2em] font-light italic">
               No Soul Attached
             </span>
          </div>
        )}
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
