"use client";

import { memo, useMemo } from 'react';
import { NodeProps, Handle, Position } from 'reactflow';
import { Settings, Activity, Cpu, Box, Database, Server, Zap, Layers, Code, Terminal, Lock, AlertTriangle } from 'lucide-react';

// --- Types ---

export interface BlockVisual {
    color?: string; // hex or tailwind color class (e.g., 'blue', 'amber')
    icon?: string;
}

export interface BlockStatus {
    state: 'running' | 'stopped' | 'error' | 'warning' | 'offline';
    message?: string;
    lastActive?: number;
}

export interface NoxBlockData {
    id: string;
    label: string;
    type: string;
    visual?: BlockVisual;
    status?: BlockStatus;
    logic?: string; // The "Soul" - Code snippet or logic description
    meta?: Record<string, any>;
    isSource?: boolean; // Visual state for connection source
}

// --- Icons Map ---
const ICON_MAP: Record<string, any> = {
    cpu: Cpu, box: Box, database: Database, server: Server,
    activity: Activity, settings: Settings, zap: Zap,
    layers: Layers, code: Code, terminal: Terminal, lock: Lock
};

// --- Sub-Components ---

const BlockHeader = memo(({ label, icon, color = 'zinc' }: { label: string, icon?: string, color?: string }) => {
    const IconComponent = ICON_MAP[icon?.toLowerCase() || 'box'] || Box;

    return (
        <div className={`
            h-8 flex items-center px-3 gap-2 border-b border-zinc-700 bg-zinc-900/50
            rounded-t-sm
        `}>
            {/* Icon Box */}
            <div className={`
                w-5 h-5 flex items-center justify-center rounded-sm
                bg-${color}-500/10 text-${color}-400
            `}>
                <IconComponent size={12} strokeWidth={2} />
            </div>

            {/* Label */}
            <span className="text-xs font-bold text-zinc-200 tracking-wide uppercase truncate leading-none">
                {label}
            </span>
        </div>
    );
});

const BlockLogic = memo(({ logic }: { logic?: string }) => {
    if (!logic) return (
        <div className="flex-grow flex items-center justify-center p-2 opacity-30">
            <span className="text-[10px] font-mono text-zinc-500 italic">No Logic Defined</span>
        </div>
    );

    return (
        <div className="flex-grow p-2 bg-zinc-950/50 overflow-hidden relative group">
            <div className="absolute top-0 right-0 p-1 opacity-50">
                <Code size={10} className="text-zinc-500" />
            </div>
            <pre className="font-mono text-[10px] text-zinc-400 leading-relaxed whitespace-pre-wrap break-words">
                {logic}
            </pre>
        </div>
    );
});

const BlockFooter = memo(({ id, status }: { id: string, status?: BlockStatus }) => {
    const isError = status?.state === 'error';

    return (
        <div className={`
            h-6 flex items-center justify-between px-2 border-t border-zinc-700 bg-zinc-900/50
            text-[9px] font-mono rounded-b-sm
        `}>
            <span className="text-zinc-600">ID: {id?.substring(0, 6) || 'unknown'}</span>

            <div className="flex items-center gap-2">
                {status?.message && (
                    <span className={`flex items-center gap-1 ${isError ? 'text-red-400' : 'text-zinc-400'}`}>
                        {isError && <AlertTriangle size={8} />}
                        {status.message}
                    </span>
                )}
                {/* Status Indicator Dot */}
                <div className={`
                    w-1.5 h-1.5 rounded-full
                    ${status?.state === 'running' ? 'bg-emerald-500' :
                        status?.state === 'error' ? 'bg-red-500' :
                            'bg-zinc-600'}
                `} />
            </div>
        </div>
    );
});

// --- Main Component ---

export const NoxBlock = memo(({ data, selected }: NodeProps<NoxBlockData>) => {
    // "Industrial" Look: Sharp corners, high contrast, utility focused
    return (
        <div className={`
            relative min-w-[240px] shadow-lg transition-shadow duration-200
            ${selected ? 'shadow-[0_0_0_1px_#3b82f6]' : ''}
            ${data.isSource ? 'shadow-[0_0_0_2px_#3b82f6] scale-105' : ''}
        `}>
            {/* Source Indicator Label */}
            {data.isSource && (
                <div className="absolute -top-8 left-1/2 -translate-x-1/2 bg-blue-500 text-white text-[10px] px-2 py-0.5 rounded-full font-bold shadow-lg animate-bounce">
                    SOURCE
                </div>
            )}

            {/* Invader Connection Slot (Antenna/Notch) */}
            <div className="absolute -top-1.5 left-4 z-0">
                <div className="flex flex-col items-center">
                    {/* The Pin/Antenna */}
                    <div className={`w-6 h-1.5 rounded-t-sm border-t border-l border-r border-zinc-600 bg-zinc-800 ${data.logic ? 'bg-amber-500/10 border-amber-500/50' : ''}`} />
                    {/* Connection Status Indicator */}
                    {data.logic && (
                        <div className="w-4 h-[2px] bg-amber-400 mt-[1px] shadow-[0_0_8px_rgba(251,191,36,0.8)]" />
                    )}
                </div>
            </div>

            {/* Main Chassis */}
            <div className={`
                flex flex-col bg-zinc-900 border border-zinc-700 rounded-sm overflow-hidden relative z-10
            `}>
                <BlockHeader
                    label={data.label}
                    icon={data.visual?.icon}
                    color={data.visual?.color}
                />

                {/* Body: Logic Display */}
                <div className="min-h-[60px] flex flex-col bg-zinc-800">
                    <BlockLogic logic={data.logic} />
                </div>

                <BlockFooter id={data.id} status={data.status} />
            </div>

            {/* Ports / Handles - Styled as actual connector points */}
            <Handle
                type="target"
                position={Position.Left}
                isConnectable={false}
                className="!w-2 !h-2 !rounded-none !bg-zinc-400 !border-2 !border-zinc-900 hover:!bg-blue-400 !-left-1.5"
            />
            <Handle
                type="source"
                position={Position.Right}
                isConnectable={false}
                className="!w-2 !h-2 !rounded-none !bg-zinc-400 !border-2 !border-zinc-900 hover:!bg-blue-400 !-right-1.5"
            />
        </div>
    );
});

NoxBlock.displayName = 'NoxBlock';
