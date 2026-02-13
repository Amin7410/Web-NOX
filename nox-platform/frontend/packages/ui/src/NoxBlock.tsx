import { memo, useMemo } from 'react';
import { NodeProps } from 'reactflow';
import { Settings, Activity, Cpu, Box, Database, Server, Zap, Layers, Code, Terminal, Lock } from 'lucide-react';

// --- Types (Database Mapping) ---

export interface BlockVisual {
    variant?: 'default' | 'rugged' | 'sleek' | 'cyber';
    color?: string; // hex or tailwind color class prefix (e.g. 'blue', 'red')
    icon?: string; // Icon identifier
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
    meta?: Record<string, any>;
}

// --- Icons Map ---
const ICON_MAP: Record<string, any> = {
    cpu: Cpu,
    box: Box,
    database: Database,
    server: Server,
    activity: Activity,
    settings: Settings,
    zap: Zap,
    layers: Layers,
    code: Code,
    terminal: Terminal,
    lock: Lock
};

// --- Sub-Components ---

const StatusLed = memo(({ status }: { status?: BlockStatus['state'] }) => {
    const colorClass = useMemo(() => {
        switch (status) {
            case 'running': return 'bg-emerald-400 shadow-[0_0_10px_2px_rgba(52,211,153,0.6)] animate-pulse-slow';
            case 'error': return 'bg-red-500 shadow-[0_0_12px_4px_rgba(239,68,68,0.8)] animate-flicker';
            case 'warning': return 'bg-amber-400 shadow-[0_0_10px_rgba(251,191,36,0.6)]';
            case 'stopped': return 'bg-zinc-600 border border-zinc-500';
            default: return 'bg-zinc-700';
        }
    }, [status]);

    return <div className={`w-2.5 h-2.5 rounded-full transition-all duration-300 border border-black/20 ${colorClass}`} />;
});

const BlockHeader = memo(({ label, icon, status, color = 'blue', variant }: { label: string, icon?: string, status?: BlockStatus['state'], color?: string, variant?: string }) => {
    const IconComponent = ICON_MAP[icon?.toLowerCase() || 'box'] || Box;

    // Dynamic header styles based on variant
    const headerStyle = useMemo(() => {
        if (variant === 'rugged') return 'bg-zinc-900 border-b-2 border-zinc-800';
        if (variant === 'sleek') return 'bg-white/[0.03] border-b border-white/5 backdrop-blur-md';
        return 'bg-zinc-900/80 border-b border-zinc-800';
    }, [variant]);

    return (
        <div className={`h-10 flex items-center justify-between px-3 relative overflow-hidden shrink-0 ${headerStyle}`}>
            {/* Heavy Industrial Stripe for Rugged */}
            {variant === 'rugged' && (
                <div className="absolute top-0 left-0 bottom-0 w-1 bg-gradient-to-b from-amber-500/50 to-amber-600/20" />
            )}

            <div className="flex items-center gap-3 min-w-0">
                <div className={`
           p-1.5 rounded flex items-center justify-center relative group
           ${status === 'running'
                        ? `text-${color}-400 bg-${color}-500/10 shadow-[0_0_10px_-2px_rgba(var(--${color}-500),0.3)]`
                        : 'text-zinc-500 bg-zinc-800/80'}
           transition-all duration-300 border border-white/5
        `}>
                    <IconComponent size={14} strokeWidth={2.5} />
                    {/* Icon Glow on Hover */}
                    <div className={`absolute inset-0 rounded bg-${color}-400/20 blur opacity-0 group-hover:opacity-100 transition-opacity`} />
                </div>

                <div className="flex flex-col">
                    <span className="text-[11px] font-bold text-zinc-100 tracking-wider uppercase truncate max-w-[140px] leading-tight">
                        {label}
                    </span>
                    <span className="text-[9px] text-zinc-500 font-mono leading-tight">{status?.toUpperCase() || 'IDLE'}</span>
                </div>
            </div>

            <StatusLed status={status} />
        </div>
    );
});

