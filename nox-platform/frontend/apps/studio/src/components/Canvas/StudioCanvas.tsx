import React, { useMemo, useRef, useCallback, useState, useEffect } from 'react';
import ReactFlow, { 
  Background, 
  Controls, 
  MiniMap, 
  BackgroundVariant,
  Panel,
  Node,
  Edge,
  SelectionMode,
  ReactFlowInstance,
  MarkerType
} from 'reactflow';
import { Undo2, Redo2, ListTree } from 'lucide-react';
import { NoxNode } from './Nodes/NoxNode';
import { NoxJunctionNode } from './Nodes/NoxJunctionNode';
import { NoxTerminalNode } from './Nodes/NoxTerminalNode';
import { NoxInvaderHub } from './Nodes/NoxInvaderHub';
import NoxEdge from './UI/NoxEdge';
import { ContextMenu } from '../UI/ContextMenu';
import { useStudio } from '../../context/StudioContext';
import { useKeyboardShortcuts } from '../../hooks/useKeyboardShortcuts';
import { useCanvasDnD } from '../../hooks/useCanvasDnD';
import 'reactflow/dist/style.css';

const nodeTypes = {
  noxNode: NoxNode,
  noxJunction: NoxJunctionNode,
  noxInputTerminal: NoxTerminalNode,
  noxOutputTerminal: NoxTerminalNode,
  noxInvaderHub: NoxInvaderHub,
};

const edgeTypes = {
  noxEdge: NoxEdge,
};

