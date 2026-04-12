import React from 'react';

interface ContextMenuProps {
  top: number;
  left: number;
  onSaveBlock: () => void;
  onClick: () => void;
}

export const ContextMenu = ({ top, left, onSaveBlock, onClick }: ContextMenuProps) => (
  <div 
    style={{ top, left }}
    className="fixed z-[1000] min-w-[140px] bg-white border border-zinc-200 shadow-2xl rounded-lg py-1.5 overflow-hidden animate-in fade-in zoom-in duration-100"
    onClick={(e) => {
      e.stopPropagation();
      onClick();
    }}
  >
    <button 
      onClick={onSaveBlock}
      className="w-full px-4 py-2 text-left text-xs font-bold text-indigo-600 hover:bg-indigo-50 transition-colors border-b border-zinc-100 flex items-center gap-2"
    >
      <div className="h-1.5 w-1.5 rounded-full bg-indigo-500" />
      Lưu block
    </button>
    <button className="w-full px-4 py-2 text-left text-xs text-zinc-500 hover:bg-zinc-50 transition-colors border-b border-zinc-100 italic">
      Settings 2
    </button>
    <button className="w-full px-4 py-2 text-left text-xs text-zinc-500 hover:bg-zinc-50 transition-colors italic">
      Settings 3
    </button>
  </div>
);
