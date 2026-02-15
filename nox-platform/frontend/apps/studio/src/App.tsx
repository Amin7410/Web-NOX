import React, { useState } from 'react';
import { DesignLab } from './DesignLab';
import { DesignNOX } from './DesignNOX';

function App() {
    // Hidden toggle: Double click the top-left logo in DesignLab to switch? 
    // Or just a temporary manual toggle for now.
    const [mode, setMode] = useState<'lab' | 'nox'>('nox');

    return (
        <>
            {mode === 'lab' ? <DesignLab /> : <DesignNOX />}

            {/* Quick Switcher (Floating Bottom Left) */}
            <div className="fixed bottom-4 left-4 z-[9999] flex gap-2 opacity-50 hover:opacity-100 transition-opacity">
                <button
                    onClick={() => setMode('lab')}
                    className={`px-3 py-1 text-xs rounded border ${mode === 'lab' ? 'bg-white text-black border-white' : 'bg-black text-white border-zinc-700'}`}
                >
                    Lab
                </button>
                <button
                    onClick={() => setMode('nox')}
                    className={`px-3 py-1 text-xs rounded border ${mode === 'nox' ? 'bg-white text-black border-white' : 'bg-black text-white border-zinc-700'}`}
                >
                    NOX
                </button>
            </div>
        </>
    );
}

export default App;
