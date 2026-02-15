import React, { useState, useCallback, useRef, useEffect } from 'react';

import ReactFlow, {
    ReactFlowProvider,
    useNodesState,
    useEdgesState,
    Controls,
    Background,
    Node,
    Edge,
    useReactFlow,
    Panel,
    MiniMap, addEdge, Connection, Handle, Position, MarkerType
} from 'reactflow';
import { Zap, Monitor, Layers, MousePointer2, Share2, Home, ChevronRight, CircleDot } from 'lucide-react';
import 'reactflow/dist/style.css';
import { Sidebar } from './components/Sidebar';
import { Button, Input, NoxRouter, NoxRelation, NoxBlock } from '@nox/ui';
import { FamilyTreeOverlay } from './FamilyTreeOverlay';

// --- Types & Config ---
const nodeTypes = {
    noxBlock: NoxBlock,
    noxRouter: NoxRouter,
};

const edgeTypes = {
    noxRelation: NoxRelation,
};

let id = 0;
const getId = () => `node_${Date.now()}_${id++}`;

// --- Error Boundary ---
class ErrorBoundary extends React.Component<{ children: React.ReactNode }, { hasError: boolean, error: Error | null }> {
    constructor(props: { children: React.ReactNode }) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error: Error) {
        return { hasError: true, error };
    }

    componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
        console.error("DesignNOX Crash:", error, errorInfo);
    }

    render() {
        if (this.state.hasError) {
            return (
                <div className="flex flex-col items-center justify-center h-screen bg-zinc-950 text-red-500 p-8 text-center">
                    <h1 className="text-3xl font-bold mb-4">System Malfunction</h1>
                    <p className="text-zinc-400 max-w-lg mb-8">
                        The DesignNOX Interface encountered a critical error.
                    </p>
                    <pre className="bg-zinc-900 p-4 rounded text-left text-xs font-mono overflow-auto max-w-2xl border border-red-900/50">
                        {this.state.error?.toString()}
                    </pre>
                    <button
                        onClick={() => window.location.reload()}
                        className="mt-8 px-6 py-2 bg-red-600 hover:bg-red-700 text-white rounded-md font-medium"
                    >
                        Reboot Interface
                    </button>
                </div>
            );
        }

        return this.props.children;
    }
}

// --- MOCK DATA GENERATOR (10 Levels) ---
const MAX_DEPTH = 10;

const generateMockHierarchy = () => {
    const hierarchy: Record<string, { nodes: Node[], edges: Edge[] }> = {};

    const ROLES = [
        { label: 'System', variant: 'rugged', icon: 'server', color: 'amber' },
        { label: 'Service', variant: 'default', icon: 'activity', color: 'blue' },
        { label: 'Worker', variant: 'sleek', icon: 'cpu', color: 'purple' },
        { label: 'Database', variant: 'rugged', icon: 'database', color: 'red' },
    ];

    const generateLevel = (viewId: string, currentDepth: number) => {
        if (currentDepth > MAX_DEPTH) return;

        const nodes: Node[] = [];
        const count = 3;

        for (let i = 0; i < count; i++) {
            const blockId = `${viewId}_child_${i} `;
            const role = ROLES[i % ROLES.length];

            nodes.push({
                id: blockId,
                type: 'noxBlock',
                data: {
                    id: blockId,
                    label: `${role.label} L${currentDepth} `,
                    visual: { variant: role.variant, icon: role.icon, color: role.color },
                    status: { state: 'running' },
                    logic: '// Processing...'
                },
                position: { x: 100 + (i * 300), y: 150 + (i % 2 * 50) }
            });

            generateLevel(blockId, currentDepth + 1);
        }

        hierarchy[viewId] = { nodes: nodes, edges: [] };
    };

    generateLevel('root', 1);
    return hierarchy;
};

const MOCK_DATA = generateMockHierarchy();