const BlockContent = memo(({ variant }: { variant?: string }) => {
    return (
        <div className="flex-grow w-full relative group overflow-hidden flex flex-col">
            {/* Background Patterns */}
            <div className="absolute inset-0 bg-[#050505]" />

            {/* Grid Pattern */}
            <div className="absolute inset-0 opacity-[0.07]"
                style={{
                    backgroundImage: 'linear-gradient(#444 1px, transparent 1px), linear-gradient(90deg, #444 1px, transparent 1px)',
                    backgroundSize: '16px 16px'
                }}
            />

            {/* Scan line effect (Sleek/Cyber only) */}
            {(variant === 'sleek' || variant === 'cyber') && (
                <div className="absolute inset-0 bg-gradient-to-b from-transparent via-blue-400/[0.03] to-transparent bg-[length:100%_200%] animate-scan pointer-events-none" />
            )}

            {/* Content Placeholder - Simulating Data */}
            <div className="relative z-10 p-3 flex flex-col gap-2 h-full opacity-60 group-hover:opacity-80 transition-opacity">
                {/* Fake Code Lines */}
                <div className="w-3/4 h-1.5 bg-zinc-800 rounded-full" />
                <div className="w-1/2 h-1.5 bg-zinc-800 rounded-full" />
                <div className="w-2/3 h-1.5 bg-zinc-800 rounded-full" />

                {/* Center Graphic */}
                <div className="mt-auto mb-auto self-center">
                    <div className="w-16 h-8 rounded border border-zinc-800 bg-zinc-900/50 flex items-center justify-center">
                        <span className="text-[9px] font-mono text-zinc-600">DATA</span>
                    </div>
                </div>
            </div>
        </div>
    );
});

const BlockFooter = memo(({ id, status, variant }: { id: string, status?: BlockStatus, variant?: string }) => {
    const footerStyle = variant === 'rugged' ? 'bg-zinc-900 border-t border-zinc-800' : 'bg-zinc-950/50 border-t border-white/5';

    return (
        <div className={`h-7 flex items-center justify-between px-3 text-[9px] text-zinc-500 font-mono shrink-0 ${footerStyle}`}>
            <span className="tracking-tighter opacity-70">ID: {id.substring(0, 6)}</span>
            <div className="flex gap-2 items-center">
                {status?.message && (
                    <span className={`px-1 rounded bg-red-500/10 text-red-400 font-bold ${variant === 'rugged' ? 'border border-red-900' : ''}`}>
                        {status.message}
                    </span>
                )}
                <span className="opacity-50">{status?.lastActive ? `${Date.now() - status.lastActive}ms` : 'IDLE'}</span>
            </div>
        </div>
    );
});

// --- Main Components ---

export const NoxBlock = memo(({ data, selected }: NodeProps<NoxBlockData>) => {
    const variant = data.visual?.variant || 'default';

    // Styles based on variant
    const variantStyles = useMemo(() => {
        switch (variant) {
            case 'rugged':
                return 'border-2 border-zinc-700 bg-zinc-950 rounded shadow-[4px_4px_0px_rgba(0,0,0,0.5)]';
            case 'sleek':
                return 'border border-white/10 bg-black/40 backdrop-blur-xl rounded-2xl shadow-2xl ring-1 ring-white/5';
            case 'cyber':
                return 'border border-purple-500/30 bg-black/80 rounded-lg shadow-[0_0_20px_rgba(168,85,247,0.1)]';
            default: // 'default'
                return `border-[1px] bg-[#09090b] rounded-xl shadow-xl border-zinc-800`;
        }
    }, [variant]);

    const selectedStyles = selected
        ? (variant === 'rugged' ? 'border-amber-500 shadow-[4px_4px_0px_rgba(245,158,11,0.5)]' : 'ring-2 ring-blue-500/50 shadow-[0_0_40px_-10px_rgba(59,130,246,0.5)]')
        : 'hover:border-zinc-600';

    return (
        <div className={`
        relative min-w-[280px] h-[160px] group transition-all duration-300 will-change-transform
        ${selected ? 'scale-[1.02] z-50' : 'z-0'}
    `}>
            {/* Main Chassis */}
            <div className={`flex flex-col h-full w-full overflow-hidden transition-all duration-300 ${variantStyles} ${selectedStyles}`}>

                {/* Decor: Screws for Rugged/Default */}
                {(variant === 'rugged' || variant === 'default') && (
                    <>
                        <div className="absolute top-2 left-2 w-1 h-1 rounded-full bg-zinc-700 shadow-[inset_0_1px_1px_rgba(0,0,0,0.8)] z-20" />
                        <div className="absolute top-2 right-2 w-1 h-1 rounded-full bg-zinc-700 shadow-[inset_0_1px_1px_rgba(0,0,0,0.8)] z-20" />
                        <div className="absolute bottom-2 left-2 w-1 h-1 rounded-full bg-zinc-700 shadow-[inset_0_1px_1px_rgba(0,0,0,0.8)] z-20" />
                        <div className="absolute bottom-2 right-2 w-1 h-1 rounded-full bg-zinc-700 shadow-[inset_0_1px_1px_rgba(0,0,0,0.8)] z-20" />
                    </>
                )}

                <BlockHeader
                    label={data.label}
                    icon={data.visual?.icon}
                    status={data.status?.state}
                    color={data.visual?.color}
                    variant={variant}
                />

                <BlockContent variant={variant} />

                <BlockFooter id={data.id} status={data.status} variant={variant} />
            </div>
        </div>
    );
});

NoxBlock.displayName = 'NoxBlock';
