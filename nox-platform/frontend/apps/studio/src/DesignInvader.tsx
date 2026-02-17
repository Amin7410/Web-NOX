
import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Settings, Plus, X, Box, Zap, Activity, ArrowRight, Play, Database, Globe, Lock, Cpu, GripVertical, Search } from 'lucide-react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

// Utility for tailwind class merging
function cn(...inputs: (string | undefined | null | false)[]) {
    return twMerge(clsx(inputs));
}

// --- Types ---
type InvaderType = 'TRIGGER' | 'LOGIC' | 'OUTPUT';

interface InvaderDef {
    id: string;
    name: string;
    type: InvaderType;
    icon: React.ElementType;
    color: string;
    description: string;
}

interface InstalledInvader {
    id: string;
    defId: string;
    config: Record<string, any>;
}

// --- Mock Library ---
const INVADER_LIBRARY: InvaderDef[] = [
    // RED (Triggers)
    { id: 'http_listener', name: 'HTTP Request', type: 'TRIGGER', icon: Globe, color: 'text-red-400', description: 'Trigger on incoming HTTP request' },
    { id: 'schedule', name: 'Schedule', type: 'TRIGGER', icon: Play, color: 'text-red-400', description: 'Run mostly on CRON schedule' },
    { id: 'auth_guard', name: 'Auth Guard', type: 'TRIGGER', icon: Lock, color: 'text-red-400', description: 'Block unauthorized execution' },

    // BLUE (Logic)
    { id: 'json_parser', name: 'JSON Parser', type: 'LOGIC', icon: Cpu, color: 'text-blue-400', description: 'Parse string to JSON object' },
    { id: 'calc_tax', name: 'Calculate Tax', type: 'LOGIC', icon: Activity, color: 'text-blue-400', description: 'Apply 10% VAT' },
    { id: 'transform', name: 'Transform Data', type: 'LOGIC', icon: Box, color: 'text-blue-400', description: 'Map fields to new structure' },

    // GREEN (Output)
    { id: 'http_response', name: 'HTTP Response', type: 'OUTPUT', icon: ArrowRight, color: 'text-emerald-400', description: 'Send response back to client' },
    { id: 'db_save', name: 'PixelDB Save', type: 'OUTPUT', icon: Database, color: 'text-emerald-400', description: 'Persist data' },
];

// --- Components ---

const ToolboxItem = ({ def }: { def: InvaderDef }) => {
    return (
        <div
            draggable
            onDragStart={(e) => {
                e.dataTransfer.setData('invader/id', def.id);
                e.dataTransfer.setData('invader/type', def.type);
                e.dataTransfer.effectAllowed = 'copy';
            }}
            className="flex items-center gap-3 p-2 rounded hover:bg-zinc-800 border border-transparent hover:border-zinc-700 cursor-grab active:cursor-grabbing transition-all group"
        >
            <div className={cn("p-1.5 rounded bg-zinc-900 border border-zinc-800 group-hover:bg-zinc-950", def.color)}>
                <def.icon size={16} />
            </div>
            <div>
                <div className="text-xs font-bold text-zinc-300 group-hover:text-white">{def.name}</div>
                <div className="text-[10px] text-zinc-500">{def.type}</div>
            </div>
            <GripVertical size={12} className="ml-auto text-zinc-700 opacity-0 group-hover:opacity-100" />
        </div>
    );
};

