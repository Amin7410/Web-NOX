import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { Node, Edge, OnNodesChange, OnEdgesChange, applyNodeChanges, applyEdgeChanges, NodeChange, EdgeChange, Connection, addEdge, MarkerType } from 'reactflow';
import { StudioState, SavedBlock, NoxNodeData, NavigationStep, SavedInvader, InvaderInstance } from '../types/studio';
import { StudioApi } from '../services/studioApi';
import { apiClient } from '../services/apiClient';
import { v4 as uuidv4 } from 'uuid';
import { debounce } from '../utils/sync';

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
          
          // Map backend blocks sang các loại Node chuyên biệt của Studio
          const mappedNodes = blocksData.map((b: any) => {
            let rfType = 'noxNode';
            if (b.type === 'junction') rfType = 'noxJunction';
            else if (b.type === 'inputTerminal') rfType = 'noxInputTerminal';
            else if (b.type === 'outputTerminal') rfType = 'noxOutputTerminal';
            else if (b.type === 'invaderHub') rfType = 'noxInvaderHub';

            return {
              id: b.id,
              type: rfType,
              position: b.visual?.position || { x: 100, y: 100 },
              data: { 
                label: b.name, 
                type: b.type,
                parentId: b.parentBlockId,
                invaders: b.invaders || [],
                terminalConfig: b.visual?.terminalConfig || {}
              }
            };
          });

          setNodes(mappedNodes);
          console.log(`[Studio] Đã tải ${mappedNodes.length} blocks lên Canvas.`);

          // 3. Tải Relations cho Workspace này
          const relationsRes = await StudioApi.getWorkspaceRelations(ws.id);
          const relationsData = relationsRes.data || relationsRes || [];

          const mappedEdges = relationsData.map((r: any) => ({
            id: r.id,
            source: r.sourceBlockId,
            target: r.targetBlockId,
            sourceHandle: r.visual?.sourceHandle, // Đưa ra ngoài cấp cao nhất
            targetHandle: r.visual?.targetHandle, // Đưa ra ngoài cấp cao nhất
            type: 'noxEdge',
            data: { 
              formalId: r.id, 
              waypoints: r.visual?.waypoints || [] 
            },
            style: r.visual?.style || { stroke: edgeColor, strokeWidth: 2 },
            markerEnd: { type: MarkerType.ArrowClosed, color: (r.visual?.style?.stroke || edgeColor) }
          }));

          setEdges(mappedEdges);
          console.log(`[Studio] Đã tải ${mappedEdges.length} dây nối (Relations) cho Workspace.`);
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

  // Sync Debouncer for Visual Properties
  const syncEdgeDebounced = useMemo(() => 
    debounce(async (wsId: string, edge: Edge) => {
      console.log(`[Sync] Đồng bộ hình ảnh Relation: ${edge.id}...`);
      try {
        await StudioApi.updateRelation(wsId, edge.id, {
          visual: {
            sourceHandle: edge.sourceHandle,
            targetHandle: edge.targetHandle,
            waypoints: edge.data?.waypoints || [],
            style: {
              stroke: edge.style?.stroke,
              strokeWidth: edge.style?.strokeWidth,
              dashed: edge.data?.dashed
            }
          }
        });
      } catch (err) {
        console.error(`❌ [Sync] Lỗi đồng bộ Relation:`, err);
      }
    }, 1000), [workspaceId]);

  const syncBlockDebounced = useMemo(() => 
    debounce(async (wsId: string, node: Node<NoxNodeData>) => {
      console.log(`[Sync] Đồng bộ vị trí Block: ${node.id}...`);
      try {
        await StudioApi.updateBlock(wsId, node.id, {
          visual: {
            position: node.position
          }
        });
      } catch (err) {
        console.error(`❌ [Sync] Lỗi đồng bộ Block:`, err);
      }
    }, 1000), [workspaceId]);

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
    setEdges((eds) => {
      const newEdges = eds.map((e) => {
        if (e.id === edgeId) {
          const updatedEdge = { 
            ...e, 
            style: { 
              ...e.style, 
              stroke: style.color || e.style?.stroke,
              strokeDasharray: style.dashed ? '5,5' : (style.dashed === false ? '0' : e.style?.strokeDasharray)
            },
            data: { ...e.data, dashed: style.dashed !== undefined ? style.dashed : e.data?.dashed }
          };
          
          if (workspaceId && !edgeId.startsWith('local_')) {
            syncEdgeDebounced(workspaceId, updatedEdge);
          }
          return updatedEdge;
        }
        return e;
      });
      return newEdges;
    });
  }, [workspaceId, syncEdgeDebounced]);

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
          });
        }
        // Handle drag/position sync
        if (change.type === 'position' && change.position) {
          const node = nodes.find(n => n.id === change.id);
          if (node && !change.id.startsWith('local_')) {
             syncBlockDebounced(workspaceId, { ...node, position: change.position });
          }
        }
      });
    }
  }, [workspaceId, nodes, syncBlockDebounced]);

  const dispatchEdgesChange = useCallback((changes: EdgeChange[]) => {
    setEdges((eds) => applyEdgeChanges(changes, eds));

    // API Call logic for deletion (Optimistic UI)
    if (workspaceId) {
      changes.forEach(change => {
        if (change.type === 'remove') {
          console.log(`[Studio] Đang xóa Relation (ID: ${change.id})...`);
          StudioApi.deleteRelation(workspaceId, change.id).catch(err => {
            console.error(`❌ [Studio] Lỗi khi xóa Relation:`, err);
          });
        }
      });
    }
  }, [workspaceId]);

  const onConnect = useCallback((connection: Connection) => {
    // 0. Kiểm tra an toàn dữ liệu
    if (!connection.source || !connection.target) return;

    // 0. Chuẩn bị Handle ID (Dùng default nếu null)
    const effectiveSourceHandle = connection.sourceHandle || 'source-default';
    const effectiveTargetHandle = connection.targetHandle || 'target-default';

    // 1. Kiểm tra trùng lặp (Exact Port Match)
    const exists = edges.some(e => 
      e.source === connection.source && 
      e.target === connection.target &&
      e.sourceHandle === effectiveSourceHandle &&
      e.targetHandle === effectiveTargetHandle
    );

    if (exists) {
      console.warn(`[Studio] Kết nối giữa ${connection.source} (${effectiveSourceHandle}) và ${connection.target} (${effectiveTargetHandle}) đã tồn tại.`);
      return;
    }

    // 2. Tạo ID tạm thời
    const tempId = uuidv4();

    const newEdge: Edge = {
      id: tempId,
      source: connection.source!,
      target: connection.target!,
      sourceHandle: effectiveSourceHandle,
      targetHandle: effectiveTargetHandle,
      type: 'noxEdge',
      data: { waypoints: [] },
      style: { stroke: edgeColor, strokeWidth: 2 },
      markerEnd: { type: MarkerType.ArrowClosed, color: edgeColor }
    };

    // 2. Cập nhật UI ngay lập tức (Optimistic)
    setEdges((eds) => addEdge(newEdge, eds));

    // 3. Đồng bộ Backend
    if (workspaceId && connection.source && connection.target) {
      console.log(`[Studio] Đang lưu Relation mới: ${connection.source} (${effectiveSourceHandle}) -> ${connection.target} (${effectiveTargetHandle})`);
      StudioApi.createRelation(workspaceId, {
        sourceBlockId: connection.source,
        targetBlockId: connection.target,
        type: 'GENERAL', // Default type
        visual: {
          sourceHandle: effectiveSourceHandle,
          targetHandle: effectiveTargetHandle,
          style: {
            stroke: edgeColor,
            strokeWidth: 2,
            dashed: false
          }
        }
      }).then(res => {
        const formalId = res.data?.id || res.id;
        console.log(`✅ [Studio] Relation đã được lưu. ID chính thức: ${formalId}`);
        
        // Cập nhật lại ID tạm bằng ID thật từ DB để các thao tác update/delete sau này chính xác
        setEdges((eds) => eds.map(e => e.id === tempId ? { ...e, id: formalId } : e));
      }).catch(err => {
        console.error("❌ [Studio] Lỗi khi tạo Relation:", err);
        // Rollback UI (xóa edge tạm)
        setEdges((eds) => eds.filter(e => e.id !== tempId));
      });
    }
  }, [edgeColor, workspaceId, edges]);

  const updateEdgeWaypoint = useCallback((edgeId: string, index: number, position: { x: number, y: number }) => {
    setEdges((eds) => {
      const newEdges = eds.map((e) => {
        if (e.id === edgeId) {
          const waypoints = [...(e.data?.waypoints || [])];
          waypoints[index] = position;
          const updatedEdge = { ...e, data: { ...e.data, waypoints } };
          
          if (workspaceId && !edgeId.startsWith('local_')) {
            syncEdgeDebounced(workspaceId, updatedEdge);
          }
          return updatedEdge;
        }
        return e;
      });
      return newEdges;
    });
  }, [workspaceId, syncEdgeDebounced]);

  const addEdgeWaypoint = useCallback((edgeId: string, position: { x: number, y: number }) => {
    setEdges((eds) => {
      const newEdges = eds.map((e) => {
        if (e.id === edgeId) {
          const waypoints = [...(e.data?.waypoints || []), position];
          const updatedEdge = { ...e, data: { ...e.data, waypoints } };
          
          if (workspaceId && !edgeId.startsWith('local_')) {
            syncEdgeDebounced(workspaceId, updatedEdge);
          }
          return updatedEdge;
        }
        return e;
      });
      return newEdges;
    });
  }, [workspaceId, syncEdgeDebounced]);

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
