import React, { useMemo } from 'react';
import { BoxSelect, Shapes, Bookmark } from 'lucide-react';
import { useStudio } from '../../../context/StudioContext';
import { DraggableItem } from '../Shared/DraggableItem';
import { NoxNodeType } from '../../../types/studio';

interface BlocksTabProps {
  onContextMenu: (e: React.MouseEvent, id?: string) => void;
}

/**
 * Tab dedicated to Module Management (Blocks & Templates).
 * Handles categorization between base modules and saved user modules.
 */
export const BlocksTab: React.FC<BlocksTabProps> = ({ onContextMenu }) => {
  const { savedBlocks } = useStudio();

  const categorizedSaved = useMemo(() => {
    const relationTypes: NoxNodeType[] = ['junction', 'inputTerminal', 'outputTerminal'];
    return {
      blocks: (savedBlocks || []).filter(b => !relationTypes.includes(b.type))
    };
  }, [savedBlocks]);

  return (
    <div className="flex flex-col gap-6 animate-in slide-in-from-left-2 duration-300">
      <div className="flex flex-col gap-3">
        <span className="text-[10px] font-bold text-zinc-600 uppercase tracking-widest mb-1">Base Modules</span>
        <DraggableItem 
          type="undefined" 
          label="Conceptual Block" 
          icon={BoxSelect} 
          onContextMenu={(e) => onContextMenu(e)}
        />
        <DraggableItem 
          type="ui" 
          label="Logical Module" 
          icon={Shapes} 
          isDefined={true} 
          onContextMenu={(e) => onContextMenu(e)}
        />
      </div>
      
      {categorizedSaved.blocks.length > 0 && (
        <div className="flex flex-col gap-3">
          <span className="text-[10px] font-bold text-emerald-500/50 uppercase tracking-widest mb-1">Saved Blocks</span>
          {categorizedSaved.blocks.map((block) => (
            <DraggableItem 
              key={block.id} 
              id={block.id}
              type={block.type} 
              label={block.label} 
              icon={Bookmark} 
              isDefined={true} 
              isCustom={true} 
              onContextMenu={(e) => onContextMenu(e, block.id)}
            />
          ))}
        </div>
      )}
    </div>
  );
};
