import React, { useState, useCallback, useEffect, useRef } from 'react';
import { MainRail, TabId } from './MainRail';
import { SidebarPanel } from './SidebarPanel';

export const Sidebar = () => {
  const [activeTab, setActiveTab] = useState<TabId | null>(null);
  const [sidebarWidth, setSidebarWidth] = useState(300);
  const isResizing = useRef(false);

  const handleTabChange = (tab: TabId) => {
    if (activeTab === tab) {
      setActiveTab(null);
    } else {
      setActiveTab(tab);
    }
  };

  const startResizing = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    isResizing.current = true;
  }, []);

  const stopResizing = useCallback(() => {
    isResizing.current = false;
  }, []);

  const resize = useCallback((e: MouseEvent) => {
    if (!isResizing.current) return;
    
    // Calculate new width: mouse X - MainRail width (64)
    const newWidth = e.clientX - 64;
    if (newWidth > 150 && newWidth < 600) {
      setSidebarWidth(newWidth);
    }
  }, []);

  useEffect(() => {
    window.addEventListener('mousemove', resize);
    window.addEventListener('mouseup', stopResizing);
    return () => {
      window.removeEventListener('mousemove', resize);
      window.removeEventListener('mouseup', stopResizing);
    };
  }, [resize, stopResizing]);

  return (
    <div className="flex h-full min-w-0 flex-none items-stretch overflow-hidden select-none">
      <MainRail activeTab={activeTab} onTabChange={handleTabChange} />
      {activeTab && (
        <SidebarPanel 
          activeTab={activeTab} 
          width={sidebarWidth}
          onResizeStart={startResizing}
        />
      )}
    </div>
  );
};
