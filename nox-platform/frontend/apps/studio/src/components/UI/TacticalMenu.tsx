import React from 'react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export interface TacticalAction {
  label: string;
  onClick: () => void;
  variant?: 'default' | 'danger';
}

interface TacticalMenuProps {
  top: number;
  left: number;
  onClose: () => void;
  actions: TacticalAction[];
  header?: string;
}

/**
 * Standardized Context Menu for the NOX tactical interface.
 * Implements monospaced typography, dark semi-transparent overlays, and hover states.
 */
export const TacticalMenu: React.FC<TacticalMenuProps> = ({ 
  top, 
  left, 
  onClose, 
  actions,
  header 
}) => {
  return (
    <div 
      className="fixed inset-0 z-[9999]" 
      onClick={onClose}
      onContextMenu={(e) => { e.preventDefault(); onClose(); }}
    >
      <div 
        style={{ top, left }}
        className="absolute min-w-[200px] bg-zinc-950/90 backdrop-blur-xl border border-white/10 rounded-xl shadow-[0_10px_40px_rgba(0,0,0,0.8)] overflow-hidden animate-in fade-in zoom-in-95 duration-150"
      >
        {header && (
          <div className="p-1 px-3 py-2 border-b border-white/5 bg-white/[0.02]">
            <span className="text-[8px] font-bold text-zinc-500 uppercase tracking-widest leading-none">
              {header}
            </span>
          </div>
        )}
        <div className="p-1">
          {actions.map((action, idx) => (
            <button
              key={idx}
              onClick={(e) => { 
                e.stopPropagation(); 
                action.onClick(); 
                onClose(); 
              }}
              className={cn(
                "w-full flex items-center gap-3 px-3 py-2.5 rounded-lg transition-all group",
                action.variant === 'danger' 
                  ? "hover:bg-red-500/10 text-zinc-400 hover:text-red-400" 
                  : "hover:bg-white/[0.05] text-zinc-400 hover:text-white"
              )}
            >
              <div className={cn(
                "w-1.5 h-1.5 rounded-full transition-colors",
                action.variant === 'danger' 
                  ? "bg-zinc-700 group-hover:bg-red-500" 
                  : "bg-zinc-700 group-hover:bg-purple-500"
              )} />
              <span className="text-[11px] font-medium tracking-wide">
                {action.label}
              </span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};
