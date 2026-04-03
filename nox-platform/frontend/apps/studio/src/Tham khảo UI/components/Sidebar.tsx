import React from 'react';
import * as Lucide from 'lucide-react';

const Icons = Lucide as any;

const NODE_TYPES = [
    { type: 'text', icon: Icons.Type, label: 'Text Block', color: 'text-sky-400' },
    { type: 'image', icon: Icons.Image, label: 'Media Node', color: 'text-amber-400' },
    { type: 'container', icon: Icons.LayoutPanelTop, label: 'Section', color: 'text-indigo-400' },
    { type: 'database', icon: Icons.Database, label: 'Data Source', color: 'text-emerald-400' },
    { type: 'router', icon: Icons.CircleDot, label: 'Router Node', color: 'text-white' }
];

const INVADER_TYPES = [
    { type: 'TRIGGER', label: 'HTTP Webhook', color: 'text-red-400', icon: Icons.Zap },
    { type: 'LOGIC', label: 'Script Handler', color: 'text-blue-400', icon: Icons.Cpu },
    { type: 'OUTPUT', label: 'Database Save', color: 'text-emerald-400', icon: Icons.Database }
];

export default function Sidebar() {
    const handleDragStart = (e: React.DragEvent, type: string, category: string) => {
        console.log('Drag started:', { type, category });
        
        // Gán dữ liệu kéo thả trực tiếp bằng string literal để tránh lỗi
        e.dataTransfer.setData('application/' + category, type);
        e.dataTransfer.effectAllowed = category === 'invader' ? 'copy' : 'move';
        
        console.log('Data set:', {
            ['application/' + category]: type,
            'effectAllowed': e.dataTransfer.effectAllowed
        });

        // Feedback thị giác khi cầm nắm (Ghost image)
        const ghost = document.createElement('div');
        ghost.style.cssText = "position:absolute; top:-1000px; padding:8px; background:#18181b; border:1px solid #27272a; color:#fff; font-size:10px; font-weight:bold; border-radius:2px;";
        ghost.textContent = category.toUpperCase() + ": " + type;
        document.body.appendChild(ghost);
        e.dataTransfer.setDragImage(ghost, 0, 0);
        setTimeout(() => document.body.removeChild(ghost), 0);
    };

    return (
        <aside className="w-64 h-full border-r border-zinc-800/50 bg-zinc-950/80 backdrop-blur-md flex flex-col p-4 z-40">
            <h2 className="text-[10px] uppercase tracking-[0.2em] text-zinc-600 font-black mb-6 px-2">Components</h2>
            
            <div className="flex flex-col gap-2 mb-8">
                {NODE_TYPES.map((node) => {
                    const Icon = node.icon;
                    return (
                        <div
                            key={node.type}
                            onDragStart={(e) => handleDragStart(e, node.type, 'reactflow')}
                            draggable
                            className="flex items-center gap-3 p-2.5 rounded-sm cursor-grab active:cursor-grabbing hover:bg-zinc-800/40 transition-all group border border-transparent hover:border-zinc-800"
                        >
                            <div className="p-2 rounded-sm bg-zinc-900 border border-zinc-800 group-hover:border-zinc-700">
                                {Icon && <Icon className={`w-4 h-4 ${node.color}`} />}
                            </div>
                            <span className="text-xs font-bold text-zinc-400 group-hover:text-slate-200 tracking-wide font-mono">{node.label}</span>
                        </div>
                    );
                })}
            </div>

            <h2 className="text-[10px] uppercase tracking-[0.2em] text-zinc-600 font-black mb-4 px-2">Invaders</h2>
            <div className="flex flex-col gap-2">
                {INVADER_TYPES.map((inv) => (
                    <div
                        key={inv.type}
                        onDragStart={(e) => handleDragStart(e, inv.type, 'invader')}
                        draggable
                        className="flex items-center gap-3 p-2.5 rounded-sm cursor-grab active:cursor-grabbing hover:bg-zinc-800/40 transition-all group border border-transparent hover:border-zinc-800"
                    >
                        <div className={`p-2 rounded-sm bg-zinc-900 border border-zinc-800 group-hover:border-zinc-700 ${inv.color}`}>
                            {inv.icon && <inv.icon size={16} />}
                        </div>
                        <span className="text-xs font-bold text-zinc-400 group-hover:text-slate-200 tracking-wide font-mono">{inv.label}</span>
                    </div>
                ))}
            </div>

            <div className="mt-auto pt-4 border-t border-zinc-800/50">
                <div className="p-4 bg-zinc-900/40 border border-zinc-800/50 rounded-sm">
                    <p className="text-[9px] uppercase tracking-widest text-zinc-600 font-bold text-center">
                        NOX Studio Operating
                    </p>
                </div>
            </div>
        </aside>
    );
}
