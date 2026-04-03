import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import * as Lucide from 'lucide-react';

const Icons = Lucide as any;

const TOKEN_KEY = 'accessToken';

export function useAuth() {
    const [token, setTokenState] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));

    const setToken = (t: string) => {
        localStorage.setItem(TOKEN_KEY, t);
        setTokenState(t);
    };

    const clearToken = () => {
        localStorage.removeItem(TOKEN_KEY);
        setTokenState(null);
    };

    return { token, setToken, clearToken, isAuthed: !!token };
}

interface DevAuthPanelProps {
    onAuthed: () => void;
}

export default function DevAuthPanel({ onAuthed }: DevAuthPanelProps) {
    const { token, setToken, clearToken } = useAuth();
    const [input, setInput] = useState('');
    const [show, setShow] = useState(false);

    // If already has token, don't show by default
    useEffect(() => {
        if (!token) setShow(true);
    }, []);

    const apply = () => {
        const t = input.trim();
        if (!t) return;
        setToken(t);
        setInput('');
        setShow(false);
        onAuthed();
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') apply();
    };

    return (
        <>
            {/* Floating token status badge */}
            <div className="fixed bottom-4 left-4 z-[400]">
                <AnimatePresence>
                    {token ? (
                        <motion.div
                            key="authed"
                            initial={{ opacity: 0, y: 8 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: 8 }}
                            className="flex items-center gap-2 bg-zinc-900/90 border border-zinc-800 backdrop-blur-sm px-3 py-2 rounded-sm shadow-lg"
                        >
                            <div className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                            <span className="text-[9px] font-black uppercase tracking-widest text-zinc-500">
                                JWT Active ···{token.slice(-6)}
                            </span>
                            <button
                                onClick={() => setShow(true)}
                                className="ml-1 text-zinc-700 hover:text-zinc-400 transition-colors"
                            >
                                <Icons.Settings size={11} />
                            </button>
                        </motion.div>
                    ) : (
                        <motion.button
                            key="no-auth"
                            initial={{ opacity: 0, y: 8 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: 8 }}
                            onClick={() => setShow(true)}
                            className="flex items-center gap-2 bg-red-900/30 border border-red-700/40 backdrop-blur-sm px-3 py-2 rounded-sm shadow-lg hover:bg-red-900/50 transition-all"
                        >
                            <Icons.ShieldOff size={12} className="text-red-400" />
                            <span className="text-[9px] font-black uppercase tracking-widest text-red-400">
                                Chưa có JWT — Nhấn để đặt Token
                            </span>
                        </motion.button>
                    )}
                </AnimatePresence>
            </div>

            {/* Token input modal */}
            <AnimatePresence>
                {show && (
                    <div
                        className="fixed inset-0 z-[500] bg-black/50 backdrop-blur-sm flex items-end justify-start p-4"
                        onClick={() => token && setShow(false)}
                    >
                        <motion.div
                            initial={{ opacity: 0, y: 20 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: 20 }}
                            className="w-[480px] bg-zinc-950 border border-zinc-700 rounded-sm shadow-2xl overflow-hidden"
                            onClick={e => e.stopPropagation()}
                        >
                            <div className="p-4 border-b border-zinc-800 flex items-center justify-between">
                                <div className="flex items-center gap-3">
                                    <div className="w-6 h-6 bg-amber-500/10 border border-amber-500/30 rounded-sm flex items-center justify-center">
                                        <Icons.Key size={13} className="text-amber-400" />
                                    </div>
                                    <div>
                                        <p className="text-[10px] font-black uppercase tracking-widest text-zinc-300">Dev Auth</p>
                                        <p className="text-[8px] text-zinc-600 mt-0.5">Paste JWT access token để gọi API</p>
                                    </div>
                                </div>
                                {token && (
                                    <button onClick={() => setShow(false)} className="p-1 hover:bg-zinc-800 rounded-sm text-zinc-600">
                                        <Icons.X size={14} />
                                    </button>
                                )}
                            </div>

                            <div className="p-4 space-y-3">
                                <p className="text-[9px] text-zinc-600 leading-relaxed">
                                    Lấy token từ Portal sau khi login:<br />
                                    <span className="font-mono text-zinc-500">DevTools → Application → Local Storage → accessToken</span>
                                </p>

                                <textarea
                                    autoFocus
                                    value={input}
                                    onChange={e => setInput(e.target.value)}
                                    onKeyDown={handleKeyDown}
                                    placeholder="eyJhbGciOiJIUzI1NiIs..."
                                    rows={4}
                                    className="w-full bg-zinc-900 border border-zinc-800 focus:border-indigo-500/50 p-3 text-[10px] text-indigo-300 font-mono focus:outline-none resize-none rounded-sm"
                                />

                                <div className="flex gap-2">
                                    {token && (
                                        <button
                                            onClick={() => { clearToken(); setInput(''); }}
                                            className="px-4 py-2 bg-red-900/20 border border-red-800/30 text-red-400 text-[9px] font-black uppercase tracking-widest rounded-sm hover:bg-red-900/30"
                                        >
                                            Clear Token
                                        </button>
                                    )}
                                    <button
                                        onClick={apply}
                                        disabled={!input.trim()}
                                        className="flex-1 py-2 bg-indigo-600 hover:bg-indigo-500 text-white text-[9px] font-black uppercase tracking-widest disabled:opacity-40 rounded-sm transition-all"
                                    >
                                        Apply Token
                                    </button>
                                </div>
                            </div>
                        </motion.div>
                    </div>
                )}
            </AnimatePresence>
        </>
    );
}
