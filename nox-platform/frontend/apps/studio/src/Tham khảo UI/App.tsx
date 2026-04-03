import { ReactFlowProvider } from 'reactflow';
import StudioCanvas from './canvas/StudioCanvas';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import DevAuthPanel from './components/DevAuthPanel';
import 'reactflow/dist/style.css';

export default function App() {
    // Read workspaceId from URL: e.g. /studio?workspaceId=<UUID>
    const params = new URLSearchParams(window.location.search);
    const workspaceId = params.get('workspaceId') ?? null;

    return (
        <div className="flex h-screen w-screen flex-col overflow-hidden bg-zinc-950 text-slate-200 font-sans selection:bg-indigo-500/30">
            {/* Top Navigation */}
            <Header />
            
            <div className="flex flex-1 relative h-full w-full overflow-hidden">
                {/* Fixed Left Sidebar */}
                <Sidebar />
                
                {/* Main Interactive Canvas */}
                <main className="flex-1 relative h-full w-full bg-[#09090B]">
                    <ReactFlowProvider>
                        <StudioCanvas workspaceId={workspaceId} />
                    </ReactFlowProvider>
                </main>
            </div>

            {/* Global Grid Overlay */}
            <div className="fixed inset-0 pointer-events-none opacity-[0.03] z-[100]"
                style={{
                    backgroundImage: `linear-gradient(#fff 1px, transparent 1px), linear-gradient(90deg, #fff 1px, transparent 1px)`,
                    backgroundSize: '100px 100px'
                }}
            />

            {/* Dev Auth Panel — JWT Token management */}
            <DevAuthPanel onAuthed={() => window.location.reload()} />
        </div>
    );
}
