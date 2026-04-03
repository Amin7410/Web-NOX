import React, { useState, useCallback, useMemo } from 'react';
import { TabId } from './MainRail';
import { BoxSelect, Shapes, Bookmark, Palette, Settings2, GitCommit, Network, LogIn, LogOut } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { useStudio } from '../../context/StudioContext';
import { ContextMenu } from '../UI/ContextMenu';
import { NoxNodeType } from '../../types/studio';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

interface SidebarPanelProps {
  activeTab: TabId | null;
  width?: number;
  onResizeStart?: (e: React.MouseEvent) => void;
}

const DraggableItem = ({ 
  type, 
  label, 
  icon: Icon, 
  isDefined = false,
  isCustom = false,
  category = 'block',
  onContextMenu,
  colorScheme = 'indigo'
}: { 
  type: string, 
  label: string, 
  icon: any, 
  isDefined?: boolean,
  isCustom?: boolean,
  category?: 'block' | 'relation' | 'terminal',
  onContextMenu?: (e: React.MouseEvent) => void,
  colorScheme?: 'indigo' | 'emerald' | 'amber'
}) => {
  const onDragStart = (event: React.DragEvent) => {
    // Determine ReactFlow type mapping
    let rfType = 'noxNode';
    if (category === 'relation') rfType = 'noxJunction';
    if (category === 'terminal') {
      rfType = type === 'inputTerminal' ? 'noxInputTerminal' : 'noxOutputTerminal';
    }
    
    event.dataTransfer.setData('application/reactflow', rfType);
    event.dataTransfer.setData('application/nox-type', type);
    event.dataTransfer.setData('application/nox-defined', isDefined.toString());
    event.dataTransfer.setData('application/nox-custom', isCustom.toString());
    event.dataTransfer.setData('text/plain', label);
    event.dataTransfer.effectAllowed = 'move';
  };

  const schemeClasses = {
    indigo: "border-indigo-500/40 bg-indigo-500/5 hover:border-indigo-400 text-indigo-400",
    emerald: "border-emerald-500/40 bg-emerald-500/5 hover:border-emerald-400 text-emerald-400",
    amber: "border-amber-500/40 bg-amber-500/5 hover:border-amber-400 text-amber-400"
  };

  return (
    <div
      className={cn(
        "group flex items-center gap-3 p-3 rounded-lg border bg-zinc-950/50 cursor-grab active:cursor-grabbing transition-all duration-200 hover:scale-[1.02]",
        category !== 'block' ? schemeClasses[colorScheme] :
        (isCustom 
          ? "border-emerald-500/20 bg-emerald-500/5 hover:border-emerald-500/50 text-emerald-400"
          : (isDefined 
              ? "border-white/10 hover:border-indigo-500/50 shadow-[0_0_0_1px_rgba(255,255,255,0.02)] text-indigo-400" 
              : "border-dashed border-white/5 hover:border-zinc-500/50"))
      )}
      onDragStart={onDragStart}
      onContextMenu={onContextMenu}
      draggable
    >
      <div className={cn(
        "p-2 rounded bg-zinc-900 border",
        (isDefined || isCustom || category !== 'block') ? "border-current/20" : "border-white/5 text-zinc-500"
      )}>
        <Icon size={18} />
      </div>
      <div className="flex flex-col">
        <span className="text-xs font-bold text-zinc-200 tracking-tight truncate max-w-[150px]">{label}</span>
        <span className="text-[10px] text-zinc-500 uppercase font-mono tracking-tighter">
          {category === 'relation' ? 'Architectural Relay' : 
           (category === 'terminal' ? 'Layer Bridge' : 
           (isCustom ? 'Saved Library' : (isDefined ? 'Module Template' : 'Conceptual')))}
        </span>
      </div>
    </div>
  );
};

