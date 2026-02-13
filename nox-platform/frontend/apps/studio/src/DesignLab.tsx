import React, { useState, useCallback, useRef } from 'react';
import { Button, Input, NodeShell } from '@nox/ui';
import ReactFlow, {
    ReactFlowProvider,
    addEdge,
    useNodesState,
    useEdgesState,
    Controls,
    Background,
    Connection,
    Node,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { Sidebar } from './components/Sidebar';

const nodeTypes = {
    start: NodeShell,
    process: NodeShell,
    end: NodeShell,
};

let id = 0;
const getId = () => `dndnode_${id++}`;

export function DesignLab() {
    const [component, setComponent] = useState<'button' | 'input' | 'typography' | 'interaction'>('button');

    return (
        <div className="min-h-screen bg-[#09090B] text-white p-8 font-sans selection:bg-blue-500/30">
            {/* Grid Background */}
            <div className="fixed inset-0 pointer-events-none opacity-20"
                style={{
                    backgroundImage: `linear-gradient(#222 1px, transparent 10px), linear-gradient(90deg, #b1b0b0 1px, transparent 1px)`,
                    backgroundSize: '40px 40px'
                }}
            />

            <div className="relative z-10 max-w-7xl mx-auto">
                <header className="flex justify-between items-center mb-12 border-b border-white/10 pb-6">
                    <div>
                        <h1 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
                            Nox Design Lab
                        </h1>
                        <p className="text-white/40 mt-1">Component Polish & Micro-interactions</p>
                    </div>

                    <div className="flex gap-2">
                        <button
                            onClick={() => setComponent('button')}
                            className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${component === 'button'
                                ? 'bg-white/10 text-white'
                                : 'text-white/40 hover:text-white'
                                }`}
                        >
                            Button
                        </button>
                        <button
                            onClick={() => setComponent('input')}
                            className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${component === 'input'
                                ? 'bg-white/10 text-white'
                                : 'text-white/40 hover:text-white'
                                }`}
                        >
                            Input
                        </button>
                        <button
                            onClick={() => setComponent('typography')}
                            className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${component === 'typography'
                                ? 'bg-white/10 text-white'
                                : 'text-white/40 hover:text-white'
                                }`}
                        >
                            Typography
                        </button>
                        <button
                            onClick={() => setComponent('interaction')}
                            className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${component === 'interaction'
                                ? 'bg-white/10 text-white'
                                : 'text-white/40 hover:text-white'
                                }`}
                        >
                            Interaction
                        </button>
                    </div>
                </header>

                <main className={component === 'interaction' ? 'h-[calc(100vh-8rem)]' : ''}>
                    {component === 'button' && <ButtonShowcase />}
                    {component === 'input' && <InputShowcase />}
                    {component === 'typography' && <TypographyShowcase />}
                    {component === 'interaction' && (
                        <div className="h-full border border-white/10 rounded-xl overflow-hidden flex bg-[#09090B]">
                            <ReactFlowProvider>
                                <InteractionLab />
                            </ReactFlowProvider>
                        </div>
                    )}
                </main>
            </div>
        </div>
    );
}

const initialNodes = [
    {
        id: '1',
        type: 'start',
        data: { label: 'Start Flow', description: 'Entry point for the user journey.' },
        position: { x: 250, y: 5 },
    },
];

function InteractionLab() {
    const reactFlowWrapper = useRef<HTMLDivElement>(null);
    const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState([]);
    const [reactFlowInstance, setReactFlowInstance] = useState<any>(null);

    const onConnect = useCallback(
        (params: Connection) => setEdges((eds) => addEdge(params, eds)),
        [setEdges]
    );

    const onDragOver = useCallback((event: React.DragEvent) => {
        event.preventDefault();
        event.dataTransfer.dropEffect = 'move';
    }, []);

    const onDrop = useCallback(
        (event: React.DragEvent) => {
            event.preventDefault();

            const type = event.dataTransfer.getData('application/reactflow');

            // check if the dropped element is valid
            if (typeof type === 'undefined' || !type) {
                return;
            }

            const position = reactFlowInstance.screenToFlowPosition({
                x: event.clientX,
                y: event.clientY,
            });

            const newNode: Node = {
                id: getId(),
                type,
                position,
                data: { label: `${type} node`, description: 'New node added via drag & drop.' },
            };

            setNodes((nds) => nds.concat(newNode));
        },
        [reactFlowInstance, setNodes]
    );

    return (
        <div className="dndflow flex w-full h-full">
            <Sidebar />
            <div className="reactflow-wrapper flex-grow h-full relative" ref={reactFlowWrapper}>
                <ReactFlow
                    nodes={nodes}
                    edges={edges}
                    onNodesChange={onNodesChange}
                    onEdgesChange={onEdgesChange}
                    onConnect={onConnect}
                    onInit={setReactFlowInstance}
                    onDrop={onDrop}
                    onDragOver={onDragOver}
                    nodeTypes={nodeTypes}
                    proOptions={{ hideAttribution: true }}
                    fitView
                >
                    <Background color="#222" gap={20} size={1} />
                    <Controls className="!bg-zinc-800 !border-zinc-700 [&>button]:!fill-zinc-400 [&>button:hover]:!bg-zinc-700" />
                </ReactFlow>
            </div>
        </div>
    );
}

