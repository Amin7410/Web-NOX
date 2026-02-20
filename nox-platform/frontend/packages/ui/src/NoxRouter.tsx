"use client";

import { memo } from 'react';
import { Handle, Position, NodeProps } from 'reactflow';

export const NoxRouter = memo(({ data, selected }: NodeProps) => {
    return (
        <div className="relative group flex flex-col items-center justify-center">
            {/* The Visual Node (Dot) */}
            <div className={`
                w-4 h-4 rounded-full bg-zinc-400 border-2 border-zinc-800 shadow-md transition-all duration-200 z-10
                group-hover:scale-125 group-hover:bg-blue-400 group-hover:border-blue-500
                ${selected ? 'bg-blue-500 border-blue-300 shadow-[0_0_10px_rgba(59,130,246,0.6)] scale-110' : ''}
            `} />

            {/* Optional Label */}
            {data.label && data.label !== 'Router' && (
                <div className={`
                    absolute top-5 text-[9px] font-medium bg-zinc-900/80 px-1.5 py-0.5 rounded text-zinc-300 border border-zinc-700 pointer-events-none whitespace-nowrap transition-opacity
                    ${selected ? 'opacity-100' : 'opacity-0 group-hover:opacity-100'}
                `}>
                    {data.label}
                </div>
            )}

            {/* Handles - Using central absolute positioning to make it feel like a single point */}
            <Handle
                type="target"
                position={Position.Top}
                className="!w-full !h-full !rounded-full !bg-transparent !border-none !top-0 !left-0 z-0"
                isConnectable={true}
            />
            <Handle
                type="source"
                position={Position.Bottom}
                className="!w-full !h-full !rounded-full !bg-transparent !border-none !top-0 !left-0 z-0"
                isConnectable={true}
            />
        </div>
    );
});

NoxRouter.displayName = 'NoxRouter';
