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

  // Optimized handles for a 40x40 node
  const handleClass = cn(
    "!w-2.5 !h-2.5 !bg-zinc-950 !border !border-indigo-500 !transition-all duration-200 z-50 hover:!scale-125",
    isConnectMode ? "opacity-100 scale-100" : "opacity-0 scale-0 pointer-events-none"
  );

  return (
    <div
      className={cn(
        "group relative w-10 h-10 bg-zinc-950 transition-all duration-300",
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

      {/* Primary Handles (Current Version Design) */}
      {/* Vertical IN (Top & Bottom Center) */}
      <Handle type="target" position={Position.Top} id="t-in" className={handleClass} isConnectable={isConnectMode} />
      <Handle type="target" position={Position.Bottom} id="b-in" className={handleClass} isConnectable={isConnectMode} />
      
      {/* Horizontal OUT (Left & Right Center) */}
      <Handle type="source" position={Position.Left} id="l-out" className={handleClass} isConnectable={isConnectMode} />
      <Handle type="source" position={Position.Right} id="r-out" className={handleClass} isConnectable={isConnectMode} />

      {/* Legacy Support (Hidden): Handles to prevent Error #008 for old data from any edge */}
      {/* Inputs (Target) */}
      <Handle type="target" position={Position.Left} id="l-in" className="opacity-0 pointer-events-none" />
      <Handle type="target" position={Position.Right} id="r-in" className="opacity-0 pointer-events-none" />
      
      {/* Outputs (Source) - Important: Fixed b-out error reported by user */}
      <Handle type="source" position={Position.Top} id="t-out" className="opacity-0 pointer-events-none" />
      <Handle type="source" position={Position.Bottom} id="b-out" className="opacity-0 pointer-events-none" />
    </div>
  );
});

NoxJunctionNode.displayName = 'NoxJunctionNode';