function TypographyShowcase() {
    return (
        <div className="space-y-12">
            <div className="space-y-4">
                <h2 className="text-xl font-semibold opacity-80">Sans-Serif (Inter)</h2>
                <div className="p-8 bg-surface border border-border rounded-2xl space-y-4">
                    <p className="text-xs">The quick brown fox jumps over the lazy dog (xs)</p>
                    <p className="text-sm">The quick brown fox jumps over the lazy dog (sm)</p>
                    <p className="text-base">The quick brown fox jumps over the lazy dog (base)</p>
                    <p className="text-lg font-medium">The quick brown fox jumps over the lazy dog (lg)</p>
                    <p className="text-xl font-bold">The quick brown fox jumps over the lazy dog (xl)</p>
                </div>
            </div>

            <div className="space-y-4">
                <h2 className="text-xl font-semibold opacity-80">Monospace (JetBrains Mono)</h2>
                <div className="p-8 bg-surface border border-border rounded-2xl space-y-4 font-mono">
                    <p className="text-xs">const nox = "awesome"; (xs)</p>
                    <p className="text-sm">console.log(nox); (sm)</p>
                    <p className="text-base">npm install @nox/ui (base)</p>
                </div>
            </div>
        </div>
    );
}

function InputShowcase() {
    return (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
            <div className="space-y-8">
                <h2 className="text-xl font-semibold opacity-80">States</h2>
                <div className="p-8 bg-[#0A0A0A] border border-white/5 rounded-2xl space-y-6">
                    <div className="space-y-2">
                        <label className="text-sm font-medium opacity-80">Default</label>
                        <Input placeholder="Enter your email..." />
                    </div>
                    <div className="space-y-2">
                        <label className="text-sm font-medium opacity-80">With Icon</label>
                        <Input
                            startIcon={<span className="">@</span>}
                            placeholder="Email address"
                        />
                    </div>
                    <div className="space-y-2">
                        <label className="text-sm font-medium opacity-80">Error State</label>
                        <Input error placeholder="Invalid input" defaultValue="wrong@value" />
                        <p className="text-xs text-destructive">Invalid email address</p>
                    </div>
                    <div className="space-y-2">
                        <label className="text-sm font-medium opacity-80">Disabled</label>
                        <Input disabled placeholder="Cannot type here" />
                    </div>
                </div>
            </div>

            <div className="space-y-8">
                <h2 className="text-xl font-semibold opacity-80">Sizes</h2>
                <div className="p-8 bg-[#0A0A0A] border border-white/5 rounded-2xl space-y-6">
                    <Input size="sm" placeholder="Small input" />
                    <Input size="md" placeholder="Medium input (Default)" />
                    <Input size="lg" placeholder="Large input" />
                </div>
            </div>
        </div>
    );
}

function ButtonShowcase() {
    return (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
            {/* Micro-Detail View (Hero) */}
            <div className="col-span-1 lg:col-span-2 bg-[#0A0A0A] border border-white/5 rounded-3xl p-24 flex items-center justify-center relative overflow-hidden group">
                <div className="absolute inset-0 bg-blue-500/5 blur-3xl rounded-full scale-150 opacity-0 group-hover:opacity-100 transition-opacity duration-1000" />

                {/* Placeholder for the "Hero" button, will use the polished Button later */}
                <div className="scale-150">
                    <Button>Hero Action</Button>
                </div>
            </div>

            {/* Variants Row */}
            <div className="space-y-8">
                <h2 className="text-xl font-semibold opacity-80">Variants</h2>
                <div className="flex flex-wrap gap-4 p-8 bg-[#0A0A0A] border border-white/5 rounded-2xl">
                    <Button variant="primary">Primary</Button>
                    <Button variant="secondary">Secondary</Button>
                    <Button variant="outline">Outline</Button>
                    <Button variant="ghost">Ghost</Button>
                    <Button variant="destructive">Destructive</Button>
                </div>
            </div>

            {/* Sizes Row */}
            <div className="space-y-8">
                <h2 className="text-xl font-semibold opacity-80">Sizes</h2>
                <div className="flex flex-wrap items-center gap-4 p-8 bg-[#0A0A0A] border border-white/5 rounded-2xl">
                    <Button size="sm">Small</Button>
                    <Button size="md">Medium</Button>
                    <Button size="lg">Large</Button>
                </div>
            </div>
        </div>
    );
}
