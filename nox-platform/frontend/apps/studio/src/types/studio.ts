import { Node, Edge, NodeChange, EdgeChange, Connection } from 'reactflow';

export type TabId = 'blocks' | 'relations' | 'settings';

export type NoxNodeType = 
  | 'undefined' 
  | 'logic' 
  | 'data' 
  | 'ui' 
  | 'system' 
  | 'junction'
  | 'inputTerminal'
  | 'outputTerminal';

export type TerminalDirection = 'input' | 'output';
export type ParentHandleSide = 'top' | 'bottom' | 'left' | 'right';

export interface TerminalConfig {
  direction: TerminalDirection;
  parentHandle: ParentHandleSide;
  bridgeId?: string; // Unique global identifier for pairing
  mappingId?: string; 
}

export interface NoxNodeData {
  label: string;
  type: NoxNodeType;
  invaders?: string[];
  isDefined?: boolean;
  parentId?: string | null;
  terminalConfig?: TerminalConfig;
}

export interface SavedBlock {
  id: string;
  label: string;
  type: NoxNodeType;
  invaders: string[];
  createdAt: number;
  childrenNodes?: Node<NoxNodeData>[];
  internalEdges?: Edge[];
}

export interface NavigationStep {
  id: string;
  label: string;
}

export interface StudioState {
  // Persistence/Library
  savedBlocks: SavedBlock[];
  saveBlock: (nodeData: NoxNodeData, nodeId?: string) => void;
  removeSavedBlock: (id: string) => void;

  // Navigation/Layers
  navigationPath: NavigationStep[];
  enterNode: (id: string, label: string) => void;
  exitToStep: (index: number) => void;
  currentParentId: string | null;

  // Workspace Data (Scoping Layer)
  nodes: Node<NoxNodeData>[];
  setNodes: React.Dispatch<React.SetStateAction<Node<NoxNodeData>[]>>;
  edges: Edge[];
  setEdges: React.Dispatch<React.SetStateAction<Edge[]>>;
  onNodesChange: (changes: NodeChange[]) => void;
  onEdgesChange: (changes: EdgeChange[]) => void;
  onConnect: (connection: Connection) => void;

  // Linking System
  isConnectMode: boolean;
  setIsConnectMode: (enabled: boolean) => void;
  edgeColor: string;
  setEdgeColor: (color: string) => void;

  // Manual Routing (Architectural Control)
  updateEdgeWaypoint: (edgeId: string, index: number, position: { x: number, y: number }) => void;
  addEdgeWaypoint: (edgeId: string, position: { x: number, y: number }) => void;
  
  // Teleportation System
  teleportToNode: (nodeId: string) => void;

  // Style Management
  updateEdgeStyle: (edgeId: string, style: { color?: string, dashed?: boolean }) => void;
  updateNodeOutputStyle: (nodeId: string, style: { color?: string, dashed?: boolean }) => void;
}
