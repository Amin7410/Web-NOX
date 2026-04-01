export interface ProjectItem {
  id: string;
  name: string;
  status: "Active" | "Archived" | "Draft";
  createdAt: string;
  owner: string;
}

export const MOCK_PROJECTS: ProjectItem[] = [
  {
    id: "1",
    name: "Website Redesign",
    status: "Active",
    createdAt: "Oct 12, 2025",
    owner: "Sarah Jenkins",
  },
  {
    id: "2",
    name: "Mobile App V2",
    status: "Active",
    createdAt: "Oct 15, 2025",
    owner: "Mike Ross",
  },
  {
    id: "3",
    name: "Q4 Marketing",
    status: "Draft",
    createdAt: "Oct 20, 2025",
    owner: "Anna Smith",
  },
  {
    id: "4",
    name: "Backend Migration",
    status: "Active",
    createdAt: "Nov 02, 2025",
    owner: "David Chen",
  },
  {
    id: "5",
    name: "User Research",
    status: "Archived",
    createdAt: "Aug 10, 2025",
    owner: "Emma Wilson",
  },
  {
    id: "6",
    name: "Design System",
    status: "Active",
    createdAt: "Sep 05, 2025",
    owner: "Sarah Jenkins",
  },
  {
    id: "7",
    name: "Security Audit",
    status: "Archived",
    createdAt: "Jul 22, 2025",
    owner: "Alex Turner",
  },
  {
    id: "8",
    name: "Social Media Campaign",
    status: "Draft",
    createdAt: "Nov 15, 2025",
    owner: "Anna Smith",
  },
];

export const MOCK_PROJECT_DETAIL = {
  id: "1",
  name: "Website Redesign",
  status: "Active",
  createdAt: "Oct 12, 2025",
  owner: "Sarah Jenkins",
  description: "A complete overhaul of our marketing website to improve conversion rates and update the brand visual language. This includes new landing pages, a rebuilt blog, and an optimized checkout flow. We are also migrating to Next.js for better performance and SEO.",
  stats: { tasksCompleted: 12, tasksTotal: 15, membersActive: 5, lastUpdated: "2 hours ago" },
  tags: ["Web", "Frontend", "Dashboard", "Marketing"]
};

export const MOCK_TEAM = [
  { id: "1", name: "Sarah Jenkins", email: "sarah@example.com", role: "Owner", status: "Active", avatar: "https://images.unsplash.com/photo-1494790108755-2616b612b786?w=32&h=32&fit=crop&crop=face" },
  { id: "2", name: "Mike Ross", email: "mike@example.com", role: "Developer", status: "Active", avatar: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=32&h=32&fit=crop&crop=face" },
  { id: "3", name: "Anna Smith", email: "anna@example.com", role: "Designer", status: "Active", avatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=32&h=32&fit=crop&crop=face" },
  { id: "4", name: "David Chen", email: "david@example.com", role: "Developer", status: "Inactive", avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=32&h=32&fit=crop&crop=face" },
  { id: "5", name: "Emma Wilson", email: "emma@example.com", role: "Project Manager", status: "Active", avatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=32&h=32&fit=crop&crop=face" }
];

export const MOCK_ANALYTICS = [
  { label: "Total Views", value: "1,234", change: "+12%", type: "trending-up", color: "text-blue-500" },
  { label: "Active Members", value: "5", change: "+1", type: "users", color: "text-green-500" },
  { label: "Tasks Completed", value: "12", change: "+3", type: "activity", color: "text-purple-500" },
  { label: "Avg. Response Time", value: "2.5h", change: "-0.5h", type: "clock", color: "text-orange-500" }
];
