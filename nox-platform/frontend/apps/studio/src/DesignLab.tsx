import React, { useState, useCallback, useRef } from 'react';
import { Button, Input, NoxBlock } from '@nox/ui';
import ReactFlow, {
    ReactFlowProvider,
    useNodesState,
    Controls,
    Background,
    Node,
} from 'reactflow';
import { ChevronRight, Home } from 'lucide-react';
import 'reactflow/dist/style.css';
import { Sidebar } from './components/Sidebar';

const nodeTypes = {
    noxBlock: NoxBlock,
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

// --- MOCK DATA FOR NESTED NAVIGATION ---
// --- RECURSIVE MOCK DATA GENERATION (10 LEVELS) ---
const MAX_DEPTH = 10;

/**
 * Generates a mock hierarchy with SEMANTIC, REALISTIC data.
 */
const generateMockHierarchy = () => {
    const hierarchy: Record<string, { nodes: Node[] }> = {};

    // Definition of "Roles" for different levels to make it look real
    const ROLES = {
        LEVEL_1: [
            { label: 'E-Commerce Platform', type: 'system', variant: 'default', icon: 'box', color: 'blue' },
            { label: 'Analytics Engine', type: 'system', variant: 'sleek', icon: 'activity', color: 'purple' },
            { label: 'Legacy Inventory', type: 'system', variant: 'rugged', icon: 'server', color: 'amber' },
        ],
        LEVEL_2: [
            { label: 'Auth Service', type: 'service', variant: 'default', icon: 'server', color: 'blue' },
            { label: 'Payment Gateway', type: 'service', variant: 'default', icon: 'server', color: 'green' },
            { label: 'User Database', type: 'db', variant: 'rugged', icon: 'database', color: 'indigo' },
            { label: 'Redis Cache', type: 'db', variant: 'rugged', icon: 'database', color: 'red' },
            { label: 'Recommendation AI', type: 'ai', variant: 'sleek', icon: 'cpu', color: 'purple' },
        ],
        LEVEL_3: [
            { label: 'API Handler', type: 'worker', variant: 'default', icon: 'settings', color: 'blue' },
            { label: 'Background Worker', type: 'worker', variant: 'rugged', icon: 'settings', color: 'zinc' },
            { label: 'Event Bus', type: 'infra', variant: 'sleek', icon: 'activity', color: 'orange' },
        ],
    };

    const getRole = (depth: number, index: number) => {
        if (depth === 1) return ROLES.LEVEL_1[index % ROLES.LEVEL_1.length];
        if (depth === 2) return ROLES.LEVEL_2[index % ROLES.LEVEL_2.length];
        return ROLES.LEVEL_3[index % ROLES.LEVEL_3.length];
    };

    // Helper to generate blocks for a specific parent view
    const generateLevel = (viewId: string, currentDepth: number) => {
        if (currentDepth > MAX_DEPTH) return;

        const nodes: Node[] = [];
        const count = currentDepth === 1 ? 3 : 4; // 3 Root nodes, 4 children each

        for (let i = 0; i < count; i++) {
            const blockId = `${viewId}-child-${i}`;
            const role = getRole(currentDepth, i);

            // Add some randomness to status
            // 10% chance error, 20% chance stopped
            // Use random seed based on ID hash to be deterministic if possible, but Math.random() is fine for mock
            const isError = Math.random() > 0.9;
            const isStopped = Math.random() > 0.8;

            const status = {
                state: (isError ? 'error' : (isStopped ? 'stopped' : 'running')) as any,
                message: isError ? 'CONNECTION ERR' : (isStopped ? 'HALTED' : undefined),
                lastActive: Date.now() - Math.floor(Math.random() * 100000),
            };

            // Custom "Sleek" variants for AI nodes
            const visual = {
                variant: role.variant as any,
                icon: role.icon,
                color: role.color,
            };

            // Create the node
            nodes.push({
                id: blockId,
                type: 'noxBlock',
                data: {
                    label: role.label,
                    id: blockId,
                    visual,
                    status
                },
                position: { x: 50 + (i * 340), y: 100 + (i % 2 * 50) } // Staggered layout
            });

            // Recursively generate the INTERNAL view for this block
            generateLevel(blockId, currentDepth + 1);
        }

        hierarchy[viewId] = { nodes };
    };

    generateLevel('root', 1);
    return hierarchy;
};

const MOCK_DATA = generateMockHierarchy();


function InteractionLab() {
    const reactFlowWrapper = useRef<HTMLDivElement>(null);
    const [reactFlowInstance, setReactFlowInstance] = useState<any>(null);

    // Navigation State
    const [_currentViewId, setCurrentViewId] = useState<string>('root');
    const [path, setPath] = useState<{ id: string, label: string }[]>([{ id: 'root', label: 'Main Workspace' }]);

    const [nodes, setNodes, onNodesChange] = useNodesState(MOCK_DATA['root'].nodes);

    // Handle Double Click to Enter Nested View
    const onNodeDoubleClick = useCallback((_event: React.MouseEvent, node: Node) => {
        if (node.type === 'noxBlock' && MOCK_DATA[node.id]) {
            const viewId = node.id;
            setCurrentViewId(viewId);
            setPath((prev) => [...prev, { id: viewId, label: node.data.label || viewId }]);

            // Load mocked data for this view
            setNodes(MOCK_DATA[viewId].nodes);
        }
    }, [setNodes]);

    // Navigate back using Breadcrumbs
    const navigateTo = (viewId: string, index: number) => {
        setCurrentViewId(viewId);
        setPath((prev) => prev.slice(0, index + 1));

        const data = MOCK_DATA[viewId] || { nodes: [] };
        setNodes(data.nodes);
    };

    const onDragOver = useCallback((event: React.DragEvent) => {
        event.preventDefault();
        event.dataTransfer.dropEffect = 'move';
    }, []);

    const onDrop = useCallback(
        (event: React.DragEvent) => {
            event.preventDefault();

            const type = event.dataTransfer.getData('application/reactflow');
            if (typeof type === 'undefined' || !type) {
                return;
            }

            const position = reactFlowInstance.screenToFlowPosition({
                x: event.clientX,
                y: event.clientY,
            });

            const newNode: Node = {
                id: getId(),
                type: type === 'noxBlock' ? 'noxBlock' : type, // Handle sidebar drop types if needed
                position,
                data: {
                    label: `${type} node`,
                    description: 'New node.',
                    status: { state: 'running' },
                    visual: { variant: 'default', icon: 'box', color: 'blue' }
                },
            };

            setNodes((nds) => nds.concat(newNode));
        },
        [reactFlowInstance, setNodes]
    );

    return (
        <div className="flex flex-col h-full w-full">
            {/* Breadcrumb Navigation */}
            <div className="h-10 bg-zinc-900 border-b border-zinc-800 flex items-center px-4 gap-2 text-sm">
                {path.map((item, index) => (
                    <React.Fragment key={item.id}>
                        {index > 0 && <ChevronRight size={14} className="text-zinc-600" />}
                        <button
                            onClick={() => navigateTo(item.id, index)}
                            className={`flex items-center gap-2 hover:text-white transition-colors ${index === path.length - 1 ? 'text-white font-semibold' : 'text-zinc-500'}`}
                        >
                            {item.id === 'root' && <Home size={14} />}
                            {item.label}
                        </button>
                    </React.Fragment>
                ))}
            </div>

            <div className="dndflow flex w-full flex-grow relative">
                <Sidebar />
                <div className="reactflow-wrapper flex-grow h-full relative" ref={reactFlowWrapper}>
                    <ReactFlow
                        nodes={nodes}
                        edges={[]}
                        onNodesChange={onNodesChange}
                        onInit={setReactFlowInstance}
                        onDrop={onDrop}
                        onDragOver={onDragOver}
                        onNodeDoubleClick={onNodeDoubleClick}
                        nodeTypes={nodeTypes}
                        proOptions={{ hideAttribution: true }}
                        fitView
                    >
                        <Background color="#222" gap={20} size={1} />
                        <Controls className="!bg-zinc-800 !border-zinc-700 [&>button]:!fill-zinc-400 [&>button:hover]:!bg-zinc-700" />
                    </ReactFlow>
                </div>
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
