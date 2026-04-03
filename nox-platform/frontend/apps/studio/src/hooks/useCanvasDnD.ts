import { useCallback } from 'react';
import { ReactFlowInstance, Node, XYPosition, Edge } from 'reactflow';
import { NoxNodeData, NoxNodeType, SavedBlock } from '../types/studio';
import { useStudio } from '../context/StudioContext';

interface DnDProps {
  reactFlowInstance: ReactFlowInstance | null;
  reactFlowWrapper: React.RefObject<HTMLDivElement>;
  setNodes: React.Dispatch<React.SetStateAction<Node<NoxNodeData>[]>>;
}

const getId = () => `nox_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

export const useCanvasDnD = ({ reactFlowInstance, reactFlowWrapper, setNodes }: DnDProps) => {
  const { currentParentId, savedBlocks, setEdges } = useStudio();

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
        // Deep Clone Mega Pattern
        const label = event.dataTransfer.getData('text/plain');
        const savedBlock = savedBlocks.find(b => b.label === label);
        
        if (savedBlock && savedBlock.childrenNodes) {
          const idMap = new Map<string, string>();
          const newRootId = getId();

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
            idMap.set(child.id, getId());
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
                id: `e_${getId()}`,
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
      const newNode: Node<NoxNodeData> = {
        id: getId(),
        type: rfType,
        position: snappedPosition,
        data: { 
          label: noxType === 'junction' ? 'Junction Point' : 
                 (noxType === 'inputTerminal' ? 'Input Bridge' : 
                 (noxType === 'outputTerminal' ? 'Output Bridge' : 
                 (isCustom ? `Clone: ${noxType.toUpperCase()}` : (isDefined ? `New ${noxType.toUpperCase()} Module` : 'New Conceptual Block')))),
          type: noxType,
          invaders: (isDefined && !['junction', 'inputTerminal', 'outputTerminal'].includes(noxType)) ? ['Placeholder-Invader'] : [],
          isDefined: isDefined || isCustom,
          parentId: currentParentId,
          // Initialize terminal config if applicable
          terminalConfig: (noxType === 'inputTerminal' || noxType === 'outputTerminal') ? {
            direction: noxType === 'inputTerminal' ? 'input' : 'output',
            parentHandle: 'top' // Default to top mapping
          } : undefined
        },
      };

      setNodes((nds) => nds.concat(newNode));
    },
    [reactFlowInstance, setNodes, setEdges, reactFlowWrapper, currentParentId, savedBlocks]
  );

  return { onDragOver, onDrop };
};
