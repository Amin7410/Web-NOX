import React, { useState, useCallback } from 'react';
import { TabId } from './MainRail';
import { Shapes, Settings2, Network, Zap, Palette } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { useStudio } from '../../context/StudioContext';
import { TacticalMenu } from '../UI/TacticalMenu';
import { BlocksTab } from './Tabs/BlocksTab';
import { InvaderLibraryTab } from './Tabs/InvaderLibraryTab';
import { RelationsTab } from './Tabs/RelationsTab';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

interface SidebarPanelProps {
  activeTab: TabId | null;
  width?: number;
  onResizeStart?: (e: React.MouseEvent) => void;
}

/**
 * Main Orchestrator for the Sidebar.
 * Manages Tab switching and Context Menu state for all sub-modules.
 */
export const SidebarPanel = ({ activeTab, width = 300, onResizeStart }: SidebarPanelProps) => {
  const { 
    removeSavedBlock, 
    edgeColor, 
    setEdgeColor, 
    removeSavedInvader,
    activeSoulNodeId,
    spawnInvaderFromLibrary
  } = useStudio();

  const [menu, setMenu] = useState<{ top: number, left: number, blockId?: string, invaderId?: string } | null>(null);
  
  const handleContextMenu = useCallback((e: React.MouseEvent, id?: string, type: 'block' | 'invader' = 'block') => {
    e.preventDefault();
    e.stopPropagation();
    if (type === 'block') {
      setMenu({ top: e.clientY, left: e.clientX, blockId: id });
    } else {
      setMenu({ top: e.clientY, left: e.clientX, invaderId: id });
    }
  }, []);

  const closeMenu = useCallback(() => setMenu(null), []);

  if (!activeTab) return null;

  const colors = [
    'rgba(99, 102, 241, 0.6)', 
    'rgba(16, 185, 129, 0.6)', 
    'rgba(245, 158, 11, 0.6)', 
    'rgba(239, 68, 68, 0.6)',  
    'rgba(6, 182, 212, 0.6)',  
    'rgba(168, 85, 247, 0.6)', 
  ];

  const renderContent = () => {
    switch (activeTab) {
      case 'blocks':
        return <BlocksTab onContextMenu={handleContextMenu} />;
      case 'invaders':
        return <InvaderLibraryTab onContextMenu={handleContextMenu} />;
      case 'relations':
        return <RelationsTab onContextMenu={handleContextMenu} />;
      case 'settings':
        return (
          <div className="flex flex-col gap-6 animate-in fade-in duration-500">
            <div className="flex flex-col gap-4">
              <div className="flex items-center gap-2">
                <Palette size={14} className="text-indigo-400" />
                <span className="text-[10px] font-bold text-zinc-400 uppercase tracking-widest">Architectural Palette</span>
              </div>
              <div className="p-4 rounded-lg bg-zinc-950/50 border border-white/5 flex flex-col gap-4">
                <span className="text-xs text-zinc-500">Default Edge Color</span>
                <div className="flex flex-wrap gap-2">
                  {colors.map((color) => (
                    <button
                      key={color}
                      onClick={() => setEdgeColor(color)}
                      className={cn(
                        "w-8 h-8 rounded-md border-2 transition-all",
                        edgeColor === color ? "border-white scale-110 shadow-lg" : "border-transparent hover:scale-105"
                      )}
                      style={{ backgroundColor: color }}
                    />
                  ))}
                </div>
              </div>
            </div>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div 
      className="flex h-full flex-col border-r border-white/20 bg-zinc-900 overflow-visible relative z-10 shadow-2xl"
      style={{ width: `${width}px` }}
      onClick={closeMenu}
    >
      <div className="p-4 border-b border-white/10 bg-zinc-900/50 backdrop-blur-sm shrink-0">
        <h2 className="text-sm font-bold uppercase tracking-widest text-zinc-400 flex items-center gap-2">
           {activeTab === 'blocks' && <Shapes size={16} />}
           {activeTab === 'invaders' && <Zap size={16} />}
           {activeTab === 'relations' && <Network size={16} />}
           {activeTab === 'settings' && <Settings2 size={16} />}
           {activeTab}
        </h2>
      </div>

      <div className="flex-1 overflow-y-auto custom-scrollbar p-4">
        {renderContent()}
      </div>

      <div 
        className="absolute top-0 right-0 w-2 h-full cursor-col-resize hover:bg-indigo-500/20 transition-colors z-[100]" 
        onMouseDown={onResizeStart}
      />

      {menu && (
        <TacticalMenu 
          top={menu.top} 
          left={menu.left} 
          onClose={closeMenu}
          header={menu.invaderId ? `Invader Instance [Captured]` : undefined}
          actions={menu.invaderId ? [
            ...(activeSoulNodeId ? [{ 
              label: 'Inject into Active Block', 
              onClick: () => spawnInvaderFromLibrary(activeSoulNodeId, menu.invaderId!), 
              variant: 'default' as const 
            }] : []),
            { label: 'Delete from Library', onClick: () => removeSavedInvader(menu.invaderId!), variant: 'danger' }
          ] : (menu.blockId && !['junction-base', 'input-base', 'output-base'].includes(menu.blockId) ? [
            { label: 'Configure Template', onClick: () => console.log('Config template'), variant: 'default' },
            { label: 'Remove from Library', onClick: () => removeSavedBlock(menu.blockId!), variant: 'danger' }
          ] : [
            { label: 'Entity Information', onClick: () => console.log('Info'), variant: 'default' },
            { label: 'Set as Default', onClick: () => console.log('Set default'), variant: 'default' }
          ])}
        />
      )}
    </div>
  );
};
