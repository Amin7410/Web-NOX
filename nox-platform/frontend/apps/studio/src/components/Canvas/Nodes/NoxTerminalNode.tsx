import React, { memo, useMemo } from 'react';
import { Handle, Position, NodeProps } from 'reactflow';
import { LogIn, LogOut, Radio } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { useStudio } from '../../../context/StudioContext';
import { NoxNodeData } from '../../../types/studio';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export const NoxTerminalNode = memo(({ id, data, selected }: NodeProps<NoxNodeData>) => {
  const { isConnectMode, nodes } = useStudio();
  const isInput = data.type === 'inputTerminal';
  const bridgeId = data.terminalConfig?.bridgeId;
  const parentSide = data.terminalConfig?.parentHandle || 'top';

  // Conflict Detection Logic: Check for pairing integrity
  const hasPartner = useMemo(() => {
    if (!bridgeId) return true; // Unregistered nodes aren't in conflict, just silent
    const partnerType = isInput ? 'noxOutputTerminal' : 'noxInputTerminal';
    return nodes.some(n => n.id !== id && n.type === partnerType && n.data.terminalConfig?.bridgeId === bridgeId);
  }, [nodes, id, bridgeId, isInput]);

  const isConflict = bridgeId && !hasPartner;

  return (
    <div 
      className={cn(
        "group relative w-10 h-10 flex items-center justify-center transition-all duration-300",
        isConflict ? "animate-pulse" : ""
      )}
    >
      {/* High-fidelity Hexagonal Case */}
      <div 
        className={cn(
          "absolute inset-0 bg-zinc-950 border-2 transition-all duration-300 transform rotate-45 rounded-sm",
          selected 
            ? (isInput ? "border-emerald-500 shadow-[0_0_15px_rgba(16,185,129,0.3)]" : "border-amber-500 shadow-[0_0_15px_rgba(245,158,11,0.3)]")
            : (isConflict 
                ? "border-rose-600 shadow-[0_0_20px_rgba(225,29,72,0.4)]" 
                : (isInput ? "border-emerald-500/30 hover:border-emerald-500/60" : "border-amber-500/30 hover:border-amber-500/60"))
        )}
      />

      {/* Bridge ID Label (Quick Identification) */}
      {bridgeId && (
        <div className={cn(
            "absolute -top-6 px-1.5 py-0.5 rounded border text-[7px] font-mono font-bold tracking-tighter uppercase backdrop-blur-md",
            isConflict 
              ? "bg-rose-500/10 border-rose-500/30 text-rose-400" 
              : "bg-zinc-900/80 border-white/5 text-zinc-400"
        )}>
          {bridgeId}
        </div>
      )}

      {/* Central Radiator / Signal Icon */}
      <div className={cn(
        "relative z-10 flex flex-col items-center transition-colors",
        isConflict ? "text-rose-500" : (isInput ? "text-emerald-400" : "text-amber-400")
      )}>
        {isInput ? <LogIn size={16} /> : <LogOut size={16} />}
        <Radio size={8} className={cn("mt-0.5", bridgeId ? "opacity-100" : "opacity-0")} />
      </div>

      {/* Terminal Label Tooltip */}
      <div className={cn(
        "absolute top-full mt-2 whitespace-nowrap px-2 py-0.5 rounded bg-zinc-900/80 border border-white/5 text-[8px] font-mono tracking-widest uppercase text-zinc-500 opacity-0 group-hover:opacity-100 transition-opacity",
        isInput ? "right-full mr-2" : "left-full ml-2"
      )}>
        {isConflict ? "Signal Conflict: No Partner" : (isInput ? `Entry from ${parentSide}` : `Exit to ${parentSide}`)}
      </div>

      {/* Smart Handles */}
      {isInput ? (
        <Handle 
          type="source" 
          position={Position.Right} 
          id="source-port"
          className={cn(
            "!w-3 !h-3 !bg-zinc-950 !border !transition-all duration-200 !z-50 hover:!scale-125",
            isInput ? "!border-emerald-500" : "!border-amber-500",
            isConnectMode ? "opacity-100 scale-100" : "opacity-0 scale-0"
          )}
          isConnectable={isConnectMode}
        />
      ) : (
        <Handle 
          type="target" 
          position={Position.Left} 
          id="target-port"
          className={cn(
            "!w-3 !h-3 !bg-zinc-950 !border !transition-all duration-200 !z-50 hover:!scale-125",
            isInput ? "!border-emerald-500" : "!border-amber-500",
            isConnectMode ? "opacity-100 scale-100" : "opacity-0 scale-0"
          )}
          isConnectable={isConnectMode}
        />
      )}
    </div>
  );
});

NoxTerminalNode.displayName = 'NoxTerminalNode';
