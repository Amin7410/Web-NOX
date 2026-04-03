import { memo } from 'react';
import { Handle, Position } from 'reactflow';
import * as Lucide from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

const Icons = Lucide as any;

const InvaderIcon = ({ type }: { type: string }) => {
    switch (type) {
        case 'TRIGGER': return <Icons.Zap size={10} className="fill-current text-red-400" />;
        case 'LOGIC': return <Icons.Activity size={10} className="fill-current text-blue-400" />;
        case 'OUTPUT': return <Icons.Database size={10} className="fill-current text-emerald-400" />;
        default: return <Icons.Box size={10} />;
    }
};

export const BaseBlockNode = memo(({ data, selected }: any) => {
    const variant = data.visual?.variant || 'DEFAULT';
    const invaders = data.invaders || [];
    const isInvaderHovered = data.isInvaderHovered || false;
    const isSuccess = data.isSuccess || false;
    const isConnectingSource = data.isConnectingSource || false;

    return (
        <div className="relative group/node">
            {/* SUCCESS FLASH EFFECT */}
            <AnimatePresence>
                {isSuccess && (
                    <motion.div 
                        initial={{ opacity: 0.8, scale: 0.95 }}
                        animate={{ opacity: 0, scale: 1.15 }}
                        exit={{ opacity: 0 }}
                        className="absolute -inset-3 bg-indigo-500/30 rounded-sm z-[-1] blur-xl"
                        transition={{ duration: 0.8 }}
                    />
                )}
            </AnimatePresence>

            {/* CONNECTION SOURCE INDICATOR */}
            <AnimatePresence>
                {isConnectingSource && (
                    <motion.div 
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        className="absolute -inset-1 border-2 border-indigo-500 rounded-sm z-[-1] animate-pulse"
                    />
                )}
            </AnimatePresence>

            <div 
                className={`
                    w-64 min-h-[140px] bg-zinc-950 rounded-sm border-2 transition-all duration-300 flex flex-col overflow-hidden shadow-2xl
                    ${selected ? 'border-indigo-600 shadow-[0_0_20px_rgba(99,102,241,0.2)]' : 'border-zinc-800'}
                    ${isInvaderHovered ? 'border-white scale-[1.02] shadow-[0_0_25px_rgba(255,255,255,0.2)]' : ''}
                    ${isConnectingSource ? 'border-indigo-500 scale-[1.05]' : ''}
                `}
            >
                {/* Header Section */}
                <div className={`
                    p-3 flex items-center gap-3 border-b
                    ${variant === 'SLEEK' ? 'bg-gradient-to-r from-indigo-900/30 to-transparent border-indigo-500/20' : 
                      variant === 'RUGGED' ? 'bg-zinc-900 border-zinc-900' : 'bg-transparent border-zinc-900'}
                `}>
                    <div className="w-8 h-8 rounded-sm bg-zinc-900 border border-zinc-800 flex items-center justify-center shadow-inner overflow-hidden">
                         <div className="relative">
                            <Icons.Box size={16} className="text-zinc-500 group-hover/node:rotate-12 transition-transform" />
                            {isConnectingSource && <div className="absolute -top-1.5 -right-1.5 w-2 h-2 bg-indigo-500 rounded-full" />}
                         </div>
                    </div>
                    <div className="flex flex-col overflow-hidden">
                        <span className="text-xs font-black uppercase tracking-widest text-zinc-300 truncate">
                            {data.label || 'New Block'}
                        </span>
                        <div className="flex gap-1 mt-1 flex-wrap">
                            {invaders.map((inv: any, idx: number) => {
                                const type = typeof inv === 'string' ? inv : inv.type;
                                return (
                                    <div key={idx} className="p-1 rounded-[1px] bg-zinc-900 border border-zinc-800/50" title={type}>
                                        <InvaderIcon type={type} />
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>

                {/* Body Content */}
                <div className="p-4 flex-grow relative bg-[url('https://grainy-gradients.vercel.app/noise.svg')] bg-repeat opacity-90">
                    <p className="text-[10px] text-zinc-500 leading-relaxed font-medium">
                        {data.description || 'Chưa có mô tả kỹ thuật cho khối này.'}
                    </p>
                    
                    {/* Level Indicator (for drill-down feedback) */}
                    {data.depth !== undefined && (
                        <div className="absolute bottom-2 right-2 px-1.5 py-0.5 bg-zinc-900/50 border border-zinc-800 rounded-sm">
                             <span className="text-[8px] font-black text-zinc-600 uppercase">L{data.depth}</span>
                        </div>
                    )}
                </div>

                {/* Footer Section */}
                <div className="px-3 py-2 bg-zinc-900/30 border-t border-zinc-800/30 flex items-center justify-between">
                    <span className="text-[8px] font-black text-zinc-700 uppercase tracking-widest flex items-center gap-2">
                        <Icons.Activity size={8} className="text-zinc-800" />
                        Nox Platform
                    </span>
                    <div className="flex gap-1.5">
                         <div className="w-1.5 h-1.5 bg-indigo-500/20 rounded-full" />
                         <div className="w-1.5 h-1.5 bg-emerald-500/20 rounded-full" />
                    </div>
                </div>
            </div>
            
            {/* PORT SOCKETS (SIDE ONLY) */}
            {/* isConnectable={false} blocks ReactFlow's default drag-to-connect logic */}
            <div className="absolute left-[-2px] top-1/2 -translate-y-1/2 flex flex-col items-center group/socket">
                <div className="w-1 h-8 bg-zinc-800 border border-zinc-700 rounded-l-md shadow-xl transition-all group-hover/socket:bg-indigo-500 group-hover/socket:scale-y-110" />
                <Handle 
                    type="target" 
                    id="target"
                    position={Position.Left} 
                    className="!bg-transparent !border-none !w-4 !h-8 !left-[-8px] !top-1/2 !-translate-y-1/2" 
                    isConnectable={false}
                />
            </div>

            <div className="absolute right-[-2px] top-1/2 -translate-y-1/2 flex flex-col items-center group/socket">
                <div className="w-1 h-8 bg-zinc-800 border border-zinc-700 rounded-r-md shadow-xl transition-all group-hover/socket:bg-indigo-500 group-hover/socket:scale-y-110" />
                <Handle 
                    type="source" 
                    id="source"
                    position={Position.Right} 
                    className="!bg-transparent !border-none !w-4 !h-8 !right-[-8px] !top-1/2 !-translate-y-1/2" 
                    isConnectable={false}
                />
            </div>
        </div>
    );
});

export default BaseBlockNode;
