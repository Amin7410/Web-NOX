import React, { useMemo } from 'react';
import { Network, GitCommit, LogIn, LogOut } from 'lucide-react';
import { useStudio } from '../../../context/StudioContext';
import { DraggableItem } from '../Shared/DraggableItem';
import { NoxNodeType } from '../../../types/studio';

interface RelationsTabProps {
  onContextMenu: (e: React.MouseEvent, id?: string) => void;
}

/**
 * Tab for Wiring architecture and Gateway management.
 * Handles the layering and port connections in the NOX ecosystem.
 */
export const RelationsTab: React.FC<RelationsTabProps> = ({ onContextMenu }) => {
  const { savedBlocks } = useStudio();

  const categorizedSaved = useMemo(() => {
    const relationTypes: NoxNodeType[] = ['junction', 'inputTerminal', 'outputTerminal'];
    return {
      relations: (savedBlocks || []).filter(b => relationTypes.includes(b.type))
    };
  }, [savedBlocks]);

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
          onContextMenu={(e) => onContextMenu(e, 'junction-base')}
        />
        <div className="grid grid-cols-2 gap-2 mt-2">
          <DraggableItem 
            type="inputTerminal" 
            label="Input Port" 
            icon={LogIn} 
            category="terminal"
            colorScheme="emerald"
            isDefined={true}
            onContextMenu={(e) => onContextMenu(e, 'input-base')}
          />
          <DraggableItem 
            type="outputTerminal" 
            label="Output Port" 
            icon={LogOut} 
            category="terminal"
            colorScheme="amber"
            isDefined={true}
            onContextMenu={(e) => onContextMenu(e, 'output-base')}
          />
        </div>
      </div>
      
      {categorizedSaved.relations.length > 0 && (
        <div className="flex flex-col gap-3 mt-4 animate-in fade-in duration-500">
          <span className="text-[10px] font-bold text-indigo-500/50 uppercase tracking-widest mb-1">Custom Relays</span>
          {categorizedSaved.relations.map((rel) => (
            <DraggableItem 
              key={rel.id} 
              id={rel.id}
              type={rel.type} 
              label={rel.label} 
              icon={rel.type === 'junction' ? GitCommit : (rel.type === 'inputTerminal' ? LogIn : LogOut)} 
              category={rel.type === 'junction' ? 'relation' : 'terminal'}
              colorScheme={rel.type === 'inputTerminal' ? 'emerald' : (rel.type === 'outputTerminal' ? 'amber' : 'indigo')}
              isDefined={true}
              isCustom={true}
              onContextMenu={(e) => onContextMenu(e, rel.id)}
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
};
