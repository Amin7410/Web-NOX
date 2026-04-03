import React, { useState, useCallback, useRef, useMemo, useEffect } from 'react';
import ReactFlow, { 
    Background, 
    Controls, 
    MiniMap,
    addEdge,
    Edge,
    Node,
    ReactFlowInstance,
    Panel,
    applyNodeChanges,
    applyEdgeChanges,
    NodeChange,
    EdgeChange
} from 'reactflow';
import * as Lucide from 'lucide-react';
import { AnimatePresence, motion } from 'framer-motion';
import BaseBlockNode from './nodes/BaseBlockNode';
import NoxRouter from './nodes/NoxRouter';
import NoxRelation from './edges/NoxRelation';
import Inspector from '../components/Inspector';
import ContextMenu from '../components/ContextMenu';
import { FamilyTreeOverlay } from '../components/FamilyTreeOverlay';
import TemplateLibrary from '../components/TemplateLibrary';
import { useWorkspace } from '../hooks/useWorkspace';
import { blockService, relationService } from '../services/studioService';

const Icons = Lucide as any;

const nodeTypes = {
    baseBlock: BaseBlockNode,
    noxRouter: NoxRouter
};

const edgeTypes = {
    noxRelation: NoxRelation
};

const MAX_DEPTH = 10;

// ── Props ─────────────────────────────────────
interface StudioCanvasProps {
    workspaceId?: string | null;
}

