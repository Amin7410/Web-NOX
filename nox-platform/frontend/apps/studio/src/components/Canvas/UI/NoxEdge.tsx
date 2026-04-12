import React, { memo, useCallback, useMemo, useState } from 'react';
import { 
  EdgeProps, 
  getSmoothStepPath, 
  EdgeLabelRenderer,
  useReactFlow,
  Position
} from 'reactflow';
import { useStudio } from '../../../context/StudioContext';
import { snapToGrid } from '../../../utils/routing';

const NoxEdge = ({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  data,
  style = {},
  markerEnd,
  selected,
}: EdgeProps) => {
  const { isConnectMode, edgeColor, updateEdgeWaypoint, addEdgeWaypoint } = useStudio();
  const { screenToFlowPosition } = useReactFlow();
  
  // High-performance path calculations
  const edgePath = useMemo(() => {
    const waypoints = data?.waypoints || [];
    
    if (waypoints.length === 0) {
      const [path] = getSmoothStepPath({
        sourceX, sourceY, sourcePosition,
        targetX, targetY, targetPosition,
        borderRadius: 8,
      });
      return path;
    }

    let fullPath = '';
    let currentX = sourceX;
    let currentY = sourceY;
    let currentPos = sourcePosition;

    [...waypoints, { x: targetX, y: targetY }].forEach((pt, idx) => {
      const isLast = idx === waypoints.length;
      const [segment] = getSmoothStepPath({
        sourceX: currentX,
        sourceY: currentY,
        sourcePosition: currentPos,
        targetX: pt.x,
        targetY: pt.y,
        targetPosition: isLast ? targetPosition : (currentX < pt.x ? Position.Left : Position.Right),
        borderRadius: 8
      });
      
      fullPath += (idx === 0 ? segment : segment.replace(/^M[^L]*/, ''));
      currentX = pt.x;
      currentY = pt.y;
      currentPos = isLast ? targetPosition : (pt.x < targetX ? Position.Right : Position.Left);
    });

    return fullPath;
  }, [sourceX, sourceY, targetX, targetY, sourcePosition, targetPosition, data?.waypoints]);

  // Interaction handlers
  const onAddWaypoint = useCallback((evt: React.MouseEvent) => {
    evt.stopPropagation();
    if (!isConnectMode) return;
    
    const position = screenToFlowPosition({ x: evt.clientX, y: evt.clientY });
    addEdgeWaypoint(id, { 
      x: snapToGrid(position.x), 
      y: snapToGrid(position.y) 
    });
  }, [id, isConnectMode, addEdgeWaypoint, screenToFlowPosition]);

  const handleWaypointDrag = useCallback((idx: number, evt: React.MouseEvent) => {
    evt.stopPropagation();
    if (!isConnectMode) return;

    const onMouseMove = (moveEvent: MouseEvent) => {
      const pos = screenToFlowPosition({ x: moveEvent.clientX, y: moveEvent.clientY });
      updateEdgeWaypoint(id, idx, { 
        x: snapToGrid(pos.x), 
        y: snapToGrid(pos.y) 
      });
    };

    const onMouseUp = () => {
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
    };

    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
  }, [id, isConnectMode, updateEdgeWaypoint, screenToFlowPosition]);

  return (
    <>
      <path
        id={id}
        style={{
          ...style,
          stroke: selected ? '#a5b4fc' : (style.stroke || edgeColor),
          strokeWidth: selected ? 4 : 2,
          filter: selected ? 'drop-shadow(0 0 10px rgba(99, 102, 241, 0.4))' : 'none',
          transition: 'stroke 0.3s ease',
          pointerEvents: 'all',
          cursor: isConnectMode ? 'crosshair' : 'pointer'
        }}
        className="react-flow__edge-path"
        d={edgePath}
        markerEnd={markerEnd}
        onDoubleClick={onAddWaypoint}
      />

      {isConnectMode && (
        <EdgeLabelRenderer>
          {(data?.waypoints || []).map((pt: any, idx: number) => (
            <div
              key={`${id}-wp-${idx}`}
              style={{
                position: 'absolute',
                transform: `translate(-50%, -50%) translate(${pt.x}px,${pt.y}px)`,
                pointerEvents: 'all',
              }}
              className="nodrag nopan"
            >
              <div 
                onMouseDown={(e) => handleWaypointDrag(idx, e)}
                className="w-4 h-4 bg-zinc-950 border-2 border-indigo-500 rounded-sm shadow-[0_0_15px_rgba(99,102,241,0.6)] cursor-move transition-transform hover:scale-125 hover:bg-indigo-500 active:bg-white flex items-center justify-center group"
              >
                 <div className="w-1 h-1 rounded-full bg-indigo-400 group-hover:bg-zinc-900" />
              </div>
            </div>
          ))}
        </EdgeLabelRenderer>
      )}
    </>
  );
};

export default memo(NoxEdge);
