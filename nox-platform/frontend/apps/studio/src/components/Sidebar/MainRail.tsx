import React from 'react';
import { Boxes, Settings, LucideIcon, Network } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export type TabId = 'blocks' | 'relations' | 'settings';

interface RailItemProps {
  icon: LucideIcon;
  label: string;
  active: boolean;
  onClick: () => void;
}

const RailItem = ({ icon: Icon, label, active, onClick }: RailItemProps) => (
  <button
    onClick={onClick}
    className={cn(
      "group relative flex h-12 w-12 shrink-0 items-center justify-center rounded-xl transition-all duration-200",
      active 
        ? "bg-indigo-500/10 text-indigo-400 shadow-[inset_0_0_0_1px_rgba(99,102,241,0.5)]" 
        : "text-zinc-500 hover:bg-zinc-800 hover:text-zinc-200"
    )}
    title={label}
  >
    <Icon size={20} strokeWidth={active ? 2.5 : 2} />
    {active && (
      <div className="absolute -left-1 h-6 w-1 rounded-r-full bg-indigo-500 shadow-[0_0_8px_rgba(99,102,241,0.5)]" />
    )}
  </button>
);

interface MainRailProps {
  activeTab: TabId | null;
  onTabChange: (tab: TabId) => void;
}

export const MainRail = ({ activeTab, onTabChange }: MainRailProps) => {
  return (
    <div className="flex h-full w-[64px] flex-col items-center gap-4 border-r border-white/20 bg-zinc-900 py-4 custom-scrollbar overflow-y-auto overflow-x-hidden relative z-20">
      {/* Brand Logo */}
      <div className="mb-4 flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-indigo-600 shadow-lg shadow-indigo-500/20 border border-indigo-400/30">
        <span className="text-xl font-bold text-white">N</span>
      </div>

      {/* Main Tools */}
      <div className="flex flex-1 flex-col items-center gap-3">
        <RailItem 
          icon={Boxes} 
          label="Blocks" 
          active={activeTab === 'blocks'} 
          onClick={() => onTabChange('blocks')}
        />
        <RailItem 
          icon={Network} 
          label="Relations" 
          active={activeTab === 'relations'} 
          onClick={() => onTabChange('relations')}
        />
      </div>

      {/* Footer Tools */}
      <div className="mt-auto pt-4 border-t border-white/10">
        <RailItem 
          icon={Settings} 
          label="Settings" 
          active={activeTab === 'settings'} 
          onClick={() => onTabChange('settings')}
        />
      </div>
    </div>
  );
};