export default function StudioCanvas({ workspaceId = null }: StudioCanvasProps) {
    const reactFlowWrapper = useRef<HTMLDivElement>(null);
    const [reactFlowInstance, setReactFlowInstance] = useState<ReactFlowInstance | null>(null);
    
    // ── Backend data load ─────────────────────
    const { nodes: remoteNodes, edges: remoteEdges, status: loadStatus, error: loadError } = useWorkspace(workspaceId);

    // NAVIGATION STATE
    const [currentViewId, setCurrentViewId] = useState<string>('root');
    const [internalPath, setInternalPath] = useState<{ id: string, label: string }[]>([{ id: 'root', label: 'Engine' }]);
    
    // HIERARCHY DATA STORAGE (local - includes remote data + sub-levels)
    const [hierarchy, setHierarchy] = useState<Record<string, { nodes: Node[], edges: Edge[] }>>({ root: { nodes: [], edges: [] } });

    // FLOW DATA STATE (Controlled by active view)
    const [nodes, setNodes] = useState<Node[]>([]);
    const [edges, setEdges] = useState<Edge[]>([]);

    // Sync remote data into hierarchy root when backend responds
    useEffect(() => {
        if (loadStatus === 'success') {
            setHierarchy(prev => ({ ...prev, root: { nodes: remoteNodes, edges: remoteEdges } }));
            if (currentViewId === 'root') {
                setNodes(remoteNodes);
                setEdges(remoteEdges);
            }
        }
    }, [loadStatus, remoteNodes, remoteEdges]);

    const [selectedId, setSelectedId] = useState<string | undefined>(undefined);
    const [selectionType, setSelectionType] = useState<'node' | 'edge' | null>(null);
    const [isConnecting, setIsConnecting] = useState(false);
    const [sourceNodeId, setSourceNodeId] = useState<string | null>(null);
    const [showStructuralView, setShowStructuralView] = useState(false);
    const [showTemplateLibrary, setShowTemplateLibrary] = useState(false);
    const [saveStatus, setSaveStatus] = useState<'idle' | 'saving' | 'saved'>('idle');
    const [contextMenu, setContextMenu] = useState<{ x: number, y: number, type: 'node' | 'edge' | 'canvas', id?: string } | null>(null);

    const onNodesChange = useCallback((changes: NodeChange[]) => {
        setNodes((nds) => {
            const result = applyNodeChanges(changes, nds);
            // Sync to hierarchy immediately
            setHierarchy(prev => ({
                ...prev,
                [currentViewId]: { ...prev[currentViewId], nodes: result }
            }));
            return result;
        });
    }, [currentViewId]);

    const onEdgesChange = useCallback((changes: EdgeChange[]) => {
        setEdges((eds) => {
            const result = applyEdgeChanges(changes, eds);
             // Sync to hierarchy immediately
             setHierarchy(prev => ({
                ...prev,
                [currentViewId]: { ...prev[currentViewId], edges: result }
            }));
            return result;
        });
    }, [currentViewId]);

    const selectedObject = useMemo(() => {
        if (selectionType === 'node') return nodes.find(n => n.id === selectedId);
        if (selectionType === 'edge') return edges.find(e => e.id === selectedId);
        return null;
    }, [nodes, edges, selectedId, selectionType]);

    // DRILL DOWN (Nesting mechanism)
    const onNodeDoubleClick = useCallback((_event: React.MouseEvent, node: Node) => {
        if (internalPath.length >= MAX_DEPTH) {
            alert("Maximum system depth (10) reached.");
            return;
        }

        const viewId = node.id;
        const viewLabel = node.data.label || 'Sub-Block';

        // Initialize view if not exists
        const targetData = hierarchy[viewId] || { nodes: [], edges: [] };
        
        setCurrentViewId(viewId);
        setInternalPath(prev => [...prev, { id: viewId, label: viewLabel }]);
        setNodes(targetData.nodes);
        setEdges(targetData.edges);
        setSelectedId(undefined);
    }, [hierarchy, internalPath]);

    const navigateTo = (viewId: string, index: number) => {
        const targetData = hierarchy[viewId] || { nodes: [], edges: [] };
        setCurrentViewId(viewId);
        setInternalPath(prev => prev.slice(0, index + 1));
        setNodes(targetData.nodes);
        setEdges(targetData.edges);
        setSelectedId(undefined);
    };

    // ZAP MODE (Connection trigger)
    const onNodeClick = useCallback((_event: React.MouseEvent, node: Node) => {
        if (!isConnecting) {
            setSelectedId(node.id);
            setSelectionType('node');
            return;
        }
        if (!sourceNodeId) {
            setSourceNodeId(node.id);
            setNodes((nds) => nds.map((n) => n.id === node.id ? { ...n, data: { ...n.data, isConnectingSource: true } } : n));
        } else {
            const tempId = `e-${sourceNodeId}-${node.id}-${Date.now()}`;
            const newEdge: Edge = {
                id: tempId,
                source: sourceNodeId,
                target: node.id,
                type: 'noxRelation',
                sourceHandle: 'source',
                targetHandle: 'target',
                data: { label: 'Route', shape: 'step', color: '#ffffff', animating: true }
            };
            setEdges((eds) => addEdge(newEdge, eds));

            // Persist to backend if in a real workspace
            if (workspaceId) {
                relationService.create(workspaceId, {
                    sourceBlockId: sourceNodeId,
                    targetBlockId: node.id,
                    type: 'DEFAULT',
                    visual: { shape: 'step', color: '#ffffff', animating: true },
                }).then(saved => {
                    // Replace temp id with backend id
                    setEdges(eds => eds.map(e => e.id === tempId ? { ...e, id: saved.id, data: { ...e.data, backendId: saved.id } } : e));
                }).catch(console.error);
            }

            setSourceNodeId(null);
            setNodes((nds) => nds.map((n) => ({ ...n, data: { ...n.data, isConnectingSource: false } })));
        }
    }, [isConnecting, sourceNodeId, workspaceId]);

    const onEdgeClick = useCallback((_event: React.MouseEvent, edge: Edge) => {
        setSelectedId(edge.id);
        setSelectionType('edge');
    }, []);

    const onPaneClick = useCallback(() => {
        setSelectedId(undefined);
        setSelectionType(null);
        setContextMenu(null);
        if (isConnecting) {
            setIsConnecting(false);
            setSourceNodeId(null);
            setNodes((nds) => nds.map((n) => ({ ...n, data: { ...n.data, isConnectingSource: false } })));
        }
    }, [isConnecting]);

    const onNodeContextMenu = useCallback((event: React.MouseEvent, node: Node) => {
        event.preventDefault();
        setContextMenu({ x: event.clientX, y: event.clientY, type: 'node', id: node.id });
    }, []);

    const onEdgeContextMenu = useCallback((event: React.MouseEvent, edge: Edge) => {
        event.preventDefault();
        setContextMenu({ x: event.clientX, y: event.clientY, type: 'edge', id: edge.id });
    }, []);

    const onContextMenuAction = (action: string) => {
        if (!contextMenu) return;
        const { id, type } = contextMenu;
        if (action === 'delete') {
            if (type === 'node') {
                const node = nodes.find(n => n.id === id);
                setNodes((nds) => nds.filter((n) => n.id !== id));
                // Delete from backend
                if (workspaceId && id) {
                    blockService.delete(workspaceId, id).catch(console.error);
                }
            }
            if (type === 'edge') {
                const edge = edges.find(e => e.id === id);
                setEdges((eds) => eds.filter((e) => e.id !== id));
                // Delete from backend
                if (workspaceId && id) {
                    relationService.delete(workspaceId, id).catch(console.error);
                }
            }
        } else if (action === 'settings') {
            setSelectedId(id);
            setSelectionType(type as any);
        } else if (action === 'router' && contextMenu) {
            spawnRouter(contextMenu.x, contextMenu.y, true);
        }
        setContextMenu(null);
    };

    const spawnRouter = (x?: number, y?: number, fromScreen = false) => {
        if (!reactFlowInstance) return;
        const position = fromScreen && x && y 
            ? reactFlowInstance.screenToFlowPosition({ x, y })
            : { x: (nodes[0]?.position.x || 200) + 100, y: (nodes[0]?.position.y || 200) + 100 };
        const newNode: Node = {
            id: `router-${Date.now()}`,
            type: 'noxRouter',
            position,
            data: { label: 'Router', color: '#ffffff' },
        };
        setNodes((nds) => nds.concat(newNode));
    };

    const onDragOver = useCallback((event: any) => {
        console.log('Drag over event triggered');
        event.preventDefault();
        
        // Check what type of drag is happening
        const hasInvader = event.dataTransfer.types.includes('application/invader');
        const hasReactFlow = event.dataTransfer.types.includes('application/reactflow');
        
        if (hasInvader) {
            event.dataTransfer.dropEffect = 'copy';
        } else if (hasReactFlow) {
            event.dataTransfer.dropEffect = 'move';
        } else {
            event.dataTransfer.dropEffect = 'copy';
        }
        
        if (!reactFlowInstance) {
            console.log('No reactFlowInstance in onDragOver');
            return;
        }
        const position = reactFlowInstance.screenToFlowPosition({ x: event.clientX, y: event.clientY });

        setNodes((nds) => nds.map(n => {
            if (n.type !== 'baseBlock') return n;
            const nw = (n as any).width || 260;
            const nh = (n as any).height || 140;
            const isHovered = (
                position.x >= n.position.x && 
                position.x <= n.position.x + nw && 
                position.y >= n.position.y && 
                position.y <= n.position.y + nh
            );
            return { ...n, data: { ...n.data, isInvaderHovered: isHovered } };
        }));
    }, [reactFlowInstance]);

    const onDrop = useCallback(
        (event: any) => {
            event.preventDefault();
            console.log('Drop event triggered');
            
            const invaderType = event.dataTransfer.getData('application/invader');
            const blockType = event.dataTransfer.getData('application/reactflow');
            
            console.log('Drop data:', { invaderType, blockType });
            
            if (!reactFlowInstance) {
                console.log('No reactFlowInstance');
                return;
            }
            const position = reactFlowInstance.screenToFlowPosition({ x: event.clientX, y: event.clientY });
            console.log('Drop position:', position);

            // 1. Check for Invader Module drop
            if (invaderType && invaderType.length > 0) {
                 setNodes((nds) => {
                    const targetNode = nds.find(n => {
                        if (n.type !== 'baseBlock') return false;
                        const nw = (n as any).width || 260;
                        const nh = (n as any).height || 140;
                        return (
                            position.x >= n.position.x - 20 && 
                            position.x <= n.position.x + nw + 20 && 
                            position.y >= n.position.y - 10 && 
                            position.y <= n.position.y + nh + 10
                        );
                    });

                    if (targetNode) {
                         return nds.map(n => {
                            if (n.id === targetNode.id) {
                                return { 
                                    ...n, 
                                    data: { 
                                        ...n.data, 
                                        invaders: [...(n.data.invaders || []), { type: invaderType, settings: {} }],
                                        isInvaderHovered: false,
                                        isSuccess: true 
                                    } 
                                };
                            }
                            return { ...n, data: { ...n.data, isInvaderHovered: false } };
                        });
                    }
                    return nds.map(n => ({ ...n, data: { ...n.data, isInvaderHovered: false } }));
                });

                setTimeout(() => {
                    setNodes((nds) => nds.map(n => ({ ...n, data: { ...n.data, isSuccess: false } })));
                }, 800);
                return;
            }

            // 2. Check for New Node drop
            if (blockType && blockType.length > 0) {
                const isRouter = blockType === 'router';
                const label = isRouter ? 'Router' : `${blockType.charAt(0).toUpperCase() + blockType.slice(1)} Block`;
                const tempId = `node-${Date.now()}`;
                const newNode: Node = {
                    id: tempId,
                    type: isRouter ? 'noxRouter' : 'baseBlock',
                    position,
                    data: {
                        label,
                        description: 'New system module spawned.',
                        invaders: [],
                        visual: { positionX: position.x, positionY: position.y },
                        depth: internalPath.length
                    },
                };
                setNodes((nds) => nds.concat(newNode));

                // Persist to backend
                if (workspaceId) {
                    blockService.create(workspaceId, {
                        name: label,
                        type: blockType.toUpperCase(),
                        config: { description: 'New system module spawned.', invaders: [] },
                        visual: { positionX: position.x, positionY: position.y },
                    }).then(saved => {
                        // Replace temp id with real backend id
                        setNodes(nds => nds.map(n => n.id === tempId
                            ? { ...n, id: saved.id, data: { ...n.data, backendId: saved.id } }
                            : n
                        ));
                    }).catch(console.error);
                }
            }
        },
        [reactFlowInstance, internalPath, workspaceId]
    );

    const updateObjectData = (id: string, newData: any) => {
        if (selectionType === 'node') {
            setNodes((nds) => nds.map(n => n.id === id ? { ...n, data: newData } : n));
            // Persist label/config changes
            if (workspaceId) {
                blockService.update(workspaceId, id, {
                    name: newData.label,
                    config: { description: newData.description, invaders: newData.invaders },
                    visual: newData.visual,
                }).catch(console.error);
            }
        }
        if (selectionType === 'edge') {
            setEdges((eds) => eds.map(e => e.id === id ? { ...e, data: newData } : e));
            // Persist edge visual (color, shape, animation)
            if (workspaceId) {
                relationService.update(workspaceId, id, {
                    visual: { color: newData.color, shape: newData.shape, animating: newData.animating },
                    rules: { label: newData.label },
                }).catch(console.error);
            }
        }
    };

    return (
        <div className="flex flex-col h-full w-full overflow-hidden relative bg-zinc-950" ref={reactFlowWrapper}>
            {/* LOADING OVERLAY */}
            {loadStatus === 'loading' && (
                <div className="absolute inset-0 z-[200] bg-zinc-950/80 backdrop-blur-sm flex items-center justify-center">
                    <div className="flex flex-col items-center gap-3">
                        <Icons.Loader2 size={24} className="text-indigo-400 animate-spin" />
                        <p className="text-[10px] font-black uppercase tracking-widest text-zinc-500">Loading Workspace...</p>
                    </div>
                </div>
            )}
            {loadStatus === 'error' && (
                <div className="absolute top-12 left-1/2 -translate-x-1/2 z-[200] bg-red-900/20 border border-red-500/30 px-4 py-2 rounded-sm flex items-center gap-2">
                    <Icons.AlertTriangle size={14} className="text-red-400" />
                    <p className="text-[10px] font-bold text-red-400">{loadError ?? 'Failed to connect to backend'}</p>
                </div>
            )}
            {/* HIERARCHICAL BREADCRUMB */}
            <div className="h-10 border-b border-zinc-800/80 bg-zinc-950/50 backdrop-blur-md flex items-center px-6 gap-3 z-50 overflow-x-auto no-scrollbar shadow-xl">
                {internalPath.map((it, idx) => (
                    <React.Fragment key={it.id}>
                        {idx > 0 && <Icons.ChevronRight size={12} className="text-zinc-700" />}
                        <button 
                            onClick={() => navigateTo(it.id, idx)}
                            className={`
                                flex items-center gap-2 group transition-all shrink-0
                                ${idx === internalPath.length - 1 ? 'text-zinc-200 pointer-events-none' : 'text-zinc-500 hover:text-white'}
                            `}
                        >
                            {idx === 0 ? <Icons.Home size={13} /> : <Icons.Box size={13} className="opacity-40" />}
                            <span className={`text-[10px] font-black uppercase tracking-[0.15em] ${idx === internalPath.length - 1 ? 'animate-in fade-in slide-in-from-left-1 duration-300' : ''}`}>
                                {it.label}
                            </span>
                        </button>
                    </React.Fragment>
                ))}
            </div>

            <div 
                className={`flex-1 relative h-full transition-all duration-500 ${showStructuralView ? 'blur-sm scale-[0.98] opacity-20 grayscale pointer-events-none' : ''}`}
            >
                <ReactFlow
                    nodes={nodes}
                    edges={edges}
                    onNodesChange={onNodesChange}
                    onEdgesChange={onEdgesChange}
                    onConnect={(p) => setEdges((eds) => addEdge({ ...p, type: 'noxRelation', sourceHandle: 'source', targetHandle: 'target', data: { shape: 'step', color: '#6366f1' } }, eds))}
                    onNodeClick={onNodeClick}
                    onNodeDoubleClick={onNodeDoubleClick}
                    onEdgeClick={onEdgeClick}
                    onPaneClick={onPaneClick}
                    onNodeContextMenu={onNodeContextMenu}
                    onEdgeContextMenu={onEdgeContextMenu}
                    onInit={setReactFlowInstance}
                    onDrop={onDrop}
                    onDragOver={onDragOver}
                    nodeTypes={nodeTypes}
                    edgeTypes={edgeTypes}
                    fitView
                    nodesConnectable={false}
                    className="bg-transparent"
                    style={{ zIndex: 1 }}
                >
                    <Background color="#111" gap={20} size={1} />
                    
                    <Panel position="top-center" className="bg-zinc-900/95 backdrop-blur-3xl border border-zinc-800/80 rounded-sm px-5 py-2.5 flex items-center gap-5 shadow-2xl z-50 mt-4 translate-y-2 border-t-zinc-700/50">
                        {/* Template Library Button */}
                        <button 
                            onClick={() => setShowTemplateLibrary(true)}
                            className="flex items-center gap-2.5 px-3 py-1.5 rounded-sm hover:bg-zinc-800 text-zinc-500 hover:text-indigo-400 transition-all text-[11px] font-black uppercase tracking-widest border border-transparent hover:border-zinc-700"
                        >
                            <Icons.Archive size={14} />
                            <span>Template Library</span>
                        </button>

                        <div className="w-px h-5 bg-zinc-800" />

                        <button 
                            onClick={() => spawnRouter()}
                            className="flex items-center gap-2.5 px-3 py-1.5 rounded-sm hover:bg-zinc-800 text-zinc-500 hover:text-white transition-all text-[11px] font-black uppercase tracking-widest border border-transparent hover:border-zinc-700 shadow-inner"
                        >
                            <Icons.CircleDot size={14} />
                            <span>Tạo Nút Giao</span>
                        </button>
                        
                        <div className="w-px h-5 bg-zinc-800" />
                        
                        <button 
                            onClick={() => setIsConnecting(!isConnecting)}
                            className={`
                                flex items-center gap-3 px-6 py-2 rounded-sm transition-all text-[11px] font-black uppercase tracking-widest border
                                ${isConnecting 
                                    ? 'bg-indigo-600 border-indigo-500 text-white shadow-[0_0_30px_rgba(99,102,241,0.4)]' 
                                    : 'bg-transparent border-transparent text-zinc-500 hover:text-zinc-200 hover:bg-zinc-800'}
                            `}
                        >
                            <Icons.Zap size={15} className={isConnecting ? 'fill-current' : ''} />
                            {isConnecting ? 'Hủy Nối' : 'Chế độ Nối dây'}
                        </button>
                    </Panel>

                    <Controls className="!bg-zinc-900 !border-zinc-800 rounded-sm" />
                    <MiniMap nodeColor={(n: any) => n.type === 'noxRouter' ? '#ffffff' : '#6366f1'} className="!bg-zinc-900 !border-zinc-800" />
                </ReactFlow>
            </div>

            <ContextMenu 
                visible={!!contextMenu} 
                x={contextMenu?.x || 0} 
                y={contextMenu?.y || 0} 
                onClose={() => setContextMenu(null)}
                onAction={onContextMenuAction}
                type={contextMenu?.type || 'canvas'}
            />

            <AnimatePresence mode="wait">
                {selectedId && selectedObject && (
                    <motion.div
                        key="inspector-wrap"
                        initial={{ x: 380 }}
                        animate={{ x: 0 }}
                        exit={{ x: 380 }}
                        transition={{ type: 'spring', damping: 25, stiffness: 200 }}
                        className="fixed right-0 top-0 h-full z-[100]"
                    >
                        <Inspector 
                            type={selectionType as any}
                            selectedObject={selectedObject as any} 
                            onClose={() => { setSelectedId(undefined); setSelectionType(null); }}
                            onUpdate={(id, data) => updateObjectData(id, data)}
                        />
                    </motion.div>
                )}
            </AnimatePresence>
            <FamilyTreeOverlay isOpen={showStructuralView} onClose={() => setShowStructuralView(false)} />

            {/* Template Library Modal */}
            <AnimatePresence>
                {showTemplateLibrary && (
                    <TemplateLibrary
                        workspaceId={workspaceId}
                        onBlockAdded={(newNode) => setNodes(nds => [...nds, newNode])}
                        onClose={() => setShowTemplateLibrary(false)}
                    />
                )}
            </AnimatePresence>
        </div>
    );
}
