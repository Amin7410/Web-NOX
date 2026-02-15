import { memo } from 'react';
import { EdgeProps, getBezierPath, getSmoothStepPath, getStraightPath, BaseEdge, EdgeLabelRenderer } from 'reactflow';

export type RelationState = 'neutral' | 'valid' | 'invalid' | 'placeholder';
export type RelationShape = 'curve' | 'straight' | 'step';

export interface RelationData {
    state?: RelationState;
    shape?: RelationShape;
    animating?: boolean;
    label?: string;
}

export const NoxRelation = memo(({
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
    selected
}: EdgeProps<RelationData>) => {
    // 1. Determine Path Function based on shape
    const shape = data?.shape || 'curve';
    let path = '';
    let labelX = 0;
    let labelY = 0;

    const params = {
        sourceX, sourceY, sourcePosition,
        targetX, targetY, targetPosition,
    };

    if (shape === 'straight') {
        [path, labelX, labelY] = getStraightPath(params);
    } else if (shape === 'step') {
        [path, labelX, labelY] = getSmoothStepPath({ ...params, borderRadius: 10 });
    } else {
        [path, labelX, labelY] = getBezierPath(params);
    }

    // 2. Determine Style based on State
    const state = data?.state || 'neutral';
    const isAnimating = data?.animating;

    let strokeColor = '#52525b'; // zinc-600
    let strokeWidth = 2;
    let className = 'transition-all duration-300';
    let filter = '';

    switch (state) {
        case 'valid':
            strokeColor = '#f59e0b'; // amber-500
            filter = 'drop-shadow(0 0 3px rgba(245, 158, 11, 0.5))'; // Neon Amber
            break;
        case 'invalid':
            strokeColor = '#ef4444'; // red-500
            filter = 'drop-shadow(0 0 5px rgba(239, 68, 68, 0.8))'; // Neon Red
            break;
        case 'placeholder':
            strokeColor = '#eab308'; // yellow-500
            // We'll handle the dashed/striped look via SVG attributes or CSS classes if possible
            break;
        default:
            if (selected) {
                strokeColor = '#3b82f6'; // blue-500
                filter = 'drop-shadow(0 0 4px rgba(59, 130, 246, 0.5))';
            }
            break;
    }

    // SVG Props for the Main Path
    const globePathStyle = {
        ...style,
        stroke: strokeColor,
        strokeWidth: selected ? 3 : 2,
        filter,
        strokeDasharray: state === 'placeholder' ? '5,5' : undefined,
        animation: state === 'placeholder' ? 'dashdraw 0.5s linear infinite' : undefined
    };

    return (
        <>
            {/* 1. Base Cable (Darker background for contrast) */}
            <BaseEdge
                path={path}
                markerEnd={markerEnd}
                style={{ stroke: '#000', strokeWidth: strokeWidth + 2, opacity: 0.5 }}
            />

            {/* 2. Main Neon Cable */}
            <BaseEdge
                path={path}
                markerEnd={markerEnd}
                style={globePathStyle}
                className={className}
            />

            {/* 3. Data Flow Animation (Moving Particles) */}
            {isAnimating && (
                <BaseEdge
                    path={path}
                    markerEnd={markerEnd}
                    style={{ stroke: '#10b981', strokeWidth: 2, strokeDasharray: '5,10', animation: 'flow 1s linear infinite' }}
                    className="opacity-80"
                />
            )}

            {/* 4. Label / Logic Controls */}
            {data?.label && (
                <EdgeLabelRenderer>
                    <div
                        style={{
                            position: 'absolute',
                            transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px)`,
                            pointerEvents: 'all',
                        }}
                        className="nodrag nopan"
                    >
                        <div className="bg-zinc-900 border border-zinc-700 px-2 py-1 rounded text-[10px] text-zinc-400 font-mono shadow-md">
                            {data.label}
                        </div>
                    </div>
                </EdgeLabelRenderer>
            )}
        </>
    );
});
