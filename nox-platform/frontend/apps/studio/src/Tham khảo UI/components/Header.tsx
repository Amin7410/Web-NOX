import { Box, Cloud } from 'lucide-react';
import { motion } from 'framer-motion';

export default function Header() {
    return (
        <header className="h-14 w-full flex items-center justify-between px-6 border-b border-zinc-800/50 bg-zinc-950/80 backdrop-blur-md z-50 shrink-0">
            {/* Logo & Breadcrumbs */}
            <div className="flex items-center gap-6">
                <div className="flex items-center gap-3 pr-6 border-r border-zinc-800/50">
                    <div className="w-8 h-8 rounded-sm bg-gradient-to-tr from-indigo-500 to-purple-600 flex items-center justify-center shadow-[0_0_15px_rgba(99,102,241,0.3)]">
                        <Box className="w-5 h-5 text-white" />
                    </div>
                    <span className="font-semibold text-lg tracking-wider text-slate-100 hidden md:block">
                        NOX<span className="font-light text-slate-400">STUDIO</span>
                    </span>
                </div>
            </div>

            {/* Technical Status & Actions */}
            <div className="flex items-center gap-4 text-xs font-medium text-slate-400">
                <div className="hidden sm:flex items-center gap-2 bg-zinc-900/50 border border-zinc-800 px-3 py-1.5 rounded-sm">
                    <motion.div 
                        initial={{ opacity: 0.5 }}
                        animate={{ opacity: [0.5, 1, 0.5] }}
                        transition={{ duration: 2, repeat: Infinity }}
                        className="w-2 h-2 rounded-full bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.5)]"
                    />
                    <span className="text-[10px] uppercase tracking-tighter">Live Syncing</span>
                </div>
                
                <button className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 transition-all text-white px-4 py-1.5 rounded-sm shadow-[0_0_15px_rgba(79,70,229,0.3)] active:scale-95">
                    <Cloud className="w-4 h-4" />
                    <span className="font-bold">PUBLISH</span>
                </button>
            </div>
        </header>
    );
}
