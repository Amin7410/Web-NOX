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

export interface InvaderInstance {
  id: string;
  templateId?: string;
  name: string;
  type: 'logic' | 'aesthetic' | 'data' | 'system';
  config?: Record<string, any>;
}

export interface NoxNodeData {
  label: string;
  type: NoxNodeType;
  invaders: InvaderInstance[];
  blueprintContext?: string; // Human instructions for AI execution
  isDefined: boolean;
  parentId?: string;
  terminalConfig?: TerminalConfig;
}

export interface SavedInvader {
  id: string;
  name: string;
  type: 'logic' | 'aesthetic' | 'data' | 'system';
  config?: Record<string, any>;
  createdAt: number;
}

export interface SavedBlock {
  id: string;
  label: string;
  type: NoxNodeType;
  invaders: InvaderInstance[];
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
  workspaceId: string | null;

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

  // Invader Management & Dashboard
  activeSoulNodeId: string | null;
  isRightSidebarOpen: boolean;
  toggleRightSidebar: (open: boolean, nodeId?: string) => void;
  addInvaderToNode: (nodeId: string) => void;
  updateInvaderOrder: (nodeId: string, newOrder: InvaderInstance[]) => void;
  
  // Library Control
  savedInvaders: SavedInvader[];
  saveInvader: (nodeId: string, invaderId: string) => void;
  deleteInvaderFromNode: (nodeId: string, invaderId: string) => void;
  removeSavedInvader: (invaderId: string) => void;
  spawnInvaderFromLibrary: (nodeId: string, templateId: string) => void;
}
