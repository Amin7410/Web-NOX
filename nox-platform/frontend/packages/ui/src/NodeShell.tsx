import { memo, useState } from 'react';
import { Handle, Position, NodeProps } from 'reactflow';
import { Pencil } from 'lucide-react';

export const NodeShell = memo(({ data, selected }: NodeProps) => {
    const [isEditing, setIsEditing] = useState(false);
    const [title, setTitle] = useState(data.label || 'Node');

    return (
        <div
            className={`relative min-w-[200px] rounded-lg border backdrop-blur-md transition-all duration-200 group
        ${selected
                    ? 'border-blue-500 bg-zinc-900/80 shadow-[0_0_20px_-5px_rgba(59,130,246,0.5)]'
                    : 'border-white/10 bg-zinc-900/50 hover:border-white/20'
                }
      `}
        >
            {/* Header / Title Area */}
            <div className={`px-4 py-2 border-b border-white/5 flex items-center justify-between rounded-t-lg
          ${data.type === 'start' ? 'bg-green-500/10' : ''}
          ${data.type === 'process' ? 'bg-blue-500/10' : ''}
          ${data.type === 'end' ? 'bg-red-500/10' : ''}
      `}>
                {isEditing ? (
                    <input
                        autoFocus
                        className="bg-transparent border-none text-sm font-semibold text-white focus:outline-none w-full p-0"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        onBlur={() => {
                            setIsEditing(false);
                            data.label = title; // Update data ref (simple way)
                        }}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter') setIsEditing(false);
                        }}
                    />
                ) : (
                    <span
                        className="text-sm font-semibold text-zinc-100 cursor-text flex-1 truncate"
                        onDoubleClick={() => setIsEditing(true)}
                    >
                        {title}
                    </span>
                )}

                {/* Edit Icon Hint (visible on hover) */}
                <button
                    onClick={() => setIsEditing(true)}
                    className="opacity-0 group-hover:opacity-50 hover:!opacity-100 transition-opacity ml-2"
                >
                    <Pencil size={12} className="text-zinc-400" />
                </button>
            </div>

            {/* Content Area */}
            <div className="p-4">
                <p className="text-xs text-zinc-400">
                    {data.description || 'No description provided.'}
                </p>
            </div>

            {/* Handles */}
            {data.type !== 'start' && (
                <Handle
                    type="target"
                    position={Position.Top}
                    className="!w-3 !h-3 !bg-zinc-400 !border-2 !border-zinc-900 transition-colors hover:!bg-blue-500"
                />
            )}
            {data.type !== 'end' && (
                <Handle
                    type="source"
                    position={Position.Bottom}
                    className="!w-3 !h-3 !bg-zinc-400 !border-2 !border-zinc-900 transition-colors hover:!bg-blue-500"
                />
            )}
        </div>
    );
});

NodeShell.displayName = 'NodeShell';