// --- Context Menu ---
const EdgeContextMenu = ({
    edge,
    position,
    onChange,
    onDelete,
    onClose
}: {
    edge: Edge | null,
    position: { x: number, y: number } | null,
    onChange: (id: string, data: any) => void,
    onDelete: (id: string) => void,
    onClose: () => void
}) => {
    if (!edge || !position) return null;
    const data = edge.data || {};

    return (
        <div
            className="fixed w-56 bg-zinc-900 border border-zinc-700 rounded-lg shadow-2xl p-2 z-[9999] flex flex-col gap-2 animate-in fade-in zoom-in-95 duration-100"
            style={{ top: position.y, left: position.x }}
            onContextMenu={(e) => e.preventDefault()}
        >
            <div className="flex items-center justify-between px-2 pb-2 border-b border-zinc-800">
                <span className="text-[10px] font-bold text-zinc-400 uppercase tracking-wider">Relation Settings</span>
                <button onClick={onClose} className="text-zinc-500 hover:text-zinc-300 text-xs">✕</button>
            </div>

            {/* Visual State */}
            <div className="grid grid-cols-3 gap-1">
                {['valid', 'invalid', 'placeholder'].map((state) => (
                    <button
                        key={state}
                        onClick={() => onChange(edge.id, { ...data, state })}
                        className={`py - 1 text - [9px] rounded uppercase font - medium transition - colors ${data.state === state ? 'bg-blue-500/20 text-blue-400 border border-blue-500/50' : 'bg-zinc-800 text-zinc-500 hover:bg-zinc-700'} `}
                        title={state}
                    >
                        {state}
                    </button>
                ))}
            </div>

            {/* Shape */}
            <div className="grid grid-cols-3 gap-1">
                {['curve', 'straight', 'step'].map((shape) => (
                    <button
                        key={shape}
                        onClick={() => onChange(edge.id, { ...data, shape })}
                        className={`py - 1 text - [9px] rounded uppercase font - medium transition - colors ${data.shape === shape ? 'bg-purple-500/20 text-purple-400 border border-purple-500/50' : 'bg-zinc-800 text-zinc-500 hover:bg-zinc-700'} `}
                        title={shape}
                    >
                        {shape.substring(0, 4)}..
                    </button>
                ))}
            </div>

            {/* Actions */}
            <div className="pt-2 border-t border-zinc-800 flex items-center justify-between gap-2">
                <button
                    onClick={() => onChange(edge.id, { ...data, animating: !data.animating })}
                    className={`flex - 1 py - 1.5 text - [10px] rounded font - medium flex items - center justify - center gap - 1.5 ${data.animating ? 'bg-emerald-500/10 text-emerald-400' : 'bg-zinc-800 text-zinc-400 hover:bg-zinc-700'} `}
                >
                    <div className={`w - 1.5 h - 1.5 rounded - full ${data.animating ? 'bg-emerald-400 animate-pulse' : 'bg-zinc-600'} `} />
                    Flow
                </button>
                <button
                    onClick={() => onDelete(edge.id)}
                    className="flex-1 py-1.5 text-[10px] rounded font-medium bg-red-500/10 text-red-400 hover:bg-red-500/20 flex items-center justify-center gap-1"
                >
                    Delete
                </button>
            </div>
        </div>
    );
};

