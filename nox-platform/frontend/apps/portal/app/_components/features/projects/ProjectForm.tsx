'use client';

import { useState, useEffect } from "react";
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { 
  ArrowLeft, UploadCloud, Users, Settings, Save, Eye, CheckCircle2,
  X, Loader2
} from 'lucide-react';
import { Button } from '../../../ui/button';
import { Input } from '../../../ui/input';
import { Textarea } from '../../../ui/textarea';
import { Label } from '../../../ui/label';
import { Badge } from '../../../ui/badge';
export function ProjectForm() {
  const router = useRouter();
  const [projectName, setProjectName] = useState("");
  const [description, setDescription] = useState("");
  const [organization, setOrganization] = useState("");
  const [organizations, setOrganizations] = useState<any[]>([]);
  const [status, setStatus] = useState("draft");
  const [loading, setLoading] = useState(false);
  const [fetchingOrgs, setFetchingOrgs] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  useEffect(() => {
    const fetchOrgs = async () => {
      try {
        const res = await fetch('/api/orgs');
        const data = await res.json();
        let list = data.data || [];
        
        setOrganizations(list);
        if (list.length > 0) {
          setOrganization(list[0].id);
        }
      } catch (err) {
        console.error("Failed to fetch organizations", err);
        setError("Could not load organizations.");
      } finally {
        setFetchingOrgs(false);
      }
    };
    fetchOrgs();
  }, []);

  const handleCreate = async () => {
    if (!projectName || !organization) return;
    setLoading(true);

    try {
      const res = await fetch('/api/projects', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: projectName,
          description,
          organizationId: organization,
          visibility: status === 'active' ? 'PUBLIC' : 'PRIVATE'
        }),
      });
      const data = await res.json().catch(() => ({}));
      if (res.ok && data.data) {
        router.push('/projects');
      } else {
        console.error('Failed to create project', data);
        alert(data.message || 'Failed to create project');
      }
    } catch (err) {
      console.error('Create project exception:', err);
      alert('Internal Server Error. Please try again.');
    } finally {
      setLoading(false);
    }
  };
  
  // Tags handling
  const [tagInput, setTagInput] = useState("");
  const [tags, setTags] = useState<string[]>([]);
  
  const handleTagKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      const val = tagInput.trim().replace(/,$/, '');
      if (val && tags.length < 10 && val.length <= 20 && !tags.includes(val)) {
        setTags([...tags, val]);
        setTagInput("");
      }
    } else if (e.key === 'Backspace' && !tagInput && tags.length > 0) {
      setTags(tags.slice(0, -1));
    }
  };

  const removeTag = (tagToRemove: string) => {
    setTags(tags.filter(tag => tag !== tagToRemove));
  };

  const isNameInvalid = projectName.length > 100;
  const isDescInvalid = description.length > 1000;
  
  return (
    <div className="flex flex-col w-full max-w-4xl mx-auto pb-16 p-6">
      {/* Top Navigation Bar */}
      <div className="flex items-center justify-between mb-8">
        <Link 
          href="/projects" 
          className="inline-flex items-center text-sm font-medium text-gray-500 hover:text-gray-900 transition-colors"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Projects
        </Link>
        <div className="flex items-center gap-3">
          <Button 
            variant="outline" 
            onClick={() => router.push('/projects')}
            className="border-gray-200 text-gray-700 bg-white hover:bg-gray-50 h-9"
          >
            Cancel
          </Button>
          <Button 
            onClick={handleCreate}
            className="bg-[#4F46E5] hover:bg-[#4338CA] text-white shadow-sm font-medium h-9"
            disabled={!projectName || isNameInvalid || isDescInvalid || loading}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
            Create Project
          </Button>
        </div>
      </div>

      {/* Header Info */}
      <div className="flex flex-col gap-2 mb-10">
        <h1 className="text-3xl font-semibold tracking-tight text-gray-900">
          Create New Project
        </h1>
        <p className="text-[15px] text-gray-500">
          Set up a new project and start collaborating
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-10">
        {/* Main Form Area (Left Column, span 3) */}
        <div className="lg:col-span-3 flex flex-col gap-8">
          
          <section className="flex flex-col">
            <div className="flex items-center border-b border-gray-200 pb-3 mb-6">
              <h2 className="text-lg font-medium text-gray-900">Project Details</h2>
            </div>
            
            <form className="flex flex-col gap-6" onSubmit={(e) => e.preventDefault()}>
              
              {/* Project Name */}
              <div className="flex flex-col gap-2.5">
                <div className="flex justify-between items-center">
                  <Label htmlFor="projectName" className="text-gray-700">
                    Project Name <span className="text-[#EF4444]">*</span>
                  </Label>
                  <span className={`text-xs ${isNameInvalid ? 'text-[#EF4444]' : 'text-gray-400'}`}>
                    {projectName.length}/100
                  </span>
                </div>
                <Input 
                  id="projectName"
                  placeholder="Enter project name..." 
                  value={projectName}
                  onChange={(e) => setProjectName(e.target.value)}
                  className={`bg-white focus-visible:ring-[#4F46E5] focus-visible:border-[#4F46E5] transition-all shadow-sm ${
                    isNameInvalid ? 'border-[#EF4444] focus-visible:ring-[#EF4444] focus-visible:border-[#EF4444]' : 'border-gray-200'
                  }`}
                />
                {isNameInvalid && (
                  <p className="text-xs text-[#EF4444] font-medium">Project name cannot exceed 100 characters.</p>
                )}
              </div>

              {/* Description */}
              <div className="flex flex-col gap-2.5">
                <div className="flex justify-between items-center">
                  <Label htmlFor="description" className="text-gray-700">Description</Label>
                  <span className={`text-xs ${isDescInvalid ? 'text-[#EF4444]' : 'text-gray-400'}`}>
                    {description.length}/1000
                  </span>
                </div>
                <Textarea 
                  id="description"
                  placeholder="Enter project description..." 
                  rows={4}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className={`bg-white resize-none focus-visible:ring-[#4F46E5] focus-visible:border-[#4F46E5] transition-all shadow-sm ${
                    isDescInvalid ? 'border-[#EF4444] focus-visible:ring-[#EF4444] focus-visible:border-[#EF4444]' : 'border-gray-200'
                  }`}
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                {/* Organization */}
                <div className="flex flex-col gap-2.5">
                  <Label className="text-gray-700">
                    Organization <span className="text-[#EF4444]">*</span>
                  </Label>
                  <select 
                    value={organization} 
                    onChange={(e) => setOrganization(e.target.value)}
                    className="flex h-10 w-full items-center justify-between rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm text-gray-900 focus:border-[#4F46E5] focus:ring-2 focus:ring-[#4F46E5] focus:ring-offset-2 focus:ring-offset-white disabled:cursor-not-allowed disabled:opacity-50 transition-colors"
                    disabled={fetchingOrgs}
                  >
                    {fetchingOrgs ? (
                      <option>Loading organizations...</option>
                    ) : organizations.length === 0 ? (
                      <option value="">No organizations available</option>
                    ) : (
                      organizations.map(org => (
                        <option key={org.id} value={org.id}>{org.name}</option>
                      ))
                    )}
                  </select>
                </div>

                {/* Status */}
                <div className="flex flex-col gap-2.5">
                  <Label className="text-gray-700">Status</Label>
                  <select 
                    value={status} 
                    onChange={(e) => setStatus(e.target.value)}
                    className="flex h-10 w-full items-center justify-between rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm text-gray-900 focus:border-[#4F46E5] focus:ring-2 focus:ring-[#4F46E5] focus:ring-offset-2 focus:ring-offset-white disabled:cursor-not-allowed disabled:opacity-50 transition-colors"
                  >
                    <option value="draft">Draft</option>
                    <option value="active">Active</option>
                    <option value="archived">Archived</option>
                  </select>
                </div>
              </div>

              {/* Tags */}
              <div className="flex flex-col gap-2.5">
                <div className="flex justify-between items-center">
                  <Label htmlFor="tags" className="text-gray-700">Tags</Label>
                  <span className={`text-xs ${tags.length >= 10 ? 'text-amber-500' : 'text-gray-400'}`}>
                    {tags.length}/10
                  </span>
                </div>
                <div className="flex flex-wrap gap-2 p-2 min-h-11 bg-white border border-gray-200 rounded-md shadow-sm focus-within:ring-[3px] focus-within:ring-[#4F46E5]/50 focus-within:border-[#4F46E5] transition-all">
                  {tags.map(tag => (
                    <Badge 
                      key={tag} 
                      variant="secondary" 
                      className="bg-gray-100 text-gray-700 hover:bg-gray-200 font-medium px-2.5 py-1 rounded-md border-0 transition-colors cursor-pointer"
                    >
                      {tag}
                      <button 
                        type="button"
                        onClick={() => removeTag(tag)}
                        className="text-gray-400 hover:text-gray-600 focus:outline-none"
                      >
                        <X className="h-3 w-3" />
                      </button>
                    </Badge>
                  ))}
                  <input
                    id="tags"
                    type="text"
                    value={tagInput}
                    onChange={(e) => setTagInput(e.target.value)}
                    onKeyDown={handleTagKeyDown}
                    disabled={tags.length >= 10}
                    placeholder={tags.length === 0 ? "web, frontend, dashboard..." : ""}
                    className="flex-1 min-w-[120px] bg-transparent outline-none text-sm placeholder:text-gray-400 disabled:cursor-not-allowed"
                  />
                </div>
                <p className="text-xs text-gray-500">e.g., web, frontend, dashboard. Press Enter or comma to add.</p>
              </div>

            </form>
          </section>

          {/* Action Buttons Section */}
          <div className="flex flex-wrap items-center gap-3 pt-4">
            <Button 
              variant="outline" 
              onClick={handleCreate}
              disabled={!projectName || loading}
              className="border-gray-200 text-gray-700 bg-white hover:bg-gray-50 font-medium shadow-sm"
            >
              {loading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : <Save className="h-4 w-4 mr-2" />}
              Save Draft
            </Button>
            <Button variant="outline" className="border-gray-200 text-gray-700 bg-white hover:bg-gray-50 font-medium shadow-sm">
              <Eye className="h-4 w-4 mr-2" />
              Preview
            </Button>
            <div className="flex-1"></div>
            <Button 
              onClick={handleCreate}
              className="bg-[#4F46E5] hover:bg-[#4338CA] text-white shadow-sm font-medium"
              disabled={!projectName || isNameInvalid || isDescInvalid || loading}
            >
              {loading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : <CheckCircle2 className="h-4 w-4 mr-2" />}
              Publish
            </Button>
          </div>

        </div>

        {/* Quick Actions (Right Column, span 1) */}
        <div className="lg:col-span-1 flex flex-col gap-4">
          <div className="flex items-center pb-2 mb-2 lg:border-b border-gray-200">
            <h2 className="text-[13px] font-semibold text-gray-900 uppercase tracking-wider">Quick Actions</h2>
          </div>
          
          <Button variant="outline" className="w-full justify-start text-gray-700 hover:text-gray-900 hover:bg-gray-50 bg-white border-gray-200 font-medium px-4 shadow-[0_1px_2px_rgba(0,0,0,0.02)] h-10 transition-colors">
            <UploadCloud className="h-[18px] w-[18px] mr-3 text-gray-400" />
            Upload Template
          </Button>
          <Button variant="outline" className="w-full justify-start text-gray-700 hover:text-gray-900 hover:bg-gray-50 bg-white border-gray-200 font-medium px-4 shadow-[0_1px_2px_rgba(0,0,0,0.02)] h-10 transition-colors">
            <Users className="h-[18px] w-[18px] mr-3 text-gray-400" />
            Invite Team
          </Button>
          <Button variant="outline" className="w-full justify-start text-gray-700 hover:text-gray-900 hover:bg-gray-50 bg-white border-gray-200 font-medium px-4 shadow-[0_1px_2px_rgba(0,0,0,0.02)] h-10 transition-colors">
            <Settings className="h-[18px] w-[18px] mr-3 text-gray-400" />
            Advanced Settings
          </Button>
        </div>
      </div>
    </div>
  );
}
