import React from 'react';
import { Box } from 'lucide-react';

export const Sidebar = () => {
    const onDragStart = (event: React.DragEvent, nodeType: string) => {
        event.dataTransfer.setData('application/reactflow', nodeType);
        event.dataTransfer.effectAllowed = 'move';
    };

    return (
        <aside className="w-64 border-r border-white/10 bg-zinc-900/50 backdrop-blur-sm p-4 flex flex-col gap-4">
            <div className="mb-4">
                <h2 className="text-sm font-semibold text-zinc-400 mb-1">Toolbox</h2>
                <p className="text-xs text-zinc-500">Drag blocks to the canvas</p>
            </div>

            <div
                className="flex items-center gap-3 p-3 rounded-lg border border-white/5 bg-zinc-800/50 cursor-grab hover:bg-zinc-800 transition-colors"
                onDragStart={(event) => onDragStart(event, 'noxBlock')}
                draggable
            >
                <div className="p-2 rounded-md bg-blue-500/10 text-blue-400">
                    <Box size={16} />
                </div>
                <div className="flex flex-col">
                    <span className="text-sm font-medium text-zinc-200">Nox Block</span>
                    <span className="text-[10px] text-zinc-500">Nested Entity</span>
                </div>
            </div>
        </aside>
    );
};
