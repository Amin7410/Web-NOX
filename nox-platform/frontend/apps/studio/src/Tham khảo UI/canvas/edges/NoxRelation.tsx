import { BaseEdge, EdgeLabelRenderer, EdgeProps, getBezierPath, getSmoothStepPath, getStraightPath } from 'reactflow';

export default function NoxRelation({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  style = {},
  markerEnd,
  data,
}: EdgeProps) {
  const shape = data?.shape || 'step';
  const color = data?.color || '#6366f1';
  const animating = data?.animating || false;

  let edgePath = '';
  let labelX = 0;
  let labelY = 0;

  const params = { sourceX, sourceY, sourcePosition, targetX, targetY, targetPosition };

  switch (shape) {
    case 'straight':
        [edgePath, labelX, labelY] = getStraightPath(params); break;
    case 'bezier':
        [edgePath, labelX, labelY] = getBezierPath(params); break;
    case 'smooth':
        [edgePath, labelX, labelY] = getSmoothStepPath(params); break;
    case 'step':
    default:
        [edgePath, labelX, labelY] = getSmoothStepPath({ ...params, borderRadius: 0 }); break;
  }

  return (
    <>
      <BaseEdge 
          path={edgePath} 
          markerEnd={markerEnd} 
          style={{ 
              ...style, 
              stroke: color, 
              strokeWidth: 2,
              opacity: 0.8
          }} 
      />
      
      {animating && (
        <circle r="3" fill={color}>
          <animateMotion 
              dur="2.4s" 
              repeatCount="indefinite" 
              path={edgePath} 
          />
        </circle>
      )}

      <EdgeLabelRenderer>
        <div
          style={{
            position: 'absolute',
            transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px)`,
            pointerEvents: 'all',
          }}
          className="z-10"
        >
          {data?.label && (
            <div className="px-2 py-0.5 rounded border bg-zinc-950/80 backdrop-blur-sm text-[9px] font-bold uppercase tracking-widest text-zinc-400 border-zinc-800">
              {data.label}
            </div>
          )}
        </div>
      </EdgeLabelRenderer>
    </>
  );
}
