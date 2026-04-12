import React from 'react';
import { ReactFlowProvider } from 'reactflow';
import { Header } from './components/Header/Header';
import { Sidebar } from './components/Sidebar/Sidebar';
import { RightSidebar } from './components/Sidebar/RightSidebar';
import { StudioCanvas } from './components/Canvas/StudioCanvas';
import { Breadcrumbs } from './components/Header/Breadcrumbs';

function App() {
  return (
    <div className="relative h-screen w-screen overflow-hidden bg-zinc-950 text-slate-200 selection:bg-indigo-500/30 flex flex-col">
      {/* 1. Control Center (Header) */}
      <Header />
      
      {/* 2. Navigation Layer (Breadcrumbs) */}
      <Breadcrumbs />

      <div className="flex flex-1 w-full relative z-10 flex-row overflow-hidden">
        {/* 3. Left Side Panel (Templates & Tools) */}
        <Sidebar />

        {/* 4. Main Workspace (Architectural Canvas) */}
        <main className="flex-1 relative h-full w-full overflow-hidden bg-[#09090B]">
          <ReactFlowProvider>
             <StudioCanvas />
          </ReactFlowProvider>
        </main>

        {/* 5. Right Side Panel (Soul & Invader Controller) */}
        <RightSidebar />
      </div>

      {/* Ultra-subtle Monochrome Scanline Effect */}
      <div className="fixed inset-0 pointer-events-none z-[100] bg-[linear-gradient(rgba(18,16,16,0)_50%,rgba(0,0,0,0.1)_50%)] bg-[length:100%_4px] opacity-20"></div>
    </div>
  );
}

export default App;
