import { useCallback } from 'react';
import { ReactFlowInstance, Node, XYPosition, Edge } from 'reactflow';
import { NoxNodeData, NoxNodeType, SavedBlock } from '../types/studio';
import { useStudio } from '../context/StudioContext';
import { v4 as uuidv4 } from 'uuid';
import { StudioApi } from '../services/studioApi';

interface DnDProps {
  reactFlowInstance: ReactFlowInstance | null;
  reactFlowWrapper: React.RefObject<HTMLDivElement>;
  setNodes: React.Dispatch<React.SetStateAction<Node<NoxNodeData>[]>>;
}

// Removed manual getId in favor of global standard UUIDv4

export const useCanvasDnD = ({ reactFlowInstance, reactFlowWrapper, setNodes }: DnDProps) => {
  const { currentParentId, savedBlocks, setEdges, workspaceId } = useStudio();

  const onDragOver = useCallback((event: React.DragEvent) => {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
  }, []);

  const onDrop = useCallback(
    (event: React.DragEvent) => {
      event.preventDefault();

      // Read ReactFlow type (noxNode/noxJunction/noxInputTerminal/noxOutputTerminal)
      const rfType = event.dataTransfer.getData('application/reactflow') || 'noxNode';
      const noxType = event.dataTransfer.getData('application/nox-type') as NoxNodeType;
      const noxId = event.dataTransfer.getData('application/nox-id');
      const isDefinedStr = event.dataTransfer.getData('application/nox-defined');
      const isCustomStr = event.dataTransfer.getData('application/nox-custom');
      
      const isDefined = isDefinedStr === 'true';
      const isCustom = isCustomStr === 'true';

      if (!rfType || !reactFlowInstance || !reactFlowWrapper.current) {
        return;
      }

      const reactFlowBounds = reactFlowWrapper.current.getBoundingClientRect();
      const position = reactFlowInstance.project({
        x: event.clientX - reactFlowBounds.left,
        y: event.clientY - reactFlowBounds.top,
      });

      // Grid-snap (40px)
      const snappedPosition: XYPosition = {
        x: Math.round(position.x / 40) * 40,
        y: Math.round(position.y / 40) * 40,
      };

      if (isCustom) {
        // Deep Clone Mega Pattern using ID for precision
        const savedBlock = savedBlocks.find(b => b.id === noxId || b.label === event.dataTransfer.getData('text/plain'));
        
        if (savedBlock && savedBlock.childrenNodes) {
          const idMap = new Map<string, string>();
          const newRootId = uuidv4();

          const newNodes: Node<NoxNodeData>[] = [];
          const newEdges: Edge[] = [];

          const rootNode: Node<NoxNodeData> = {
            id: newRootId,
            type: savedBlock.type === 'junction' ? 'noxJunction' : 
                  (savedBlock.type === 'inputTerminal' ? 'noxInputTerminal' : 
                  (savedBlock.type === 'outputTerminal' ? 'noxOutputTerminal' : 'noxNode')),
            position: snappedPosition,
            data: { 
              label: savedBlock.label,
              type: savedBlock.type,
              invaders: [...savedBlock.invaders],
              isDefined: true,
              parentId: currentParentId
            },
          };
          newNodes.push(rootNode);

          savedBlock.childrenNodes.forEach(child => {
            idMap.set(child.id, uuidv4());
          });

          savedBlock.childrenNodes.forEach(child => {
            const newChildId = idMap.get(child.id)!;
            const newParentIdForChild = child.data.parentId ? (idMap.get(child.data.parentId) || newRootId) : newRootId;

            newNodes.push({
              ...child,
              id: newChildId,
              data: {
                ...child.data,
                parentId: newParentIdForChild
              }
            });
          });

          if (savedBlock.internalEdges) {
            savedBlock.internalEdges.forEach(edge => {
              const newSource = idMap.get(edge.source) || newRootId;
              const newTarget = idMap.get(edge.target) || newRootId;

              newEdges.push({
                ...edge,
                id: uuidv4(),
                source: newSource,
                target: newTarget
              });
            });
          }

          setNodes((nds) => [...nds, ...newNodes]);
          setEdges((eds) => [...eds, ...newEdges]);
          return;
        }
      }

      // Default instantiation
      const newNodeId = uuidv4();
      const nodeName = noxType === 'junction' ? 'Junction Point' : 
                 (noxType === 'inputTerminal' ? 'Incoming Bridge' : 
                 (noxType === 'outputTerminal' ? 'Outgoing Bridge' : 
                 (isCustom ? `Clone: ${noxType.toUpperCase()}` : (isDefined ? `New ${noxType.toUpperCase()} Module` : 'New Conceptual Block'))));

      const newNode: Node<NoxNodeData> = {
        id: newNodeId,
        type: rfType,
        position: snappedPosition,
        data: { 
          label: nodeName,
          type: noxType,
          invaders: (isDefined && !['junction', 'inputTerminal', 'outputTerminal'].includes(noxType)) ? ['Placeholder-Invader'] : [],
          isDefined: isDefined || isCustom,
          parentId: currentParentId,
          terminalConfig: (noxType === 'inputTerminal' || noxType === 'outputTerminal') ? {
            direction: noxType === 'inputTerminal' ? 'input' : 'output',
            parentHandle: 'top'
          } : undefined
        },
      };

      setNodes((nds) => nds.concat(newNode));
      
      // API Call logic for creation (Optimistic UI Sync)
      if (workspaceId) {
        console.log(`[Studio] Đang đẩy Block mới lên server với ID: ${newNodeId}`);
        StudioApi.createBlock(workspaceId, {
          id: newNodeId, // Đồng bộ ID từ Frontend xuống Backend
          type: noxType === 'undefined' ? 'undefined' : noxType,
          name: nodeName,
          visual: { position: snappedPosition },
          parentBlockId: currentParentId || undefined
        }).catch(err => {
          console.error(`❌ [Studio] Lỗi tạo Block trên server:`, err);
        });
      }
    },
    [reactFlowInstance, setNodes, setEdges, reactFlowWrapper, currentParentId, savedBlocks, workspaceId]
  );

  return { onDragOver, onDrop };
};