const SlotHeader = ({
    type,
    colorClass,
    bgClass,
    count,
    isOpen,
    onClick,
    onDrop
}: {
    type: string,
    colorClass: string,
    bgClass: string,
    count: number,
    isOpen: boolean,
    onClick: () => void,
    onDrop: (item: any) => void
}) => {
    const [isDragOver, setIsDragOver] = useState(false);

    return (
        <button
            onClick={onClick}
            onDragOver={(e) => {
                e.preventDefault();
                // Only allow drop if type matches
                const dragType = e.dataTransfer.types.includes('invader/type') ? 'CHECK' : null; // Can't read data in dragover, but we can trust user for now or check types if we set them
                setIsDragOver(true);
            }}
            onDragLeave={() => setIsDragOver(false)}
            onDrop={(e) => {
                e.preventDefault();
                setIsDragOver(false);
                const defId = e.dataTransfer.getData('invader/id');
                const dragType = e.dataTransfer.getData('invader/type');

                if (dragType === type.toUpperCase()) {
                    onDrop(defId);
                }
            }}
            className={cn(
                "flex-1 h-1.5 transition-all duration-300 relative group",
                isOpen ? "h-8" : "hover:h-3"
            )}
        >
            <div className={cn(
                "absolute inset-0 w-full h-full flex items-center justify-center gap-2 overflow-hidden transition-all",
                bgClass,
                isOpen ? "rounded-t-md opacity-100" : "opacity-60 group-hover:opacity-100",
                isDragOver ? "bg-white/20 opacity-100 h-10 -translate-y-2 rounded-t-lg shadow-lg ring-2 ring-white/50" : ""
            )}>
                {(isOpen || isDragOver) && (
                    <motion.div
                        initial={{ opacity: 0, y: 5 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="flex items-center gap-1.5"
                    >
                        {isDragOver ? (
                            <span className="text-[10px] font-black tracking-widest uppercase text-white animate-pulse">
                                DROP TO ADD
                            </span>
                        ) : (
                            <>
                                <span className={cn("text-[10px] font-black tracking-widest uppercase", colorClass)}>
                                    {type}
                                </span>
                                {count > 0 && (
                                    <span className="px-1.5 py-0.5 bg-black/40 rounded text-[9px] font-mono text-white/80">
                                        {count}
                                    </span>
                                )}
                            </>
                        )}
                    </motion.div>
                )}
            </div>

            {/* Active Indicator Line (when closed) */}
            {!isOpen && !isDragOver && count > 0 && (
                <div className={cn("absolute -bottom-1 left-1/2 -translate-x-1/2 w-4 h-0.5 rounded-full", colorClass.replace('text-', 'bg-'))} />
            )}
        </button>
    );
};

const InvaderItem = ({ item, onDelete }: { item: InstalledInvader, onDelete: () => void }) => {
    const def = INVADER_LIBRARY.find(d => d.id === item.defId);
    if (!def) return null;

    const Icon = def.icon;

    return (
        <motion.div
            layout
            initial={{ opacity: 0, x: -10 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, scale: 0.9 }}
            className="flex items-center gap-3 p-2 bg-zinc-900/50 border border-zinc-800 rounded group hover:bg-zinc-800 hover:border-zinc-700 transition-colors cursor-pointer"
        >
            <div className={cn("p-1.5 rounded bg-zinc-950 border border-zinc-800", def.color)}>
                <Icon size={14} />
            </div>

            <div className="flex-grow min-w-0">
                <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-zinc-300">{def.name}</span>
                    <span className="text-[9px] text-zinc-600 font-mono uppercase px-1 border border-zinc-800 rounded">{def.id}</span>
                </div>
                <div className="text-[10px] text-zinc-500 truncate">{def.description}</div>
            </div>

            <button
                onClick={(e) => { e.stopPropagation(); onDelete(); }}
                className="opacity-0 group-hover:opacity-100 p-1 hover:bg-red-500/20 hover:text-red-400 rounded transition-all"
            >
                <X size={12} />
            </button>
        </motion.div>
    );
};

export const DesignInvader = () => {
    const [openSlot, setOpenSlot] = useState<InvaderType | null>(null);
    const [searchTerm, setSearchTerm] = useState('');

    const [invaders, setInvaders] = useState<{
        TRIGGER: InstalledInvader[];
        LOGIC: InstalledInvader[];
        OUTPUT: InstalledInvader[];
    }>({
        TRIGGER: [
            { id: '1', defId: 'http_listener', config: {} }
        ],
        LOGIC: [
            { id: '2', defId: 'json_parser', config: {} },
            { id: '3', defId: 'transform', config: {} }
        ],
        OUTPUT: []
    });

    const addInvader = (type: InvaderType, defId: string) => {
        const newInvader: InstalledInvader = {
            id: Date.now().toString(),
            defId,
            config: {}
        };
        setInvaders(prev => ({
            ...prev,
            [type]: [...prev[type], newInvader]
        }));

        // Auto open the slot to show the added item
        if (openSlot !== type) setOpenSlot(type);
    };

    const removeInvader = (type: InvaderType, id: string) => {
        setInvaders(prev => ({
            ...prev,
            [type]: prev[type].filter(i => i.id !== id)
        }));
    };

    const filteredLibrary = INVADER_LIBRARY.filter(i =>
        i.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        i.type.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="flex h-screen w-full bg-[#09090B] overflow-hidden">

            {/* TOOLBOX SIDEBAR */}
            <div className="w-64 border-r border-zinc-800 flex flex-col bg-zinc-900/50">
                <div className="p-4 border-b border-zinc-800">
                    <h2 className="text-sm font-bold text-zinc-400 mb-4 uppercase tracking-wider">Invader Toolbox</h2>
                    <div className="relative">
                        <Search className="absolute left-2 top-1.5 text-zinc-500" size={14} />
                        <input
                            type="text"
                            placeholder="Search modules..."
                            className="w-full bg-zinc-950 border border-zinc-800 rounded py-1.5 pl-8 pr-2 text-xs text-white focus:outline-none focus:border-blue-500/50"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                </div>

                <div className="flex-grow overflow-y-auto p-2 space-y-6 scrollbar-hide">

                    {['TRIGGER', 'LOGIC', 'OUTPUT'].map(type => {
                        const items = filteredLibrary.filter(i => i.type === type);
                        if (items.length === 0) return null;

                        return (
                            <div key={type} className="space-y-2">
                                <div className={cn(
                                    "px-2 text-[10px] font-bold uppercase tracking-wider",
                                    type === 'TRIGGER' ? "text-red-500" :
                                        type === 'LOGIC' ? "text-blue-500" : "text-emerald-500"
                                )}>
                                    {type} Modules
                                </div>
                                <div className="space-y-1">
                                    {items.map(def => (
                                        <ToolboxItem key={def.id} def={def} />
                                    ))}
                                </div>
                            </div>
                        );
                    })}

                </div>
                <div className="p-2 border-t border-zinc-800 text-[10px] text-zinc-600 text-center">
                    Drag items onto the Block slots
                </div>
            </div>

            {/* CANVAS AREA */}
            <div className="flex-grow flex items-center justify-center p-20 bg-[url('https://grainy-gradients.vercel.app/noise.svg')]">

                {/* BLOCK CONTAINER */}
                <div className="relative w-[450px]">

                    {/* EXPANSION SLOTS (Top) */}
                    <div className="flex gap-1 mb-0.5 px-4 items-end h-8">
                        {/* RED SLOT */}
                        <SlotHeader
                            type="Trigger"
                            colorClass="text-red-400"
                            bgClass="bg-red-500/10 border-t border-x border-red-500/30"
                            count={invaders.TRIGGER.length}
                            isOpen={openSlot === 'TRIGGER'}
                            onClick={() => setOpenSlot(openSlot === 'TRIGGER' ? null : 'TRIGGER')}
                            onDrop={(defId) => addInvader('TRIGGER', defId)}
                        />

                        {/* BLUE SLOT */}
                        <SlotHeader
                            type="Logic"
                            colorClass="text-blue-400"
                            bgClass="bg-blue-500/10 border-t border-x border-blue-500/30"
                            count={invaders.LOGIC.length}
                            isOpen={openSlot === 'LOGIC'}
                            onClick={() => setOpenSlot(openSlot === 'LOGIC' ? null : 'LOGIC')}
                            onDrop={(defId) => addInvader('LOGIC', defId)}
                        />

                        {/* GREEN SLOT */}
                        <SlotHeader
                            type="Output"
                            colorClass="text-emerald-400"
                            bgClass="bg-emerald-500/10 border-t border-x border-emerald-500/30"
                            count={invaders.OUTPUT.length}
                            isOpen={openSlot === 'OUTPUT'}
                            onClick={() => setOpenSlot(openSlot === 'OUTPUT' ? null : 'OUTPUT')}
                            onDrop={(defId) => addInvader('OUTPUT', defId)}
                        />
                    </div>

                    {/* MODDING PANEL (Expands when slot is open) */}
                    <AnimatePresence>
                        {openSlot && (
                            <motion.div
                                initial={{ height: 0, opacity: 0 }}
                                animate={{ height: 'auto', opacity: 1 }}
                                exit={{ height: 0, opacity: 0 }}
                                className="bg-zinc-900 border-x border-t border-zinc-700 overflow-hidden relative z-10"
                            >
                                <div className="p-3 space-y-2">
                                    {/* Header */}
                                    <div className="flex items-center justify-between">
                                        <span className="text-[10px] font-bold text-zinc-500 uppercase tracking-wider">
                                            {openSlot} Stack
                                        </span>
                                        <button className="text-zinc-500 hover:text-zinc-300">
                                            <Settings size={12} />
                                        </button>
                                    </div>

                                    {/* Stack List */}
                                    <div className="space-y-1">
                                        <AnimatePresence>
                                            {invaders[openSlot].map((invader) => (
                                                <InvaderItem
                                                    key={invader.id}
                                                    item={invader}
                                                    onDelete={() => removeInvader(openSlot, invader.id)}
                                                />
                                            ))}
                                            {invaders[openSlot].length === 0 && (
                                                <div className="py-4 text-center text-zinc-600 text-xs border border-dashed border-zinc-800 rounded">
                                                    Slot Empty. Drag {openSlot} mods here.
                                                </div>
                                            )}
                                        </AnimatePresence>
                                    </div>
                                </div>
                            </motion.div>
                        )}
                    </AnimatePresence>

                    {/* MAIN BLOCK BODY */}
                    <div className={cn(
                        "relative bg-zinc-950 border-2 rounded-xl p-6 shadow-2xl transition-colors duration-300",
                        openSlot === 'TRIGGER' ? "border-red-500/50 shadow-[0_0_30px_rgba(239,68,68,0.1)]" :
                            openSlot === 'LOGIC' ? "border-blue-500/50 shadow-[0_0_30px_rgba(59,130,246,0.1)]" :
                                openSlot === 'OUTPUT' ? "border-emerald-500/50 shadow-[0_0_30px_rgba(16,185,129,0.1)]" :
                                    "border-zinc-700"
                    )}>
                        {/* Header */}
                        <div className="flex items-center gap-3 mb-6">
                            <div className="w-10 h-10 rounded bg-gradient-to-br from-zinc-800 to-zinc-900 flex items-center justify-center border border-zinc-700 shadow-inner">
                                <Box className="text-zinc-400" size={20} />
                            </div>
                            <div>
                                <div className="text-sm font-bold text-zinc-100">User Signup Flow</div>
                                <div className="text-[10px] text-zinc-500 font-mono">ID: block_8291a</div>
                            </div>
                        </div>

                        {/* Content Placeholder */}
                        <div className="space-y-2">
                            <div className="h-2 bg-zinc-900 rounded w-3/4" />
                            <div className="h-2 bg-zinc-900 rounded w-1/2" />
                            <div className="h-2 bg-zinc-900 rounded w-2/3" />
                        </div>

                        {/* Footer / Status */}
                        <div className="mt-8 pt-4 border-t border-zinc-900 flex items-center justify-between">
                            <div className="flex items-center gap-1.5 px-2 py-1 rounded bg-green-500/10 border border-green-500/20 text-green-400 text-[10px] font-bold uppercase tracking-wider">
                                <Zap size={10} className="fill-current" />
                                Active
                            </div>
                            <div className="text-[10px] text-zinc-600">v1.2.0</div>
                        </div>
                    </div>

                </div>
            </div>
        </div>
    );
};
