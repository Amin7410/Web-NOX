import { memo } from 'react';
import { Handle, Position } from 'reactflow';

export const NoxRouter = memo(({ data, selected }: any) => {
    const color = data.color || '#ffffff';
    
    return (
        <div 
            className={`
                w-4 h-4 rounded-full border-2 transition-all duration-300
                flex items-center justify-center
                ${selected ? 'scale-125' : 'scale-100'}
            `}
            style={{ 
                backgroundColor: `${color}20`, 
                borderColor: color,
                boxShadow: selected ? `0 0 15px ${color}` : `0 0 5px ${color}50`
            }}
        >
            {/* Single centered handle that allows multiple connections */}
            <Handle 
                type="target" 
                id="target"
                position={Position.Top} 
                className="!bg-transparent !border-none !w-full !h-full !top-0 !left-0 !absolute"
                style={{ borderRadius: '50%' }}
                isConnectable={false}
            />
            
            <div className="w-1.5 h-1.5 rounded-full bg-white opacity-80" />

            <Handle 
                type="source" 
                id="source"
                position={Position.Bottom} 
                className="!bg-transparent !border-none !w-full !h-full !top-0 !left-0 !absolute"
                style={{ borderRadius: '50%' }}
                isConnectable={false}
            />
        </div>
    );
});

export default NoxRouter;
