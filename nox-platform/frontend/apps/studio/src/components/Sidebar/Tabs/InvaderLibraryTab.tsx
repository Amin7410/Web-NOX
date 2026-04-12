import React from 'react';
import { Fingerprint } from 'lucide-react';
import { useStudio } from '../../../context/StudioContext';

interface InvaderLibraryTabProps {
  onContextMenu: (e: React.MouseEvent, id: string, type: 'invader') => void;
}

/**
 * Isolated Tab for managing the Invader Library.
 * Handles the display of saved templates and repository empty states.
 */
export const InvaderLibraryTab: React.FC<InvaderLibraryTabProps> = ({ onContextMenu }) => {
  const { savedInvaders } = useStudio();

  return (
    <div className="space-y-4 animate-in fade-in duration-300">
      <div className="flex items-center justify-between px-1">
        <h3 className="text-[10px] font-bold text-zinc-500 uppercase tracking-[0.2em]">Invader Library</h3>
        <div className="h-[1px] flex-1 bg-white/5 ml-4" />
      </div>
      
      <div className="grid grid-cols-1 gap-2">
        {(savedInvaders || []).length > 0 ? (
          savedInvaders?.map((invader) => (
            <div 
              key={invader.id}
              onContextMenu={(e) => onContextMenu(e, invader.id, 'invader')}
              className="group flex items-center gap-3 p-2.5 rounded bg-zinc-900 border border-white/5 hover:border-purple-500/30 transition-all cursor-grab active:cursor-grabbing"
            >
              <div className="w-8 h-8 rounded bg-zinc-950 border border-white/5 flex items-center justify-center text-zinc-600 group-hover:text-purple-400 transition-colors">
                <Fingerprint size={16} />
              </div>
              <div className="flex flex-col flex-1 min-w-0">
                <span className="text-[11px] font-bold text-zinc-200 truncate">{invader.name}</span>
                <div className="flex items-center gap-2">
                  <span className="text-[8px] font-mono text-zinc-600 uppercase tracking-tighter">{invader.type}</span>
                  <span className="text-[8px] font-mono text-zinc-700">•</span>
                  <span className="text-[8px] font-mono text-zinc-700">
                    {invader.createdAt ? new Date(invader.createdAt).toLocaleDateString() : 'N/A'}
                  </span>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="py-20 flex flex-col items-center justify-center text-center opacity-20">
            <div className="p-4 rounded-full border border-dashed border-white/20 mb-4">
              <Fingerprint size={32} className="text-zinc-600" />
            </div>
            <p className="text-[10px] uppercase tracking-widest text-zinc-500">Invader Repository Empty</p>
            <p className="text-[9px] italic text-zinc-700 mt-1">Capture invaders to build your library.</p>
          </div>
        )}
      </div>
    </div>
  );
};
