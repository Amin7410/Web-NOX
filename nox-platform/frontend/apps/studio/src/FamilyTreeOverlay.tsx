import { memo, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

// Mock Data for "Family Trees" (Class Hierarchies)
const MOCK_FAMILIES = [
    {
        id: 'fam_vehicle',
        name: 'Vehicle Hierarchy',
        root: {
            id: 'n_vehicle', label: 'Vehicle', type: 'abstract', children: [
                {
                    id: 'n_car', label: 'Car', type: 'class', children: [
                        { id: 'n_sedan', label: 'Sedan', type: 'class' },
                        { id: 'n_suv', label: 'SUV', type: 'class' }
                    ]
                },
                {
                    id: 'n_bike', label: 'Bike', type: 'class', children: [
                        { id: 'n_sport', label: 'SportBike', type: 'class' }
                    ]
                }
            ]
        }
    },
    {
        id: 'fam_animal',
        name: 'Kingdom Animalia',
        root: {
            id: 'n_animal', label: 'Animal', type: 'abstract', children: [
                {
                    id: 'n_mammal', label: 'Mammal', type: 'class', children: [
                        { id: 'n_dog', label: 'Dog', type: 'class' },
                        { id: 'n_cat', label: 'Cat', type: 'class' }
                    ]
                },
                {
                    id: 'n_reptile', label: 'Reptile', type: 'class', children: [
                        { id: 'n_lizard', label: 'Lizard', type: 'class' }
                    ]
                }
            ]
        }
    },
    {
        id: 'fam_shape',
        name: 'Geometry Shapes',
        root: {
            id: 'n_shape', label: 'Shape', type: 'interface', children: [
                { id: 'n_circle', label: 'Circle', type: 'class' },
                { id: 'n_rect', label: 'Rectangle', type: 'class' },
                { id: 'n_poly', label: 'Polygon', type: 'class' }
            ]
        }
    }
];

// Recursive Tree Node Component
const TreeNode = ({ node, depth = 0 }: { node: any, depth?: number }) => {
    const isRoot = depth === 0;
    return (
        <div className="flex flex-col items-center">
            {/* Connection Line from Parent */}
            {!isRoot && (
                <div className="h-4 w-px bg-zinc-600"></div>
            )}

            {/* The Node Itself */}
            <div className={`
                px-3 py-1.5 rounded border shadow-lg text-xs font-mono transition-transform hover:scale-110 cursor-default
                ${node.type === 'abstract' ? 'bg-zinc-800 border-zinc-500 text-zinc-300 italic' : ''}
                ${node.type === 'interface' ? 'bg-zinc-900 border-dashed border-zinc-500 text-indigo-400' : ''}
                ${node.type === 'class' ? 'bg-zinc-900 border-zinc-700 text-blue-300' : ''}
            `}>
                <span className="opacity-50 text-[9px] mr-1">
                    {node.type === 'interface' ? 'I' : node.type === 'abstract' ? 'A' : 'C'}
                </span>
                {node.label}
            </div>

            {/* Children */}
            {node.children && node.children.length > 0 && (
                <>
                    <div className="h-4 w-px bg-zinc-600"></div>
                    <div className="flex gap-4 relative">
                        {/* Horizontal connector bar (only if > 1 child) */}
                        {node.children.length > 1 && (
                            <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[calc(100%-2rem)] h-px bg-zinc-600" />
                        )}

                        {node.children.map((child: any) => (
                            <TreeNode key={child.id} node={child} depth={depth + 1} />
                        ))}
                    </div>
                </>
            )}
        </div>
    );
};

export const FamilyTreeOverlay = memo(({ isOpen, onClose }: { isOpen: boolean, onClose: () => void }) => {
    return (
        <AnimatePresence>
            {isOpen && (
                <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="absolute inset-0 z-[50] pointer-events-none flex items-center justify-center"
                >
                    {/* Backdrop with Scanlines effect */}
                    <div className="absolute inset-0 bg-black/80 backdrop-blur-sm pointer-events-auto" onClick={onClose} />

                    {/* Content Container */}
                    <motion.div
                        initial={{ scale: 0.9, y: 20 }}
                        animate={{ scale: 1, y: 0 }}
                        exit={{ scale: 0.9, y: 20 }}
                        className="relative z-10 w-[90%] h-[90%] pointer-events-auto flex flex-col gap-8 overflow-auto p-12"
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div className="text-center space-y-2">
                            <h2 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-indigo-500 tracking-widest uppercase filter drop-shadow-[0_0_10px_rgba(59,130,246,0.5)]">
                                Structural View (X-Ray)
                            </h2>
                            <p className="text-zinc-500 text-xs font-mono uppercase tracking-widest">
                                Family Tree Declarations
                            </p>
                        </div>

                        <div className="flex flex-wrap justify-center gap-16 items-start">
                            {MOCK_FAMILIES.map(fam => (
                                <div key={fam.id} className="flex flex-col items-center">
                                    <div className="mb-8 px-4 py-1 rounded-full bg-zinc-900/50 border border-zinc-700 text-zinc-400 text-[10px] uppercase tracking-wider">
                                        {fam.name}
                                    </div>
                                    <TreeNode node={fam.root} />
                                </div>
                            ))}
                        </div>

                        {/* Close Hint */}
                        <div className="fixed bottom-8 left-1/2 -translate-x-1/2 text-zinc-600 text-xs font-mono animate-pulse">
                            Click anywhere to return to Runtime Canvas
                        </div>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
});