export const SidebarPanel = ({ activeTab, width = 300, onResizeStart }: SidebarPanelProps) => {
  const { savedBlocks, removeSavedBlock, edgeColor, setEdgeColor } = useStudio();
  const [menu, setMenu] = useState<{ top: number, left: number, blockId?: string } | null>(null);
  
  const handleContextMenu = useCallback((e: React.MouseEvent, blockId?: string) => {
    e.preventDefault();
    e.stopPropagation();
    setMenu({ top: e.clientY, left: e.clientX, blockId });
  }, []);

  const closeMenu = useCallback(() => setMenu(null), []);

  // Categorize Saved Items to keep workspace organized
  const categorizedSaved = useMemo(() => {
    const relationTypes: NoxNodeType[] = ['junction', 'inputTerminal', 'outputTerminal'];
    return {
      blocks: savedBlocks.filter(b => !relationTypes.includes(b.type)),
      relations: savedBlocks.filter(b => relationTypes.includes(b.type))
    };
  }, [savedBlocks]);

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
        return (
          <div className="flex flex-col gap-6">
            <div className="flex flex-col gap-3">
              <span className="text-[10px] font-bold text-zinc-600 uppercase tracking-widest mb-1">Base Modules</span>
              <DraggableItem 
                type="undefined" 
                label="Conceptual Block" 
                icon={BoxSelect} 
                onContextMenu={(e) => handleContextMenu(e)}
              />
              <DraggableItem 
                type="ui" 
                label="Logical Module" 
                icon={Shapes} 
                isDefined={true} 
                onContextMenu={(e) => handleContextMenu(e)}
              />
            </div>
            {categorizedSaved.blocks.length > 0 && (
              <div className="flex flex-col gap-3">
                <span className="text-[10px] font-bold text-emerald-500/50 uppercase tracking-widest mb-1">Saved Blocks</span>
                {categorizedSaved.blocks.map((block) => (
                  <DraggableItem 
                    key={block.id} 
                    type={block.type} 
                    label={block.label} 
                    icon={Bookmark} 
                    isDefined={true} 
                    isCustom={true} 
                    onContextMenu={(e) => handleContextMenu(e, block.id)}
                  />
                ))}
              </div>
            )}
          </div>
        );
      case 'relations':
        return (
          <div className="flex flex-col gap-6 animate-in slide-in-from-left-2 duration-300">
             <div className="flex flex-col gap-3">
                <div className="flex items-center gap-2 mb-2 p-2 rounded bg-indigo-500/5 border border-indigo-500/10">
                   <Network size={14} className="text-indigo-400" />
                   <span className="text-[10px] font-bold text-zinc-400 uppercase tracking-widest">Wiring Architecture</span>
                </div>
                <DraggableItem 
                  type="junction" 
                  label="Junction Point" 
                  icon={GitCommit} 
                  category="relation"
                  isDefined={true}
                  onContextMenu={(e) => handleContextMenu(e, 'junction-base')}
                />
                <div className="grid grid-cols-2 gap-2 mt-2">
                   <DraggableItem 
                    type="inputTerminal" 
                    label="Input Port" 
                    icon={LogIn} 
                    category="terminal"
                    colorScheme="emerald"
                    isDefined={true}
                    onContextMenu={(e) => handleContextMenu(e, 'input-base')}
                   />
                   <DraggableItem 
                    type="outputTerminal" 
                    label="Output Port" 
                    icon={LogOut} 
                    category="terminal"
                    colorScheme="amber"
                    isDefined={true}
                    onContextMenu={(e) => handleContextMenu(e, 'output-base')}
                   />
                </div>
             </div>
             
             {categorizedSaved.relations.length > 0 && (
              <div className="flex flex-col gap-3 mt-4 animate-in fade-in duration-500">
                <span className="text-[10px] font-bold text-indigo-500/50 uppercase tracking-widest mb-1">Custom Relays</span>
                {categorizedSaved.relations.map((rel) => (
                  <DraggableItem 
                    key={rel.id} 
                    type={rel.type} 
                    label={rel.label} 
                    icon={rel.type === 'junction' ? GitCommit : (rel.type === 'inputTerminal' ? LogIn : LogOut)} 
                    category={rel.type === 'junction' ? 'relation' : 'terminal'}
                    colorScheme={rel.type === 'inputTerminal' ? 'emerald' : (rel.type === 'outputTerminal' ? 'amber' : 'indigo')}
                    isDefined={true}
                    isCustom={true}
                    onContextMenu={(e) => handleContextMenu(e, rel.id)}
                  />
                ))}
              </div>
             )}

             <div className="mt-4 p-4 rounded-lg border border-dashed border-white/5 text-center">
                <p className="text-[9px] text-zinc-600 italic tracking-tight">
                  Drag terminals to establish bridge connections with exterior layers.
                </p>
             </div>
          </div>
        );
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
        <ContextMenu 
          top={menu.top} 
          left={menu.left} 
          onClose={closeMenu}
          actions={menu.blockId && !['junction-base', 'input-base', 'output-base'].includes(menu.blockId) ? [
            { label: 'Cấu hình mẫu', onClick: () => console.log('Config template'), variant: 'default' },
            { label: 'Xóa khỏi thư viện', onClick: () => removeSavedBlock(menu.blockId!), variant: 'danger' }
          ] : [
            { label: 'Thông tin vật thể', onClick: () => console.log('Info'), variant: 'default' },
            { label: 'Đặt làm mặc định', onClick: () => console.log('Set default'), variant: 'default' }
          ]}
        />
      )}
    </div>
  );
};
