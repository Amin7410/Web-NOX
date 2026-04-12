'use client';

// Helper to manage mock data in localStorage so different pages can interact
export const mockStore = {
  getOrganizations: () => {
    if (typeof window === 'undefined') return [];
    const saved = localStorage.getItem('nox_mock_orgs');
    const orgs = saved ? JSON.parse(saved) : [];
    
    // Default mock if totally empty
    if (orgs.length === 0) {
      return [{
        id: "mock-1",
        name: "NOX Default Team",
        slug: "nox-default",
        role: "Owner",
        memberCount: 1,
        projectCount: 3,
        createdAt: new Date().toLocaleDateString(),
        status: "Active"
      }];
    }
    return orgs;
  },

  addOrganization: (org: any) => {
    if (typeof window === 'undefined') return;
    const orgs = mockStore.getOrganizations();
    const newOrg = {
      ...org,
      id: org.id || `mock-${Math.random().toString(36).substr(2, 9)}`,
      role: "Owner",
      memberCount: 1,
      projectCount: 0,
      createdAt: new Date().toLocaleDateString(),
      status: "Active"
    };
    localStorage.setItem('nox_mock_orgs', JSON.stringify([newOrg, ...orgs]));
    return newOrg;
  },

  getProjects: () => {
    if (typeof window === 'undefined') return [];
    const saved = localStorage.getItem('nox_mock_projects');
    const projects = saved ? JSON.parse(saved) : [];

    if (projects.length === 0) {
        return [
            {
              id: "mock-p1",
              name: "E-Commerce Dashboard",
              status: "Active",
              createdAt: new Date().toLocaleDateString(),
              organizationName: "NOX Default Team"
            }
          ];
    }
    return projects;
  },

  addProject: (project: any) => {
    if (typeof window === 'undefined') return;
    const projects = mockStore.getProjects();
    const orgs = mockStore.getOrganizations();
    const org = orgs.find((o: any) => o.id === project.organizationId);

    const newProject = {
      ...project,
      id: `mock-p-${Math.random().toString(36).substr(2, 9)}`,
      organizationName: org ? org.name : "Unknown Organization",
      createdAt: new Date().toLocaleDateString(),
      status: project.status || "Active"
    };
    localStorage.setItem('nox_mock_projects', JSON.stringify([newProject, ...projects]));
    return newProject;
  }
};
