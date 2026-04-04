import React from 'react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export interface DraggableItemProps {
  id?: string;
  type: string;
  label: string;
  icon: any; // Lucide Icon component
  isDefined?: boolean;
  isCustom?: boolean;
  category?: 'block' | 'relation' | 'terminal' | 'invader';
  onContextMenu?: (e: React.MouseEvent) => void;
  colorScheme?: 'indigo' | 'emerald' | 'amber' | 'purple';
}

/**
 * Modular DraggableItem for Sidebar resource management.
 * Handles ReactFlow internal data transfer and technical aesthetic rendering.
 */
export const DraggableItem: React.FC<DraggableItemProps> = ({ 
  id,
  type, 
  label, 
  icon: Icon, 
  isDefined = false,
  isCustom = false,
  category = 'block',
  onContextMenu,
  colorScheme = 'indigo'
}) => {
  const onDragStart = (event: React.DragEvent) => {
    let rfType = 'noxNode';
    if (category === 'relation') rfType = 'noxJunction';
    if (category === 'terminal') {
      rfType = type === 'inputTerminal' ? 'noxInputTerminal' : 'noxOutputTerminal';
    }
    if (category === 'invader' && type === 'hub-base') {
      rfType = 'noxInvaderHub';
    }
    
    event.dataTransfer.setData('application/reactflow', rfType);
    event.dataTransfer.setData('application/nox-type', type);
    event.dataTransfer.setData('application/nox-id', id || '');
    event.dataTransfer.setData('application/nox-defined', isDefined.toString());
    event.dataTransfer.setData('application/nox-custom', isCustom.toString());
    event.dataTransfer.setData('text/plain', label);
    event.dataTransfer.effectAllowed = 'move';
  };

  const schemeClasses = {
    indigo: "border-indigo-500/40 bg-indigo-500/5 hover:border-indigo-400 text-indigo-400",
    emerald: "border-emerald-500/40 bg-emerald-500/5 hover:border-emerald-400 text-emerald-400",
    amber: "border-amber-500/40 bg-amber-500/5 hover:border-amber-400 text-amber-400",
    purple: "border-purple-500/40 bg-purple-500/5 hover:border-purple-400 text-purple-400"
  };

  return (
    <div
      className={cn(
        "group flex items-center gap-3 transition-all duration-200 active:cursor-grabbing",
        category === 'invader' 
          ? "px-3 py-2 rounded-md border border-purple-500/20 bg-zinc-950/30 hover:border-purple-500/50 hover:bg-purple-500/5 cursor-grab"
          : "p-3 rounded-lg border bg-zinc-950/50 cursor-grab hover:scale-[1.02]",
        category !== 'block' && category !== 'invader' ? schemeClasses[colorScheme] :
        category === 'block' ? (isCustom 
          ? "border-emerald-500/20 bg-emerald-500/5 hover:border-emerald-500/50 text-emerald-400"
          : (isDefined 
              ? "border-white/10 hover:border-indigo-500/50 shadow-[0_0_0_1px_rgba(255,255,255,0.02)] text-indigo-400" 
              : "border-dashed border-white/5 hover:border-zinc-500/50")) : ""
      )}
      onDragStart={onDragStart}
      onContextMenu={onContextMenu}
      draggable
    >
      <div className={cn(
        "p-1.5 rounded transition-colors",
        category === 'invader' 
          ? "bg-purple-500/10 text-purple-400 border border-purple-500/20"
          : (isDefined || isCustom || category !== 'block') ? "bg-zinc-900 border border-current/20" : "bg-zinc-900 border border-white/5 text-zinc-500"
      )}>
        <Icon size={category === 'invader' ? 14 : 18} />
      </div>
      <div className="flex flex-col">
        <span className={cn(
          "font-bold truncate max-w-[150px] tracking-tight",
          category === 'invader' ? "text-[11px] text-purple-200" : "text-xs text-zinc-200"
        )}>{label}</span>
        <span className="text-[10px] text-zinc-500 uppercase font-mono tracking-tighter leading-none mt-0.5">
          {category === 'relation' ? 'Architectural Relay' : 
           (category === 'terminal' ? 'Layer Bridge' : 
           (category === 'invader' ? (isDefined ? 'Soul Gateway [Canvas]' : 'Soul Fragment [Internal]') :
           (isCustom ? 'Saved Library' : (isDefined ? 'Module Template' : 'Conceptual'))))}
        </span>
      </div>
    </div>
  );
};
