export interface Organization {
  id: string;
  name: string;
  slug: string;
  role: "Owner" | "Member" | "Admin";
  memberCount: number;
  projectCount: number;
  createdAt: string;
  status: "Active" | "Inactive";
}

export const MOCK_ORGANIZATIONS: Organization[] = [
  { 
    id: "org_1", 
    name: "Acme Inc", 
    slug: "acme", 
    role: "Owner", 
    memberCount: 12, 
    projectCount: 5,
    createdAt: "Jan 15, 2025",
    status: "Active"
  },
  { 
    id: "org_2", 
    name: "Design Ops", 
    slug: "design-ops", 
    role: "Admin", 
    memberCount: 4, 
    projectCount: 2,
    createdAt: "Feb 20, 2025",
    status: "Active"
  },
  { 
    id: "org_3", 
    name: "Nox Team", 
    slug: "nox-team", 
    role: "Member", 
    memberCount: 25, 
    projectCount: 10,
    createdAt: "Dec 10, 2024",
    status: "Active"
  }
];
