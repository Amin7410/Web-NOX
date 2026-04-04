import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { Node, Edge, OnNodesChange, OnEdgesChange, applyNodeChanges, applyEdgeChanges, NodeChange, EdgeChange, Connection, addEdge, MarkerType } from 'reactflow';
import { StudioState, SavedBlock, NoxNodeData, NavigationStep, SavedInvader, InvaderInstance } from '../types/studio';
import { StudioApi } from '../services/studioApi';
import { apiClient } from '../services/apiClient';
import { v4 as uuidv4 } from 'uuid';

const StudioContext = createContext<StudioState | undefined>(undefined);

const LS_KEY = 'nox-studio-library';

export const StudioProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [savedBlocks, setSavedBlocks] = useState<SavedBlock[]>([]);
  const [nodes, setNodes] = useState<Node<NoxNodeData>[]>([]);
  const [edges, setEdges] = useState<Edge[]>([]);
  const [projectId, setProjectId] = useState<string | null>(null);
  const [workspaceId, setWorkspaceId] = useState<string | null>(null);
  
  const [navigationPath, setNavigationPath] = useState<NavigationStep[]>([
    { id: 'root', label: 'Root' }
  ]);

  const [isConnectMode, setIsConnectMode] = useState(false);
  const [edgeColor, setEdgeColor] = useState('rgba(99, 102, 241, 0.6)');

  // Soul Dashboard 2.0 State
  const [activeSoulNodeId, setActiveSoulNodeId] = useState<string | null>(null);
  const [isRightSidebarOpen, setIsRightSidebarOpen] = useState(false);
  const [savedInvaders, setSavedInvaders] = useState<SavedInvader[]>([]);

  const toggleRightSidebar = useCallback((open: boolean, nodeId?: string) => {
    setIsRightSidebarOpen(open);
    if (nodeId) setActiveSoulNodeId(nodeId);
    else if (!open) setActiveSoulNodeId(null);
  }, []);

  const addInvaderToNode = useCallback(async (nodeId: string) => {
    // 1. Tạo UUID thật ngay từ Frontend (Figma Style)
    const newInvaderId = uuidv4(); 
    const nextNum = Math.floor(Math.random() * 100).toString().padStart(2, '0');
    
    const newInvader: InvaderInstance = {
      id: newInvaderId,
      name: `Invader ${nextNum}`,
      type: 'logic',
      config: {}
    };

    // 2. GỌI XUỐNG BACKEND (Dùng UUID thật)
    try {
       console.log(`[Fullstack Sync] Đang gắn Invader ${newInvaderId} vào Block...`);
       await StudioApi.attachInvader(nodeId, {
          invaderAssetId: '39a7a9e1-9f7a-4b9e-8c9d-1a2b3c4d5e6f', // Giả lập Invader Definition UUID (Cần fetch từ Warehouse sau)
          configSnapshot: newInvader.config
       });
       console.log(`✅ [Fullstack Sync] Backend đã chấp nhận UUID: ${newInvaderId}`);
    } catch (error) {
       console.warn(`⚠️ [Fullstack Sync] Backend tạm từ chối (có thể do chưa sync định nghĩa Invader). UI vẫn hiển thị mượt mà.`, error);
    }

    // 3. Cập nhật Local UI State để Canvas luôn mượt mà.
    setNodes((nds: Node<NoxNodeData>[]) => nds.map((node: Node<NoxNodeData>) => {
      if (node.id === nodeId) {
        return {
          ...node,
          data: {
            ...node.data,
            invaders: [...(node.data.invaders || []), newInvader]
          }
        };
      }
      return node;
    }));
  }, []);

  const updateInvaderOrder = useCallback((nodeId: string, newOrder: InvaderInstance[]) => {
    setNodes((nds) => nds.map((node) => {
      if (node.id === nodeId) {
        return { ...node, data: { ...node.data, invaders: newOrder } };
      }
      return node;
    }));
  }, []);

  const saveInvader = useCallback((nodeId: string, invaderId: string) => {
    const node = nodes.find((n: Node<NoxNodeData>) => n.id === nodeId);
    const invader = node?.data.invaders?.find((i: InvaderInstance) => i.id === invaderId);

    if (invader) {
      setSavedInvaders((prev: SavedInvader[]) => {
        const alreadySaved = prev.some((i: SavedInvader) => i.name === invader.name && i.type === invader.type);
        if (alreadySaved) return prev;

        const newInvader: SavedInvader = {
           id: uuidv4(),
           name: invader.name,
           type: invader.type,
           config: invader.config,
           createdAt: Date.now()
        };
        return [newInvader, ...prev];
      });
    }
  }, [nodes]);

  const deleteInvaderFromNode = useCallback(async (nodeId: string, invaderId: string) => {
    // 1. Sync Backend
    try {
       await StudioApi.detachInvader(nodeId, invaderId);
    } catch (error) {
       console.error(`❌ [Fullstack Sync] Lỗi khi tháo Invader:`, error);
    }

    // 2. Local State update
    setNodes((nds: Node<NoxNodeData>[]) => nds.map((node: Node<NoxNodeData>) => {
      if (node.id === nodeId) {
        return {
          ...node,
          data: {
            ...node.data,
            invaders: (node.data.invaders || []).filter((i: InvaderInstance) => i.id !== invaderId)
          }
        };
      }
      return node;
    }));
  }, []);

  // Persistence logic
  const LS_SOUL_KEY = 'nox_soul_library';

  useEffect(() => {
    const storedBlocks = localStorage.getItem(LS_KEY);
    if (storedBlocks) {
      try {
        setSavedBlocks(JSON.parse(storedBlocks));
      } catch (e) {
        console.error('Failed to parse Studio Library', e);
      }
    }

    const storedSouls = localStorage.getItem(LS_SOUL_KEY);
    if (storedSouls) {
       try {
          setSavedInvaders(JSON.parse(storedSouls));
       } catch (e) {
          console.error('Failed to parse Soul Library', e);
       }
    }

    // 3. Khởi tạo Project & Workspace từ URL
    const urlParams = new URLSearchParams(window.location.search);
    const pid = urlParams.get('project');
    if (pid) {
      console.log(`[Studio] Đang tải dự án: ${pid}`);
      setProjectId(pid);
    }
  }, []);

  // Sync Logic: Tải Workspaces khi có ProjectId
  useEffect(() => {
    if (!projectId) return;

    const loadWorkspace = async () => {
      try {
        // 1. Lấy danh sách workspace của dự án
        // Chú ý: Backend Endpoint cho WorkspaceController là /api/v1/projects/{projectId}/workspaces
        const res = await apiClient.get(`/v1/projects/${projectId}/workspaces`);
        const workspaces = res.data.data || res.data || [];
        
        if (workspaces.length > 0) {
          const ws = workspaces[0];
          setWorkspaceId(ws.id);
          console.log(`[Studio] Đã chọn Workspace: ${ws.name} (${ws.id})`);
          
          // 2. Tải Blocks cho Workspace này
          const blocksRes = await StudioApi.getWorkspaceBlocks(ws.id);
          const blocksData = blocksRes.data || blocksRes || [];
          
          // Map backend blocks sang React Flow nodes
          const mappedNodes = blocksData.map((b: any) => ({
            id: b.id,
            type: 'noxNode',
            position: b.visual?.position || { x: 100, y: 100 },
            data: { 
              label: b.name, 
              type: b.type,
              parentId: b.parentBlockId,
              invaders: b.invaders || []  // Sẽ fetch chi tiết sau nếu cần
            }
          }));
          
          setNodes(mappedNodes);
          console.log(`[Studio] Đã tải ${mappedNodes.length} blocks lên Canvas.`);
        }
      } catch (err) {
        console.error("❌ [Studio] Lỗi khi tải dữ liệu thiết kế:", err);
      }
    };

    loadWorkspace();
  }, [projectId]);

  useEffect(() => {
    localStorage.setItem(LS_KEY, JSON.stringify(savedBlocks));
  }, [savedBlocks]);

  useEffect(() => {
     localStorage.setItem(LS_SOUL_KEY, JSON.stringify(savedInvaders));
  }, [savedInvaders]);

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
      id: uuidv4(),
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
    
    // API Call logic for deletion (Optimistic UI)
    if (workspaceId) {
      changes.forEach(change => {
        if (change.type === 'remove') {
          console.log(`[Studio] Đang xóa Block (ID: ${change.id})...`);
          StudioApi.deleteBlock(workspaceId, change.id).catch(err => {
            console.error(`❌ [Studio] Lỗi khi xóa Block:`, err);
            // Có thể bổ sung Toast Component báo lỗi tại đây
          });
        }
      });
    }
  }, [workspaceId]);

  const dispatchEdgesChange = useCallback((changes: EdgeChange[]) => {
    setEdges((eds) => applyEdgeChanges(changes, eds));
  }, []);

  const onConnect = useCallback((connection: Connection) => {
    // 1. Tạo UUID thật cho Edge
    const id = uuidv4();
    
    // 2. Tương lai: Báo xuống Backend tạo Relation
    // StudioApi.createRelation(...)

    setEdges((eds) => {
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

  const removeSavedInvader = useCallback((invaderId: string) => {
    setSavedInvaders((prev) => {
      const updated = prev.filter(inv => inv.id !== invaderId);
      localStorage.setItem(LS_SOUL_KEY, JSON.stringify(updated));
      return updated;
    });
  }, []);

  const spawnInvaderFromLibrary = useCallback((nodeId: string, templateId: string) => {
    const template = savedInvaders.find(inv => inv.id === templateId);
    if (!template) return;

    setNodes((nds) => nds.map((node) => {
      if (node.id === nodeId) {
        const newInvader: InvaderInstance = {
          id: uuidv4(),
          templateId: template.id,
          name: template.name,
          type: template.type,
          config: template.config
        };
        return {
          ...node,
          data: {
            ...node.data,
            invaders: [...(node.data.invaders || []), newInvader]
          }
        };
      }
      return node;
    }));
  }, [savedInvaders]);

  // ==========================================================================
  // RENDER & EXPORT
  // ==========================================================================

  return (
    <StudioContext.Provider value={{ 
      // Persistence & Library
      savedBlocks, 
      saveBlock, 
      removeSavedBlock,
      savedInvaders,
      saveInvader,
      removeSavedInvader,

      // Navigation & Layers
      navigationPath, 
      enterNode, 
      exitToStep, 
      currentParentId,
      workspaceId,
      teleportToNode,

      // Workspace Data
      nodes, 
      setNodes, 
      edges, 
      setEdges,
      onNodesChange: dispatchNodesChange,
      onEdgesChange: dispatchEdgesChange,
      onConnect,

      // Linking & Routing
      isConnectMode, 
      setIsConnectMode,
      edgeColor, 
      setEdgeColor,
      updateEdgeWaypoint,
      addEdgeWaypoint,

      // Styling
      updateEdgeStyle,
      updateNodeOutputStyle,

      // Invader Management (Dashboard)
      activeSoulNodeId,
      isRightSidebarOpen,
      toggleRightSidebar,
      addInvaderToNode,
      updateInvaderOrder,
      deleteInvaderFromNode,
      spawnInvaderFromLibrary
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
