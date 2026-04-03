import { memo } from 'react';
import { Handle, Position, NodeProps } from 'reactflow';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { useStudio } from '../../../context/StudioContext';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export const NoxJunctionNode = memo(({ selected }: NodeProps) => {
  const { isConnectMode } = useStudio();

  // Optimized handles for a 32x32 node to prevent crowding
  const handleClass = cn(
    "!w-2 !h-2 !bg-zinc-950 !border !border-indigo-500/80 !transition-all duration-200 z-20",
    isConnectMode ? "opacity-100 scale-100" : "opacity-0 scale-0 pointer-events-none"
  );

  return (
    <div 
      className={cn(
        "group relative w-8 h-8 bg-zinc-950 transition-all duration-300",
        selected 
          ? "ring-1 ring-indigo-500 shadow-[0_0_15px_rgba(99,102,241,0.4)]" 
          : "border border-indigo-500/30 hover:border-indigo-500/60"
      )}
    >
      {/* Visual Center Crosshair (Engineering Style) */}
      <div className="absolute inset-0 flex items-center justify-center pointer-events-none opacity-20">
        <div className="w-[1px] h-full bg-indigo-500/50 absolute" />
        <div className="h-[1px] w-full bg-indigo-500/50 absolute" />
      </div>

      {/* 4-Way Hybrid Ports (Source & Target) */}
      
      {/* TOP */}
      <Handle type="target" position={Position.Top} id="t-in" className={cn(handleClass, "!top-0")} isConnectable={isConnectMode} />
      <Handle type="source" position={Position.Top} id="t-out" className={cn(handleClass, "!top-0 opacity-0")} isConnectable={isConnectMode} />
      
      {/* BOTTOM */}
      <Handle type="target" position={Position.Bottom} id="b-in" className={cn(handleClass, "!bottom-0")} isConnectable={isConnectMode} />
      <Handle type="source" position={Position.Bottom} id="b-out" className={cn(handleClass, "!bottom-0 opacity-0")} isConnectable={isConnectMode} />
      
      {/* LEFT */}
      <Handle type="target" position={Position.Left} id="l-in" className={cn(handleClass, "!left-0")} isConnectable={isConnectMode} />
      <Handle type="source" position={Position.Left} id="l-out" className={cn(handleClass, "!left-0 opacity-0")} isConnectable={isConnectMode} />
      
      {/* RIGHT */}
      <Handle type="target" position={Position.Right} id="r-in" className={cn(handleClass, "!right-0")} isConnectable={isConnectMode} />
      <Handle type="source" position={Position.Right} id="r-out" className={cn(handleClass, "!right-0 opacity-0")} isConnectable={isConnectMode} />
    </div>
  );
});

NoxJunctionNode.displayName = 'NoxJunctionNode';
