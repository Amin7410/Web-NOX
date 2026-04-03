import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { Node, Edge, OnNodesChange, OnEdgesChange, applyNodeChanges, applyEdgeChanges, NodeChange, EdgeChange, Connection, addEdge, MarkerType } from 'reactflow';
import { StudioState, SavedBlock, NoxNodeData, NavigationStep } from '../types/studio';

const StudioContext = createContext<StudioState | undefined>(undefined);

const LS_KEY = 'nox-studio-library';

export const StudioProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [savedBlocks, setSavedBlocks] = useState<SavedBlock[]>([]);
  const [nodes, setNodes] = useState<Node<NoxNodeData>[]>([]);
  const [edges, setEdges] = useState<Edge[]>([]);
  const [navigationPath, setNavigationPath] = useState<NavigationStep[]>([
    { id: 'root', label: 'Root' }
  ]);

  const [isConnectMode, setIsConnectMode] = useState(false);
  const [edgeColor, setEdgeColor] = useState('rgba(99, 102, 241, 0.6)');

  // Persistence logic
  useEffect(() => {
    const stored = localStorage.getItem(LS_KEY);
    if (stored) {
      try {
        setSavedBlocks(JSON.parse(stored));
      } catch (e) {
        console.error('Failed to parse Studio Library', e);
      }
    }
  }, []);

  useEffect(() => {
    localStorage.setItem(LS_KEY, JSON.stringify(savedBlocks));
  }, [savedBlocks]);

  // Hierarchical Helpers
  const getDescendants = useCallback((parentId: string) => {
    const descendants: Node<NoxNodeData>[] = [];
    const stack = [parentId];
    while (stack.length > 0) {
      const currentId = stack.pop();
      const children = nodes.filter(n => n.data.parentId === currentId);
      descendants.push(...children);
      stack.push(...children.map(c => c.id));
    }
    return descendants;
  }, [nodes]);

  const saveBlock = useCallback((nodeData: NoxNodeData, nodeId?: string) => {
    let children: Node<NoxNodeData>[] = [];
    let internalEdges: Edge[] = [];
    if (nodeId) {
      children = getDescendants(nodeId);
      const childIds = new Set([nodeId, ...children.map(c => c.id)]);
      internalEdges = edges.filter(e => childIds.has(e.source) && childIds.has(e.target));
    }
    const newBlock: SavedBlock = {
      id: `sb_${Date.now()}`,
      label: nodeData.label,
      type: nodeData.type,
      invaders: nodeData.invaders ? [...nodeData.invaders] : [],
      createdAt: Date.now(),
      childrenNodes: children.length > 0 ? children : undefined,
      internalEdges: internalEdges.length > 0 ? internalEdges : undefined
    };
    setSavedBlocks((prev) => [newBlock, ...prev]);
  }, [getDescendants, edges]);

  const removeSavedBlock = useCallback((id: string) => {
    setSavedBlocks((prev) => prev.filter((b) => b.id !== id));
  }, []);

  // Navigation Logic
  const enterNode = useCallback((id: string, label: string) => {
    setNavigationPath((prev) => [...prev, { id, label }]);
  }, []);

  const exitToStep = useCallback((index: number) => {
    setNavigationPath((prev) => prev.slice(0, index + 1));
  }, []);

  const teleportToNode = useCallback((nodeId: string) => {
    const target = nodes.find(n => n.id === nodeId);
    if (!target) return;

    const newPath: NavigationStep[] = [];
    let currentParentId = target.data.parentId;

    while (currentParentId) {
      const parentNode = nodes.find(n => n.id === currentParentId);
      if (parentNode) {
        newPath.unshift({ id: parentNode.id, label: parentNode.data.label });
        currentParentId = parentNode.data.parentId;
      } else {
        break;
      }
    }

    newPath.unshift({ id: 'root', label: 'Root' });
    setNavigationPath(newPath);
    
    setTimeout(() => {
        const event = new CustomEvent('nox-focus-node', { detail: { nodeId } });
        window.dispatchEvent(event);
    }, 100);
  }, [nodes]);

  const currentParentId = useMemo(() => {
    const last = navigationPath[navigationPath.length - 1];
    return last.id === 'root' ? null : last.id;
  }, [navigationPath]);

  // Style Orchestration
  const updateEdgeStyle = useCallback((edgeId: string, style: { color?: string, dashed?: boolean }) => {
    setEdges((eds) => eds.map((e) => {
      if (e.id === edgeId) {
        return { 
          ...e, 
          style: { 
            ...e.style, 
            stroke: style.color || e.style?.stroke,
            strokeDasharray: style.dashed ? '5,5' : (style.dashed === false ? '0' : e.style?.strokeDasharray)
          },
          data: { ...e.data, dashed: style.dashed !== undefined ? style.dashed : e.data?.dashed }
        };
      }
      return e;
    }));
  }, []);

  const updateNodeOutputStyle = useCallback((nodeId: string, style: { color?: string, dashed?: boolean }) => {
    setEdges((eds) => eds.map((e) => {
      if (e.source === nodeId) {
        return { 
          ...e, 
          style: { 
            ...e.style, 
            stroke: style.color || e.style?.stroke,
            strokeDasharray: style.dashed ? '5,5' : (style.dashed === false ? '0' : e.style?.strokeDasharray)
          },
          data: { ...e.data, dashed: style.dashed !== undefined ? style.dashed : e.data?.dashed }
        };
      }
      return e;
    }));
  }, []);

  const dispatchNodesChange = useCallback((changes: NodeChange[]) => {
    setNodes((nds) => applyNodeChanges(changes, nds));
  }, []);

  const dispatchEdgesChange = useCallback((changes: EdgeChange[]) => {
    setEdges((eds) => applyEdgeChanges(changes, eds));
  }, []);

  const onConnect = useCallback((connection: Connection) => {
    // Find source node to inherit its default output style if possible
    setEdges((eds) => {
      const id = `edge_${Date.now()}_${Math.random().toString(36).substr(2, 5)}`;
      return addEdge({
        ...connection,
        id,
        type: 'noxEdge',
        data: { waypoints: [] },
        style: { stroke: edgeColor, strokeWidth: 2 },
        markerEnd: { type: MarkerType.ArrowClosed, color: edgeColor }
      }, eds);
    });
  }, [edgeColor]);

  const updateEdgeWaypoint = useCallback((edgeId: string, index: number, position: { x: number, y: number }) => {
    setEdges((eds) => eds.map((e) => {
      if (e.id === edgeId) {
        const waypoints = [...(e.data?.waypoints || [])];
        waypoints[index] = position;
        return { ...e, data: { ...e.data, waypoints } };
      }
      return e;
    }));
  }, []);

  const addEdgeWaypoint = useCallback((edgeId: string, position: { x: number, y: number }) => {
    setEdges((eds) => eds.map((e) => {
      if (e.id === edgeId) {
        const waypoints = [...(e.data?.waypoints || []), position];
        return { ...e, data: { ...e.data, waypoints } };
      }
      return e;
    }));
  }, []);

  return (
    <StudioContext.Provider value={{ 
      savedBlocks, saveBlock, removeSavedBlock,
      navigationPath, enterNode, exitToStep, currentParentId,
      nodes, setNodes, edges, setEdges,
      onNodesChange: dispatchNodesChange,
      onEdgesChange: dispatchEdgesChange,
      onConnect,
      isConnectMode, setIsConnectMode,
      edgeColor, setEdgeColor,
      updateEdgeWaypoint,
      addEdgeWaypoint,
      teleportToNode,
      updateEdgeStyle,
      updateNodeOutputStyle
    }}>
      {children}
    </StudioContext.Provider>
  );
};

export const useStudio = () => {
  const context = useContext(StudioContext);
  if (context === undefined) {
    throw new Error('useStudio must be used within a StudioProvider');
  }
  return context;
};
