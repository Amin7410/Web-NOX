import React, { memo } from 'react';
import { NodeProps } from 'reactflow';
import { Cpu } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export interface NoxInvaderHubData {
  label: string;
  type: string;
}

export const NoxInvaderHub = memo(({ data, selected }: NodeProps<NoxInvaderHubData>) => {
  // Use a slightly longer slice if needed, but keeping it concise
  const displayLabel = (data.label || 'HUB').substring(0, 4).toUpperCase();

  return (
    <div 
      className={cn(
        "relative flex flex-col items-center justify-start py-1.5 rounded-sm transition-all duration-300",
        "bg-[#0D0D0F] border shadow-2xl w-[32px] h-[52px] group/chip",
        selected 
          ? "border-purple-500 shadow-[0_0_15px_rgba(168,85,247,0.4)]" 
          : "border-white/5 hover:border-purple-500/40"
      )}
    >
      {/* Top Status LED - Smaller and cleaner */}
      <div className={cn(
         "w-1 h-1 rounded-full mb-2",
         selected ? "bg-purple-500 shadow-[0_0_4px_rgba(168,85,247,1)]" : "bg-purple-900/30"
      )} />

      {/* Main Label: Centered and much smaller for professional look */}
      <div className="flex-1 flex items-center justify-center w-full px-0.5 mt-1">
        <span className="text-[7px] font-mono font-bold text-purple-400/90 tracking-tighter leading-none select-none text-center break-all">
          {displayLabel}
        </span>
      </div>

      {/* Simplified Hardware detail: Single divider line */}
      <div className="w-4 h-[1px] bg-white/5 mb-2" />

      {/* Gold-Finger Contacts at the bottom (The Plugging Part) */}
      <div className="absolute -bottom-[1px] left-0 right-0 h-[4px] bg-gradient-to-t from-amber-500/30 to-transparent flex justify-around px-1 overflow-hidden rounded-b-sm">
        {[1,2,3].map(i => (
          <div key={i} className="w-[1.5px] h-full bg-amber-600/40" />
        ))}
      </div>
      
      {/* Tooltip on mouse over for full label visibility */}
      <div className="absolute -top-10 left-1/2 -translate-x-1/2 hidden group-hover/chip:block z-50">
        <div className="bg-zinc-950 border border-purple-500/30 px-2 py-1 rounded shadow-2xl text-[9px] whitespace-nowrap text-purple-100 font-mono">
           {data.label}
        </div>
      </div>
    </div>
  );
});

NoxInvaderHub.displayName = 'NoxInvaderHub';