// --- Main Canvas Logic ---
const DesignCanvas = () => {
    const reactFlowWrapper = useRef<HTMLDivElement>(null);
    const [reactFlowInstance, setReactFlowInstance] = useState<any>(null);

    // Initial Data
    const [currentViewId, setCurrentViewId] = useState<string>('root');
    const [path, setPath] = useState<{ id: string, label: string }[]>([{ id: 'root', label: 'ROOT' }]);

    const [nodes, setNodes, onNodesChange] = useNodesState(MOCK_DATA['root'].nodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState([]);
    const [showFamilyTree, setShowFamilyTree] = useState(false); // Structural View State // Initially empty edges

    // Interaction State
    const [selectedEdgeId, setSelectedEdgeId] = useState<string | null>(null);
    const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
    const [isConnecting, setIsConnecting] = useState(false);
    const [sourceNodeId, setSourceNodeId] = useState<string | null>(null);
    const [menuPosition, setMenuPosition] = useState<{ x: number, y: number } | null>(null);
    const [nodeMenuPosition, setNodeMenuPosition] = useState<{ x: number, y: number } | null>(null);

    const selectedEdge = edges.find(e => e.id === selectedEdgeId) || null;
    const selectedNode = nodes.find(n => n.id === selectedNodeId) || null;

    // Helpers
    const navigateTo = (viewId: string, index: number) => {
        // Save state of current view ??? (In a real app, yes. Here, mock data resets or persists in memory object)
        // Ideally we update MOCK_DATA with current nodes/edges before leaving

        setCurrentViewId(viewId);
        setPath((prev) => prev.slice(0, index + 1));

        const data = MOCK_DATA[viewId] || { nodes: [], edges: [] };
        setNodes(data.nodes);
        setEdges(data.edges);
    };

    const updateEdgeData = (id: string, newData: any) => {
        setEdges((eds) => eds.map((e) => e.id === id ? { ...e, data: { ...e.data, ...newData } } : e));
    };

    const updateNodeData = (id: string, newData: any) => {
        setNodes((nds) => nds.map((n) => n.id === id ? { ...n, data: { ...n.data, ...newData } } : n));
    };

    const deleteEdge = (id: string) => {
        setEdges((eds) => eds.filter(e => e.id !== id));
        setMenuPosition(null);
        setSelectedEdgeId(null);
    };

    const deleteNode = (id: string) => {
        setNodes((nds) => nds.filter(n => n.id !== id));
        // Also remove connected edges? ReactFlow usually handles visuals, but data wise:
        setEdges((eds) => eds.filter(e => e.source !== id && e.target !== id));
        setNodeMenuPosition(null);
        setSelectedNodeId(null);
    };

    // Handlers
    const onNodeDoubleClick = useCallback((_event: React.MouseEvent, node: Node) => {
        if (node.type === 'noxBlock') {
            // Drill Down
            const viewId = node.id;

            // Generate data if not exists (lazy load logic simulation)
            if (!MOCK_DATA[viewId]) {
                MOCK_DATA[viewId] = { nodes: [], edges: [] };
            }

            setCurrentViewId(viewId);
            setPath((prev) => [...prev, { id: viewId, label: node.data.label || 'Unknown' }]);

            setNodes(MOCK_DATA[viewId].nodes);
            setEdges(MOCK_DATA[viewId].edges);
        }
    }, [setNodes, setEdges]);

    const onEdgeClick = useCallback((event: React.MouseEvent, edge: Edge) => {
        event.stopPropagation();
        setSelectedEdgeId(edge.id);
        setMenuPosition(null);
        setNodeMenuPosition(null);
    }, []);

    const onEdgeContextMenu = useCallback((event: React.MouseEvent, edge: Edge) => {
        event.preventDefault();
        event.stopPropagation();
        setSelectedEdgeId(edge.id);
        setMenuPosition({ x: event.clientX, y: event.clientY });
        setNodeMenuPosition(null);
    }, []);

    const onNodeContextMenu = useCallback((event: React.MouseEvent, node: Node) => {
        event.preventDefault();
        event.stopPropagation();
        setSelectedNodeId(node.id);
        setNodeMenuPosition({ x: event.clientX, y: event.clientY });
        setMenuPosition(null);
    }, []);

    const onPaneClick = useCallback(() => {
        setSelectedEdgeId(null);
        setMenuPosition(null);
        if (isConnecting) {
            setIsConnecting(false);
            setSourceNodeId(null);
            setNodes((nds) => nds.map((n) => ({ ...n, data: { ...n.data, isSource: false } })));
        }
    }, [isConnecting, setNodes]);

    const onNodeClick = useCallback((event: React.MouseEvent, node: Node) => {
        if (!isConnecting) return;

        if (!sourceNodeId) {
            setSourceNodeId(node.id);
            setNodes((nds) => nds.map((n) => ({
                ...n,
                data: { ...n.data, isSource: n.id === node.id }
            })));
        } else {
            // Allow Self-Loops (previously blocked)
            // Create Connection
            const newEdge: Edge = {
                id: `e - ${sourceNodeId} -${node.id} -${Date.now()} `,
                source: sourceNodeId,
                target: node.id,
                type: 'noxRelation',
                data: { state: 'placeholder', shape: 'step', label: 'Relation' }
            };

            setEdges((eds) => eds.concat(newEdge));

            // CONTINUOUS MODE: Do NOT turn off isConnecting
            // Reset source so user can pick next source
            setSourceNodeId(null);

            // Clear Visuals (Source Highlight)
            setNodes((nds) => nds.map((n) => ({ ...n, data: { ...n.data, isSource: false } })));
        }
    }, [isConnecting, sourceNodeId, setEdges, setNodes]);

    const onDragOver = useCallback((event: React.DragEvent) => {
        event.preventDefault();
        event.dataTransfer.dropEffect = 'move';
    }, []);

    const onDrop = useCallback((event: React.DragEvent) => {
        event.preventDefault();
        const type = event.dataTransfer.getData('application/reactflow');
        if (!type) return;

        const position = reactFlowInstance.screenToFlowPosition({
            x: event.clientX,
            y: event.clientY,
        });

        const newStateId = getId();
        const newNode: Node = {
            id: newStateId,
            type: 'noxBlock',
            position,
            data: {
                id: newStateId,
                label: `New Block`,
                status: { state: 'running' },
                visual: { variant: 'default', icon: 'box', color: 'zinc' }
            },
        };

        setNodes((nds) => nds.concat(newNode));

        // Also update MOCK storage
        if (MOCK_DATA[currentViewId]) {
            MOCK_DATA[currentViewId].nodes.push(newNode);
        }
    },
        [reactFlowInstance, setNodes, currentViewId]
    );

    return (
        <div className="flex h-screen w-screen bg-[#09090B] text-white overflow-hidden flex-col">
            {/* Breadcrumb Bar */}
            <div className="h-10 bg-zinc-900 border-b border-zinc-800 flex items-center px-4 gap-2 text-sm z-50 shadow-sm relative">
                {path.map((item, index) => (
                    <React.Fragment key={item.id}>
                        {index > 0 && <ChevronRight size={14} className="text-zinc-600" />}
                        <button
                            onClick={() => navigateTo(item.id, index)}
                            className={`flex items - center gap - 2 hover: text - white transition - colors ${index === path.length - 1 ? 'text-white font-semibold' : 'text-zinc-500'} `}
                        >
                            {item.id === 'root' && <Home size={14} />}
                            <span className="uppercase tracking-wider text-xs">{item.label}</span>
                        </button>
                    </React.Fragment>
                ))}
            </div>

            {/* Family Tree Toggle (Top Right) */}
            <div className="absolute top-14 right-4 z-[60]">
                <button
                    onClick={() => setShowFamilyTree(!showFamilyTree)}
                    className={`
                        flex items-center gap-2 px-3 py-2 rounded-lg text-xs font-bold uppercase tracking-wider border transition-all
                        ${showFamilyTree
                            ? 'bg-blue-500/20 text-blue-400 border-blue-500 shadow-[0_0_15px_rgba(59,130,246,0.5)]'
                            : 'bg-zinc-900/80 text-zinc-500 border-zinc-700 hover:border-zinc-500 hover:text-zinc-300'
                        }
                    `}
                >
                    <Share2 size={14} className={showFamilyTree ? 'animate-pulse' : ''} />
                    {showFamilyTree ? 'Close S-View' : 'Structural View'}
                </button>
            </div>

            {/* Structural View Overlay */}
            <FamilyTreeOverlay isOpen={showFamilyTree} onClose={() => setShowFamilyTree(false)} />

            <div className={`flex flex-grow overflow-hidden transition-all duration-500 ${showFamilyTree ? 'blur-sm scale-95 opacity-30 grayscale' : ''}`}>
                <Sidebar />

                <div className="flex-grow h-full relative" ref={reactFlowWrapper}>
                    <ReactFlow
                        nodes={nodes}
                        edges={edges}
                        onNodesChange={onNodesChange}
                        onEdgesChange={onEdgesChange}
                        onEdgeClick={onEdgeClick}
                        onEdgeContextMenu={onEdgeContextMenu}
                        onNodeClick={onNodeClick}
                        onNodeContextMenu={onNodeContextMenu}
                        onNodeDoubleClick={onNodeDoubleClick}
                        onPaneClick={onPaneClick}
                        onInit={setReactFlowInstance}
                        onDrop={onDrop}
                        onDragOver={onDragOver}
                        nodeTypes={nodeTypes}
                        edgeTypes={edgeTypes}
                        proOptions={{ hideAttribution: true }}
                        fitView
                        className={isConnecting ? 'cursor-crosshair' : ''}
                    >
                        <Background color="#222" gap={20} size={1} />
                        <Controls className="!bg-zinc-800 !border-zinc-700 [&>button]:!fill-zinc-400 [&>button:hover]:!bg-zinc-700" />

                        {/* Header Panel (Floating) */}
                        <Panel position="top-center" className="bg-zinc-900/80 backdrop-blur border border-zinc-700 p-2 rounded-full flex gap-4 shadow-xl">
                            <button
                                className="flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-bold bg-transparent text-zinc-400 hover:text-white hover:bg-zinc-800 transition-all border border-transparent hover:border-zinc-700"
                                onClick={() => {
                                    const id = getId();
                                    const position = reactFlowInstance.project({
                                        x: window.innerWidth / 2 - 200, // Offset slightly to account for sidebar
                                        y: window.innerHeight / 2
                                    });

                                    const newNode: Node = {
                                        id,
                                        type: 'noxRouter',
                                        position,
                                        data: { label: 'Router' }
                                    };

                                    setNodes((nds) => nds.concat(newNode));

                                    // Add to mock data if needed
                                    if (MOCK_DATA[currentViewId]) {
                                        MOCK_DATA[currentViewId].nodes.push(newNode);
                                    }
                                }}
                            >
                                <CircleDot size={14} />
                                <span>Router</span>
                            </button>

                            <div className="w-px h-6 bg-zinc-700" />

                            <button
                                className={`flex items - center gap - 2 px - 3 py - 1.5 rounded - full text - xs font - bold transition - all ${isConnecting
                                    ? 'bg-blue-500 text-white shadow-[0_0_10px_rgba(59,130,246,0.5)]'
                                    : 'bg-transparent text-zinc-400 hover:text-white hover:bg-zinc-800'
                                    } `}
                                onClick={() => {
                                    if (isConnecting) {
                                        setSourceNodeId(null);
                                        setNodes((nds) => nds.map((n) => ({ ...n, data: { ...n.data, isSource: false } })));
                                    }
                                    setIsConnecting(!isConnecting);
                                }}
                            >
                                <Zap size={14} className={isConnecting ? 'fill-white' : ''} />
                                {isConnecting ? 'CONNECTING (ON)' : 'Connect'}
                            </button>
                        </Panel>

                        <EdgeContextMenu
                            edge={selectedEdge}
                            position={menuPosition}
                            onChange={updateEdgeData}
                            onDelete={deleteEdge}
                            onClose={() => setMenuPosition(null)}
                        />

                        <NodeContextMenu
                            node={selectedNode}
                            position={nodeMenuPosition}
                            onChange={updateNodeData}
                            onDelete={deleteNode}
                            onClose={() => setNodeMenuPosition(null)}
                        />

                    </ReactFlow>
                </div>
            </div>
        </div>
    );
};

const NodeContextMenu = ({
    node,
    position,
    onChange,
    onDelete,
    onClose
}: {
    node: Node | null,
    position: { x: number, y: number } | null,
    onChange: (id: string, data: any) => void,
    onDelete: (id: string) => void,
    onClose: () => void
}) => {
    if (!node || !position) return null;
    const data = node.data || {};

    // Only for NoxRouter for now, or generic?
    // User asked specifically for "node relation" (Router) management
    if (node.type !== 'noxRouter') return null;

    return (
        <div
            className="fixed w-48 bg-zinc-900 border border-zinc-700 rounded-lg shadow-2xl p-2 z-[9999] flex flex-col gap-2 animate-in fade-in zoom-in-95 duration-100"
            style={{ top: position.y, left: position.x }}
            onContextMenu={(e) => e.preventDefault()}
        >
            <div className="flex items-center justify-between px-2 pb-2 border-b border-zinc-800">
                <span className="text-[10px] font-bold text-zinc-400 uppercase tracking-wider">Router Settings</span>
                <button onClick={onClose} className="text-zinc-500 hover:text-zinc-300 text-xs">✕</button>
            </div>

            {/* Label Input */}
            <div className="px-1">
                <input
                    type="text"
                    placeholder="Label..."
                    className="w-full bg-zinc-950 border border-zinc-800 rounded px-2 py-1 text-xs text-zinc-300 focus:outline-none focus:border-blue-500/50"
                    defaultValue={data.label === 'Router' ? '' : data.label}
                    onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                            onChange(node.id, { ...data, label: e.currentTarget.value || 'Router' });
                            e.currentTarget.blur();
                        }
                    }}
                    onBlur={(e) => onChange(node.id, { ...data, label: e.target.value || 'Router' })}
                />
            </div>

            {/* Actions */}
            <div className="pt-2 border-t border-zinc-800 flex items-center justify-between gap-2">
                <button
                    onClick={() => onDelete(node.id)}
                    className="flex-1 py-1.5 text-[10px] rounded font-medium bg-red-500/10 text-red-400 hover:bg-red-500/20 flex items-center justify-center gap-1"
                >
                    Delete Node
                </button>
            </div>
        </div>
    );
}

export const DesignNOX = () => {
    return (
        <ErrorBoundary>
            <ReactFlowProvider>
                <DesignCanvas />
            </ReactFlowProvider>
        </ErrorBoundary>
    );
};
