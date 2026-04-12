import React, { useState, useCallback, useEffect, useRef } from 'react';
import { X, Plus, ShieldAlert, Fingerprint, GripVertical } from 'lucide-react';
import { useStudio } from '../../context/StudioContext';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { Reorder } from 'framer-motion';
import { TacticalMenu } from '../UI/TacticalMenu';
import { InvaderInstance } from '../../types/studio';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export const RightSidebar = () => {
  const { 
    isRightSidebarOpen, 
    toggleRightSidebar, 
    activeSoulNodeId, 
    nodes,
    addInvaderToNode,
    updateInvaderOrder,
    saveInvader,
    deleteInvaderFromNode
  } = useStudio();

  // Resize Logic
  const [width, setWidth] = useState(300);
  const isResizing = useRef(false);

  // DRAG PERFORMANCE FIX: Local state for smooth reordering
  const [localInvaders, setLocalInvaders] = useState<InvaderInstance[]>([]);
  
  // MINIMALIST CONTEXT MENU STATE
  const [menuState, setMenuState] = useState<{ x: number, y: number, id: string } | null>(null);

  const activeNode = nodes.find((n: { id: any; }) => n.id === activeSoulNodeId);
  const contextInvaders = activeNode?.data.invaders || [];

  // Initialize local state when node context changes
  useEffect(() => {
    if (activeSoulNodeId) {
      setLocalInvaders(contextInvaders);
    }
  }, [activeSoulNodeId, contextInvaders.length]);

  // DEBOUNCED SYNC: Push order to context only when user stops dragging for 100ms
  useEffect(() => {
    if (!activeSoulNodeId) return;
    
    // Check if data actually changed to avoid redundant updates
    const isDifferent = JSON.stringify(localInvaders) !== JSON.stringify(contextInvaders);
    if (!isDifferent) return;

    const timeout = setTimeout(() => {
      updateInvaderOrder(activeSoulNodeId, localInvaders);
    }, 100);

    return () => clearTimeout(timeout);
  }, [localInvaders, activeSoulNodeId]);

  // Global click listener to close menu
  useEffect(() => {
    const handleClick = () => setMenuState(null);
    window.addEventListener('click', handleClick);
    window.addEventListener('contextmenu', handleClick);
    return () => {
      window.removeEventListener('click', handleClick);
      window.removeEventListener('contextmenu', handleClick);
    };
  }, []);

  const startResizing = useCallback(() => {
    isResizing.current = true;
    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', stopResizing);
    document.body.style.cursor = 'col-resize';
  }, []);

  const stopResizing = useCallback(() => {
    isResizing.current = false;
    document.removeEventListener('mousemove', handleMouseMove);
    document.removeEventListener('mouseup', stopResizing);
    document.body.style.cursor = 'default';
  }, []);

  const handleMouseMove = useCallback((e: MouseEvent) => {
    if (!isResizing.current) return;
    const newWidth = window.innerWidth - e.clientX;
    if (newWidth >= 200 && newWidth <= 800) {
      setWidth(newWidth);
    }
  }, []);

  // Update only local state during reorder (Smooth)
  const handleReorder = (newOrder: InvaderInstance[]) => {
    setLocalInvaders(newOrder);
  };

  const openSettings = (e: React.MouseEvent, invaderId: string) => {
    e.preventDefault();
    e.stopPropagation();
    setMenuState({ x: e.clientX, y: e.clientY, id: invaderId });
  };

  const handleSaveToLibrary = () => {
     if (activeSoulNodeId && menuState) {
        saveInvader(activeSoulNodeId, menuState.id);
        setMenuState(null);
     }
  };

  const handleDeleteInvader = () => {
     if (activeSoulNodeId && menuState) {
        deleteInvaderFromNode(activeSoulNodeId, menuState.id);
        setMenuState(null);
     }
  };

  if (!isRightSidebarOpen) return null;

  return (
    <aside 
      style={{ width: `${width}px` }}
      className="h-full flex flex-col bg-[#0B0B0E] border-l border-white/5 relative z-20 shadow-[-10px_0_30px_rgba(0,0,0,0.5)] animate-in slide-in-from-right duration-300"
    >
      {/* 0. Resize Handle */}
      <div 
        onMouseDown={startResizing}
        className="absolute left-0 top-0 w-1 h-full cursor-col-resize hover:bg-purple-500/30 transition-colors z-30"
      />

      <div key="list-view" className="flex flex-col h-full animate-in fade-in duration-300">
        {/* 1. Header (Dynamic Block Name + Add) */}
        <div className="p-4 border-b border-white/5 flex items-center justify-between bg-black/20 min-h-[64px]">
          <div className="flex flex-col">
             <span className="text-[9px] font-bold text-zinc-600 uppercase tracking-[0.3em] leading-none mb-1">
               Invader Control Console
             </span>
             <h2 className="text-sm font-semibold text-white truncate max-w-[180px]">
               {activeNode?.data.label || 'Unknown Block'}
             </h2>
          </div>
          <div className="flex items-center gap-1">
            <button 
              onClick={() => activeSoulNodeId && addInvaderToNode(activeSoulNodeId)}
              className="p-1.5 rounded-md bg-purple-500/10 border border-purple-500/20 hover:bg-purple-500/20 text-purple-400 transition-all hover:scale-105 active:scale-95"
              title="Spawn New Invader"
            >
              <Plus size={16} />
            </button>
            <button 
              onClick={() => toggleRightSidebar(false)}
              className="p-1.5 rounded-md hover:bg-white/5 transition-colors text-zinc-500 hover:text-zinc-200"
            >
              <X size={16} />
            </button>
          </div>
        </div>

        {/* 2. Content Area (Tactical Invader List) */}
        <Reorder.Group 
          axis="y" 
          values={localInvaders} 
          onReorder={handleReorder}
          className="flex-1 overflow-y-auto custom-scrollbar p-3 space-y-2"
        >
          {localInvaders.length > 0 ? (
            localInvaders.map((invader) => (
              <Reorder.Item 
                key={invader.id}
                value={invader}
                onContextMenu={(e) => openSettings(e, invader.id)}
                className="group relative flex items-center gap-3 p-3 rounded bg-zinc-900 border border-white/5 hover:border-purple-500/30 transition-all cursor-grab active:cursor-grabbing overflow-hidden"
              >
                 {/* Drag Handle */}
                 <div className="text-zinc-700 group-hover:text-zinc-500 transition-colors">
                    <GripVertical size={14} />
                 </div>
 
                 {/* Status Indicator (Dynamic per Type) */}
                 <div className="relative flex items-center">
                    <div className={cn(
                      "w-1.5 h-1.5 rounded-full animate-pulse shadow-[0_0_8px_rgba(168,85,247,0.8)]",
                      invader.type === 'logic' ? "bg-indigo-500" : (invader.type === 'data' ? "bg-emerald-500" : "bg-purple-500")
                    )} />
                 </div>
 
                 {/* Tactical Identity Info */}
                 <div className="flex flex-col flex-1">
                    <div className="flex items-center justify-between">
                       <span className="text-[10px] font-mono font-black text-white/90 tracking-widest">{invader.name}</span>
                       <span className="text-[8px] font-mono text-zinc-600 uppercase tracking-tighter">{invader.type}</span>
                    </div>
                    <div className="flex items-center gap-2 mt-1">
                       <div className="h-[2px] flex-1 bg-zinc-800 rounded-full overflow-hidden">
                          <div className={cn(
                            "h-full transition-all duration-1000",
                            invader.type === 'logic' ? "w-[65%] bg-indigo-500/40" : "w-[45%] bg-purple-500/40"
                          )} />
                       </div>
                       <span className="text-[7px] font-mono text-zinc-700">STABLE</span>
                    </div>
                 </div>
 
                 {/* Decor Icon */}
                 <div className="text-white/5 group-hover:text-purple-500/10 transition-colors">
                    <Fingerprint size={16} />
                 </div>
                 
                 <div className="absolute left-0 top-0 bottom-0 w-[2px] bg-purple-500/50 scale-y-0 group-hover:scale-y-100 transition-transform origin-top" />
              </Reorder.Item>
            ))
          ) : (
            <div className="h-full flex flex-col items-center justify-center text-center space-y-4 py-20">
               <div className="p-4 rounded-full bg-zinc-900/50 border border-dashed border-white/10 opacity-20">
                  <ShieldAlert size={32} className="text-zinc-600" />
               </div>
               <div className="space-y-1 opacity-30">
                  <p className="text-[10px] font-bold text-zinc-400 uppercase tracking-[0.2em]">Zero Invaders Detected</p>
                  <p className="text-[9px] text-zinc-600 italic">Initiate broadcast to spawn invaders.</p>
               </div>
            </div>
          )}
        </Reorder.Group>
        
        <div className="h-10 border-t border-white/5 bg-black/40 flex items-center px-4">
          <span className="text-[8px] font-mono text-zinc-700 tracking-widest uppercase">
            Invader_Control_Console_Active
          </span>
        </div>
      </div>

      {menuState && (
        <TacticalMenu 
          top={menuState.y} 
          left={menuState.x} 
          onClose={() => setMenuState(null)}
          header={`Invader: ${menuState.id}`}
          actions={[
            { label: 'Save Invader Template', onClick: handleSaveToLibrary },
            { label: 'Purge Invader', onClick: handleDeleteInvader, variant: 'danger' }
          ]}
        />
      )}

      <style dangerouslySetInnerHTML={{ __html: `
        .custom-scrollbar::-webkit-scrollbar {
          width: 4px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
          background: transparent;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: rgba(255, 255, 255, 0.05);
          border-radius: 10px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: rgba(168, 85, 247, 0.2);
        }
      `}} />
    </aside>
  );
};
