import React from 'react';
import { 
  Shield, 
  Save, 
  History, 
  Users, 
  Eye, 
  ChevronDown, 
  Share2,
  Search,
  Zap,
  MousePointer2
} from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { useStudio } from '../../context/StudioContext';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

const NavButton = ({ 
  icon: Icon, 
  label, 
  active = false, 
  onClick 
}: { 
  icon: any; 
  label?: string; 
  active?: boolean; 
  onClick?: () => void 
}) => (
  <button
    onClick={onClick}
    className={cn(
      "flex items-center gap-2 px-3 py-1.5 rounded-md transition-all duration-200 border border-transparent",
      active 
        ? "bg-indigo-500/10 text-indigo-400 border-indigo-500/30 shadow-[0_0_8px_rgba(99,102,241,0.2)]" 
        : "text-zinc-400 hover:bg-white/5 hover:text-zinc-100"
    )}
  >
    <Icon size={16} />
    {label && <span className="text-xs font-medium">{label}</span>}
  </button>
);

export const Header = () => {
  const { isConnectMode, setIsConnectMode } = useStudio();

  return (
    <header className="h-14 w-full flex items-center justify-between px-4 border-b border-white/20 bg-zinc-900/90 backdrop-blur-md relative z-50">
      {/* 1. Identity & Navigation (Left) */}
      <div className="flex items-center gap-6">
        <div className="flex items-center gap-3">
          <div className="flex h-8 w-8 items-center justify-center rounded bg-zinc-800 border border-white/10 group cursor-pointer hover:border-indigo-500/50 transition-colors">
            <Shield size={18} className="text-indigo-500 group-hover:scale-110 transition-transform" />
          </div>
          <div className="flex flex-col">
            <div className="flex items-center gap-2">
              <span className="text-sm font-bold tracking-tight text-zinc-100">NOX_STUDIO</span>
              <span className="px-1.5 py-0.5 rounded bg-indigo-500/10 text-[10px] font-bold text-indigo-400 border border-indigo-500/20">V1.0</span>
            </div>
            <div className="flex items-center gap-1.5 text-[10px] text-zinc-500 font-mono uppercase tracking-widest">
              <span className="hover:text-zinc-300 cursor-pointer">Workspace</span>
              <span>/</span>
              <span className="hover:text-zinc-300 cursor-pointer text-zinc-400">Core_Blueprint</span>
            </div>
          </div>
        </div>

        <div className="h-6 w-px bg-white/10" />

        <div className="flex items-center gap-1">
          <div className="h-2 w-2 rounded-full bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.5)] animate-pulse" />
          <span className="text-[10px] font-bold text-emerald-500 uppercase tracking-tighter">Live_Engine</span>
        </div>
      </div>

      {/* 2. Snapshot & Discovery (Center) */}
      <div className="absolute left-1/2 -translate-x-1/2 flex items-center gap-4">
        {/* Mode Switcher */}
        <div className="flex items-center gap-1 bg-zinc-950/80 border border-white/5 rounded-lg p-1 mr-4">
          <button 
            onClick={() => setIsConnectMode(false)}
            className={cn(
              "p-1.5 rounded transition-all duration-200",
              !isConnectMode ? "bg-zinc-800 text-zinc-100" : "text-zinc-500 hover:text-zinc-300"
            )}
            title="Navigation Mode"
          >
            <MousePointer2 size={14} />
          </button>
          <button 
            onClick={() => setIsConnectMode(true)}
            className={cn(
              "p-1.5 rounded transition-all duration-200",
              isConnectMode ? "bg-indigo-600 text-white shadow-[0_0_10px_rgba(79,70,229,0.4)]" : "text-zinc-500 hover:text-zinc-300"
            )}
            title="Linking Mode"
          >
            <Zap size={14} />
          </button>
        </div>

        {/* Search / Command Palette */}
        <div className="flex items-center gap-2 px-3 py-1.5 bg-zinc-950/50 border border-white/5 rounded-lg w-64 group focus-within:border-indigo-500/50 transition-all">
          <label htmlFor="global-search" className="text-zinc-500 group-focus-within:text-indigo-400 transition-colors">
            <Search size={14} />
          </label>
          <input 
            id="global-search"
            type="text" 
            placeholder="Search blocks..." 
            className="bg-transparent border-none outline-none text-xs text-zinc-300 placeholder:text-zinc-600 w-full"
          />
        </div>

        <div className="flex items-center gap-2 bg-zinc-950/50 border border-white/5 rounded-lg p-1">
          <button className="flex items-center gap-2 px-4 py-1.5 rounded-md bg-indigo-600 hover:bg-indigo-500 text-white transition-all shadow-lg shadow-indigo-500/20 group">
            <Save size={14} className="group-hover:scale-110 transition-transform" />
            <span className="text-xs font-bold uppercase tracking-wider">Commit</span>
          </button>
        </div>
      </div>

      {/* 3. System Tools & Profile (Right) */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-1 mr-2">
          <NavButton icon={Users} />
          <NavButton icon={Share2} />
          <NavButton icon={Eye} label="Presentation" />
        </div>

        <div className="h-6 w-px bg-white/10" />

        <button className="flex h-8 w-8 items-center justify-center rounded-full bg-zinc-800 border border-white/10 hover:border-indigo-500/50 transition-colors overflow-hidden">
          <div className="h-full w-full bg-gradient-to-br from-zinc-700 to-zinc-900 flex items-center justify-center text-[10px] font-bold text-zinc-400">
            ADM
          </div>
        </button>
      </div>
    </header>
  );
};
