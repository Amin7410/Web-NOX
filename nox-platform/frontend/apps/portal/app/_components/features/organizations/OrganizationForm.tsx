'use client';

import { useState } from "react";
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { 
  ArrowLeft, Building2, Globe, Shield, 
  CheckCircle2, Info, Layout, Users, ExternalLink, Loader2
} from 'lucide-react';
import { Button } from '../../../ui/button';
import { Input } from '../../../ui/input';
import { Label } from '../../../ui/label';
import { Badge } from '../../../ui/badge';
export function OrganizationForm() {
  const router = useRouter();
  const [orgName, setOrgName] = useState("");
  const [slug, setSlug] = useState("");
  const [plan, setPlan] = useState("free");
  const [loading, setLoading] = useState(false);
  
  const handleNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const name = e.target.value;
    setOrgName(name);
    // Auto-generate slug if empty or matching previous name
    if (!slug || slug === orgName.toLowerCase().replace(/[^a-z0-9]+/g, '-')) {
      setSlug(name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, ''));
    }
  };

  const handleCreate = async () => {
    if (!orgName || !slug) return;
    setLoading(true);
    try {
      const res = await fetch('/api/orgs', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: orgName, slug }),
      });
      const data = await res.json().catch(() => ({}));

      if (res.ok && data.data) {
        router.push(`/organizations`);
      } else {
        console.error('Failed to create organization', data);
        alert(data.message || 'Failed to create organization');
      }
    } catch (error) {
      console.error('Create organization exception:', error);
      alert('Internal Server Error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col w-full max-w-4xl mx-auto pb-16 p-6">
      {/* Top Navigation */}
      <div className="flex items-center justify-between mb-8">
        <Link 
          href="/organizations" 
          className="inline-flex items-center text-sm font-semibold text-gray-500 hover:text-gray-900 transition-colors"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Teams
        </Link>
        <div className="flex items-center gap-3">
          <Button 
            variant="outline" 
            onClick={() => router.push('/organizations')}
            className="border-gray-200 text-gray-700 bg-white hover:bg-gray-50 h-9 font-semibold shadow-sm"
          >
            Cancel
          </Button>
          <Button 
            onClick={handleCreate}
            className="bg-[#4F46E5] hover:bg-[#4338CA] text-white shadow-md font-semibold h-9 px-6 transition-all"
            disabled={!orgName || !slug || loading}
          >
            {loading ? <Loader2 className="h-4 w-4 animate-spin mr-2" /> : null}
            Create Organization
          </Button>
        </div>
      </div>

      {/* Header Info */}
      <div className="flex flex-col gap-2 mb-10">
        <h1 className="text-3xl font-bold tracking-tight text-gray-900">
          Create Organization
        </h1>
        <p className="text-[15px] font-medium text-gray-500">
          Set up a workspace to collaborate with your team and manage shared projects.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
        {/* Main Form Area */}
        <div className="lg:col-span-8 flex flex-col gap-8">
          
          <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
            <div className="p-6 sm:p-8 flex flex-col gap-8">
              
              <div className="flex items-center gap-3 pb-4 border-b border-gray-100">
                <div className="h-10 w-10 rounded-xl bg-[#4F46E5]/5 flex items-center justify-center text-[#4F46E5]">
                  <Building2 className="h-5 w-5" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-gray-900 leading-tight">Team Profile</h2>
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest mt-0.5">Basic Information</p>
                </div>
              </div>
              
              <div className="grid gap-8">
                {/* Organization Name */}
                <div className="flex flex-col gap-2.5">
                  <Label htmlFor="orgName" className="text-gray-900 font-bold text-sm tracking-tight">
                    Organization Name <span className="text-[#EF4444]">*</span>
                  </Label>
                  <Input 
                    id="orgName"
                    placeholder="e.g. Acme Corp, Design Ops..." 
                    value={orgName}
                    onChange={handleNameChange}
                    className="bg-white border-gray-300 focus-visible:ring-[#4F46E5] focus-visible:border-[#4F46E5] transition-all h-11 text-[15px] shadow-sm placeholder:text-gray-300"
                  />
                  <p className="text-[11px] font-medium text-gray-500 mt-0.5">This is your team's visible name across the platform.</p>
                </div>

                {/* Slug */}
                <div className="flex flex-col gap-2.5">
                  <Label htmlFor="slug" className="text-gray-900 font-bold text-sm tracking-tight">
                    Slug / URL Identifier <span className="text-[#EF4444]">*</span>
                  </Label>
                  <div className="relative">
                    <div className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 font-medium text-sm border-r border-gray-200 pr-2 pointer-events-none">
                      nox.app/
                    </div>
                    <Input 
                      id="slug"
                      placeholder="acme-corp" 
                      value={slug}
                      onChange={(e) => setSlug(e.target.value.toLowerCase().replace(/[^a-z0-9-]+/g, ''))}
                      className="pl-[78px] bg-white border-gray-300 focus-visible:ring-[#4F46E5] focus-visible:border-[#4F46E5] transition-all h-11 shadow-sm font-semibold text-[#4F46E5]"
                    />
                  </div>
                  <p className="text-[11px] font-medium text-gray-500 mt-0.5">Used for your organization's unique URL. Lowercase, numbers and hyphens only.</p>
                </div>
              </div>

              {/* Plans Selection */}
              <div className="flex flex-col gap-4 pt-4">
                <Label className="text-gray-900 font-bold text-sm tracking-tight">Select Plan</Label>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div 
                    onClick={() => setPlan('free')}
                    className={`cursor-pointer rounded-xl border-2 p-4 transition-all ${
                      plan === 'free' ? 'border-[#4F46E5] bg-[#4F46E5]/5 shadow-sm' : 'border-gray-100 bg-gray-50/50 hover:border-gray-200'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-2">
                      <span className="font-bold text-sm">Free</span>
                      {plan === 'free' && <CheckCircle2 className="h-4 w-4 text-[#4F46E5]" />}
                    </div>
                    <p className="text-[11px] font-medium text-gray-500 leading-normal">Perfect for personal projects and small teams up to 5 members.</p>
                  </div>
                  <div 
                    onClick={() => setPlan('pro')}
                    className={`cursor-pointer rounded-xl border-2 p-4 transition-all ${
                      plan === 'pro' ? 'border-[#4F46E5] bg-[#4F46E5]/5 shadow-sm' : 'border-gray-100 bg-gray-50/50 hover:border-gray-200'
                    }`}
                  >
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <span className="font-bold text-sm">Pro</span>
                        <Badge className="bg-amber-100 text-amber-700 text-[9px] uppercase tracking-tighter border-0 font-bold">$19/mo</Badge>
                      </div>
                      {plan === 'pro' && <CheckCircle2 className="h-4 w-4 text-[#4F46E5]" />}
                    </div>
                    <p className="text-[11px] font-medium text-gray-500 leading-normal">Advanced analytics, unlimited projects and up to 50 team members.</p>
                  </div>
                </div>
              </div>
            </div>

            <div className="bg-gray-50/80 px-8 py-5 border-t border-gray-100 flex items-center justify-between">
              <div className="flex items-center gap-2 text-gray-500">
                <Shield className="h-4 w-4" />
                <span className="text-xs font-bold uppercase tracking-widest">Enterprise Security</span>
              </div>
              <Button 
                variant="ghost" 
                size="sm" 
                className="text-gray-500 hover:text-gray-900 font-bold text-xs uppercase tracking-widest h-auto p-0 hover:bg-transparent"
              >
                Learn More
              </Button>
            </div>
          </div>
        </div>

        {/* Sidebar Help */}
        <div className="lg:col-span-4 flex flex-col gap-6">
          <div className="bg-[#4F46E5] rounded-2xl p-6 text-white shadow-lg shadow-[#4F46E5]/20">
            <div className="h-10 w-10 rounded-xl bg-white/20 flex items-center justify-center mb-4">
              <Info className="h-5 w-5" />
            </div>
            <h3 className="text-lg font-bold mb-2 leading-tight">Why Organizations?</h3>
            <p className="text-sm text-white/80 font-medium leading-relaxed mb-6">
              Organizations allow you to group multiple projects and manage access for your entire team in one central place.
            </p>
            <div className="space-y-3">
              <div className="flex items-center gap-3 bg-white/10 rounded-lg p-2.5">
                <Users className="h-4 w-4 text-white/70" />
                <span className="text-xs font-bold tracking-tight">Centralized Billing</span>
              </div>
              <div className="flex items-center gap-3 bg-white/10 rounded-lg p-2.5">
                <Globe className="h-4 w-4 text-white/70" />
                <span className="text-xs font-bold tracking-tight">Shared Domains</span>
              </div>
              <div className="flex items-center gap-3 bg-white/10 rounded-lg p-2.5">
                <Layout className="h-4 w-4 text-white/70" />
                <span className="text-xs font-bold tracking-tight">Project Templates</span>
              </div>
            </div>
          </div>
          
          <div className="bg-white rounded-2xl border border-gray-200 p-6 flex flex-col gap-4 shadow-sm">
            <h4 className="text-sm font-bold text-gray-900 uppercase tracking-widest border-b border-gray-50 pb-2">Questions?</h4>
            <p className="text-[13px] text-gray-500 font-medium leading-relaxed">
              Our support team is available 24/7 to help you set up your workspace and migrate existing data.
            </p>
            <Button variant="outline" className="w-full justify-between h-10 border-gray-200 text-gray-600 font-bold text-xs uppercase tracking-widest hover:bg-gray-50 shadow-sm transition-all group">
              Contact Support
              <ExternalLink className="h-3 w-3 text-gray-300 group-hover:text-gray-500 transition-colors" />
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