export const StudioCanvas = () => {
  const reactFlowWrapper = useRef<HTMLDivElement>(null);
  const { 
    nodes, setNodes, onNodesChange,
    edges, setEdges, onEdgesChange,
    onConnect, currentParentId, enterNode, saveBlock,
    isConnectMode
  } = useStudio();
  
  const [reactFlowInstance, setReactFlowInstance] = useState<ReactFlowInstance | null>(null);
  const [menu, setMenu] = useState<{ top: number, left: number, nodeId?: string, edgeId?: string } | null>(null);

  // Filter nodes and edges for the current layer (Scoping)
  const scopedNodes = useMemo(() => 
    nodes.filter(node => (node.data.parentId || null) === currentParentId),
  [nodes, currentParentId]);

  const scopedEdges = useMemo(() => {
    const nodeIds = new Set(scopedNodes.map(n => n.id));
    return edges.filter(edge => nodeIds.has(edge.source) && nodeIds.has(edge.target));
  }, [edges, scopedNodes]);

  // Viewport Coordination: Handle Teleportation focusing
  useEffect(() => {
    const handleFocus = (event: any) => {
      const { nodeId } = event.detail;
      if (reactFlowInstance) {
        const targetNode = nodes.find(n => n.id === nodeId);
        if (targetNode) {
          reactFlowInstance.fitView({ 
            nodes: [targetNode], 
            duration: 800, 
            padding: 0.5 
          });
        }
      }
    };

    window.addEventListener('nox-focus-node', handleFocus);
    return () => window.removeEventListener('nox-focus-node', handleFocus);
  }, [reactFlowInstance, nodes]);

  // Hook: Keyboard Shortcuts
  useKeyboardShortcuts({
    onUndo: () => console.log('Undo triggered'),
    onRedo: () => console.log('Redo triggered'),
    onSearch: () => document.getElementById('global-search')?.focus()
  });

  // Hook: Drag and Drop
  const { onDragOver, onDrop } = useCanvasDnD({
    reactFlowInstance,
    reactFlowWrapper,
    setNodes
  });

  // Interaction: Double Click logic
  const onNodeDoubleClick = useCallback((event: React.MouseEvent, node: Node) => {
    if (['noxJunction', 'noxInputTerminal', 'noxOutputTerminal'].includes(node.type || '')) return; 
    enterNode(node.id, node.data.label);
  }, [enterNode]);

  // Context Menu logic (Generic for Nodes and Edges)
  const handleContextMenu = useCallback(
    (event: React.MouseEvent, node?: Node, edge?: Edge) => {
      event.preventDefault();
      event.stopPropagation();
      setMenu({
        top: event.clientY,
        left: event.clientX,
        nodeId: node?.id,
        edgeId: edge?.id
      });
    },
    [setMenu]
  );

  const closeMenu = useCallback(() => setMenu(null), []);

  const handleSaveBlock = useCallback(() => {
    if (menu?.nodeId) {
      const nodeToSave = nodes.find(n => n.id === menu.nodeId);
      if (nodeToSave) {
        saveBlock(nodeToSave.data, nodeToSave.id);
      }
    }
    closeMenu();
  }, [menu, nodes, saveBlock, closeMenu]);

  // Dynamic context menu actions
  const canvasActions = useMemo(() => {
    // If it's an edge, we might have different primary actions
    if (menu?.edgeId) {
      return [
        { 
          label: 'Xóa sợi dây', 
          onClick: () => {
             setEdges((eds) => eds.filter((e) => e.id !== menu.edgeId));
          }, 
          variant: 'danger' as const 
        }
      ];
    }

    const defaultActions = [
      { label: 'Lưu mẫu thiết kế', onClick: handleSaveBlock, variant: 'primary' as const },
      { 
        label: 'Xóa vật thể', 
        onClick: () => {
          if (menu?.nodeId) setNodes((nds) => nds.filter((n) => n.id !== menu.nodeId));
        }, 
        variant: 'danger' as const 
      },
    ];

    return defaultActions;
  }, [menu, nodes, handleSaveBlock, setNodes, setEdges]);

  const defaultEdgeOptions = useMemo(() => ({
    type: 'noxEdge',
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: 'rgba(99, 102, 241, 0.4)',
    },
    style: { strokeWidth: 2 }
  }), []);

  const miniMapStyle = useMemo(() => ({
    backgroundColor: '#09090B',
    borderRadius: '4px',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    overflow: 'hidden' as const,
    width: 160,
    height: 100,
    boxShadow: '0 4px 12px rgba(0,0,0,0.5)'
  }), []);

  return (
    <div 
      className="h-full w-full bg-[#09090B]" 
      ref={reactFlowWrapper}
      onClick={closeMenu}
    >
      <ReactFlow
        nodes={scopedNodes}
        edges={scopedEdges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        onInit={setReactFlowInstance}
        onDrop={onDrop}
        onDragOver={onDragOver}
        onNodeDoubleClick={onNodeDoubleClick}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        defaultEdgeOptions={defaultEdgeOptions}
        nodesConnectable={isConnectMode}
        onPaneContextMenu={(event) => {
          event.preventDefault();
          closeMenu();
        }}
        onNodeContextMenu={(event, node) => handleContextMenu(event, node)}
        onEdgeContextMenu={(event, edge) => handleContextMenu(event, undefined, edge)}
        panOnDrag={[2]}
        selectionOnDrag={true}
        selectionMode={SelectionMode.Partial}
        fitView
        aria-label="Studio Canvas"
      >
        <Background 
          variant={BackgroundVariant.Lines} 
          gap={40} 
          size={1} 
          color="rgba(255, 255, 255, 0.03)" 
        />
        
        <Controls 
          className="!bg-zinc-900 !border-white/20 !shadow-none !fill-white" 
          showInteractive={false}
          position="bottom-left"
        />

        <Panel position="bottom-left" className="ml-12 mb-0 flex gap-0.5 bg-zinc-900 border border-white/20 rounded-md overflow-hidden p-0.5">
          <button 
            title="Undo (Ctrl+Z)"
            className="p-1 px-2 text-zinc-400 hover:bg-white/5 hover:text-zinc-100 transition-colors border-r border-white/5"
            onClick={() => console.log('Undo')}
          >
            <Undo2 size={14} />
          </button>
          <button 
            title="Redo (Ctrl+Y)"
            className="p-1 px-2 text-zinc-400 hover:bg-white/5 hover:text-zinc-100 transition-colors"
            onClick={() => console.log('Redo')}
          >
            <Redo2 size={14} />
          </button>
        </Panel>
        
        <MiniMap 
          style={miniMapStyle}
          maskColor="rgba(0, 0, 0, 0.6)"
          nodeStrokeColor="rgba(255, 255, 255, 0.1)"
          nodeColor="#27272a"
          nodeStrokeWidth={3}
          zoomable
          pannable
        />
      </ReactFlow>

      {menu && (
        <ContextMenu 
          top={menu.top} 
          left={menu.left} 
          actions={canvasActions}
          onClose={closeMenu} 
          nodeId={menu.nodeId}
          edgeId={menu.edgeId}
        />
      )}

      <style dangerouslySetInnerHTML={{ __html: `
        .react-flow__controls button {
          background-color: #18181b !important;
          border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
          color: #a1a1aa !important;
          fill: currentColor !important;
        }
        .react-flow__controls button:hover {
          background-color: #27272a !important;
          color: #f4f4f5 !important;
        }
        .react-flow__minimap-mask {
          fill: rgba(0, 0, 0, 0.6) !important;
        }
        .react-flow__edge-path {
          stroke-dasharray: 0;
          animation: none;
        }
        .react-flow__selection {
          background: rgba(99, 102, 241, 0.1) !important;
          border: 1px solid rgba(99, 102, 241, 0.4) !important;
        }
        .react-flow__handle {
          opacity: ${isConnectMode ? 1 : 0};
          transition: all 0.3s ease;
        }
      `}} />
    </div>
  );
};
