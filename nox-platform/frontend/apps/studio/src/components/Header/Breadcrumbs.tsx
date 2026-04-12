import React from 'react';
import { useStudio } from '../../context/StudioContext';
import { ChevronRight, Home } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export const Breadcrumbs = () => {
  const { navigationPath, exitToStep } = useStudio();

  return (
    <div className="flex h-10 w-full shrink-0 items-center border-b border-white/5 bg-zinc-950/30 px-6 backdrop-blur-sm relative z-30">
      <div className="flex items-center gap-2">
        {navigationPath.map((step, index) => {
          const isLast = index === navigationPath.length - 1;
          
          return (
            <React.Fragment key={step.id}>
              <button
                onClick={() => exitToStep(index)}
                disabled={isLast}
                className={cn(
                  "flex items-center gap-1.5 px-2 py-1 rounded transition-all duration-200",
                  isLast 
                    ? "text-zinc-100 font-bold bg-white/5 cursor-default" 
                    : "text-zinc-500 hover:text-zinc-300 hover:bg-white/5"
                )}
              >
                {step.id === 'root' ? (
                  <Home size={14} className={isLast ? "text-indigo-400" : ""} />
                ) : null}
                <span className="text-[11px] uppercase tracking-wider font-mono">
                  {step.label}
                </span>
              </button>
              
              {!isLast && (
                <ChevronRight size={14} className="text-zinc-700" />
              )}
            </React.Fragment>
          );
        })}
      </div>
      
      {/* Layer Depth Indicator */}
      <div className="ml-auto flex items-center gap-3">
        <span className="text-[10px] text-zinc-600 uppercase font-mono tracking-tighter">
          Layer Depth: <span className="text-indigo-500/50">{navigationPath.length - 1}</span>
        </span>
      </div>
    </div>
  );
};
