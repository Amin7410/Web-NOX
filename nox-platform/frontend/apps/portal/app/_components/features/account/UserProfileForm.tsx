'use client';

import { useState, useEffect } from "react";
import { 
  User, Shield, Smartphone, Globe, 
  Camera, CheckCircle2, Lock, Bell
} from 'lucide-react';
import { Button } from "../../../ui/button";
import { Input } from "../../../ui/input";
import { Avatar, AvatarImage, AvatarFallback } from "../../../ui/avatar";
import { Alert } from "../../../_components/UiBits";
import { useRouter } from "next/navigation";

export function UserProfileForm() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const [profile, setProfile] = useState({
      fullName: "",
      email: "",
      avatarUrl: ""
  });

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        alert("File size exceeds 5MB limit. Please choose a smaller file.");
        e.target.value = ''; // Reset
        return;
      }
      const reader = new FileReader();
      reader.onloadend = () => {
        setProfile(prev => ({ ...prev, avatarUrl: reader.result as string }));
      };
      reader.readAsDataURL(file);
    }
  };

  useEffect(() => {
      const fetchProfile = async () => {
          try {
              const res = await fetch('/api/auth/me');
              if (res.status === 401) {
                  router.push("/auth/login");
                  return;
              }
              const data = await res.json();
              if (res.ok) {
                  setProfile({
                      fullName: data.data.fullName || "",
                      email: data.data.email || "",
                      avatarUrl: data.data.avatarUrl || ""
                  });
              } else {
                  setError("Failed to load profile data");
              }
          } catch (err) {
              setError("An error occurred while loading profile");
          } finally {
              setLoading(false);
          }
      };

      fetchProfile();
  }, [router]);

  const handleSave = async (e: React.FormEvent) => {
      e.preventDefault();
      setSaving(true);
      setError(null);
      setSuccess(false);
      
      try {
        const res = await fetch('/api/auth/me', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(profile),
        });
        
        if (res.ok) {
          setSuccess(true);
          setTimeout(() => { window.location.reload(); }, 1500);
        } else {
          setError("Failed to update profile");
        }
      } catch (err) {
        setError("Network error. Please try again later.");
      } finally {
        setSaving(false);
      }
  };

  return (
    <div className="flex flex-col w-full max-w-4xl mx-auto pb-16 p-6">
      {/* Header Info */}
      <div className="flex flex-col gap-2 mb-10">
        <h1 className="text-3xl font-bold tracking-tight text-gray-900">
          Account Settings
        </h1>
        <p className="text-[15px] font-medium text-gray-500">
          Manage your personal information, security preferences, and notifications.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10">
        {/* Main Settings Area */}
        <div className="lg:col-span-8 flex flex-col gap-8">
          
          {/* Profile Section */}
          <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
            <div className="p-6 sm:p-8 flex flex-col gap-8">
              
              {error && (
                <div className="p-4 bg-red-50 text-red-600 rounded-lg border border-red-100 text-sm font-medium">
                  {error}
                </div>
              )}
              {success && (
                <div className="p-4 bg-green-50 text-green-600 rounded-lg border border-green-100 text-sm font-medium">
                  Profile updated successfully! Refreshing...
                </div>
              )}

              <div className="flex items-center gap-3 pb-4 border-b border-gray-100">
                <div className="h-10 w-10 rounded-xl bg-[#4F46E5]/5 flex items-center justify-center text-[#4F46E5]">
                  <User className="h-5 w-5" />
                </div>
                <div>
                  <h2 className="text-lg font-bold text-gray-900 leading-tight">Public Profile</h2>
                  <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest mt-0.5">Personal Identity</p>
                </div>
              </div>

              {/* Avatar Upload */}
              <div className="flex flex-col sm:flex-row items-center gap-6 pb-2">
                <div className="relative group">
                  <Avatar className="h-24 w-24 border-4 border-white shadow-md cursor-pointer group-hover:opacity-80 transition-opacity">
                    <AvatarImage src={profile.avatarUrl || "https://images.unsplash.com/photo-1655249481446-25d575f1c054?w=100&h=100&fit=crop"} />
                    <AvatarFallback className="bg-[#4F46E5] text-white text-xl font-bold">JD</AvatarFallback>
                  </Avatar>
                  <label 
                    htmlFor="avatar-upload" 
                    className="absolute bottom-0 right-0 h-8 w-8 rounded-full bg-white border border-gray-200 shadow-sm flex items-center justify-center cursor-pointer text-gray-500 hover:text-[#4F46E5] hover:border-[#4F46E5] transition-all"
                  >
                    <Camera className="h-4 w-4" />
                    <input id="avatar-upload" type="file" accept="image/png, image/jpeg, image/gif" className="hidden" onChange={handleImageUpload} />
                  </label>
                </div>
                <div className="flex flex-col gap-2 text-center sm:text-left">
                  <h3 className="text-sm font-bold text-gray-900">Profile Picture</h3>
                  <p className="text-xs font-medium text-gray-500 max-w-[200px]">JPG, GIF or PNG. Max size of 5MB.</p>
                  <div className="flex items-center gap-2 mt-1">
                    <Button 
                      type="button"
                      onClick={() => document.getElementById('avatar-upload')?.click()}
                      variant="outline" 
                      size="sm" 
                      className="h-8 text-xs font-bold uppercase tracking-widest border-gray-200 hover:bg-gray-50"
                    >
                      Upload New
                    </Button>
                    <Button 
                      type="button"
                      onClick={() => setProfile(prev => ({ ...prev, avatarUrl: "" }))}
                      variant="ghost" 
                      size="sm" 
                      className="h-8 text-xs font-bold uppercase tracking-widest text-[#EF4444] hover:bg-red-50 hover:text-[#EF4444]"
                    >
                      Remove
                    </Button>
                  </div>
                </div>
              </div>
              
              <form className="space-y-6" onSubmit={handleSave}>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-8">
                  {/* Full Name */}
                  <div className="flex flex-col gap-2.5">
                    <label htmlFor="fullName" className="text-gray-900 font-bold text-sm tracking-tight">Full Name</label>
                    <Input 
                      id="fullName"
                      value={profile.fullName}
                      onChange={(e) => setProfile({ ...profile, fullName: e.target.value })}
                      className="bg-white border-gray-300 focus-visible:ring-[#4F46E5] focus-visible:border-[#4F46E5] transition-all h-11 text-[15px] shadow-sm"
                    />
                  </div>

                  {/* Username (Placeholder for now, not in profile state) */}
                  <div className="flex flex-col gap-2.5">
                    <label htmlFor="username" className="text-gray-900 font-bold text-sm tracking-tight">Username</label>
                    <div className="relative">
                      <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 font-bold text-sm">@</span>
                      <Input 
                        id="username"
                        value="johndoe" // Placeholder
                        readOnly
                        className="pl-7 bg-gray-50 border-gray-200 text-gray-500 h-11 text-[15px] shadow-none cursor-not-allowed"
                      />
                    </div>
                  </div>

                  {/* Email (Read Only) */}
                  <div className="flex flex-col gap-2.5 sm:col-span-2">
                    <label htmlFor="email" className="text-gray-900 font-bold text-sm tracking-tight text-gray-400">Email Address</label>
                    <div className="relative group">
                      <Input 
                        id="email"
                        value={profile.email}
                        readOnly
                        className="bg-gray-50 border-gray-200 text-gray-500 h-11 text-[15px] shadow-none cursor-not-allowed pr-10"
                      />
                      <CheckCircle2 className="absolute right-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[#22C55E]" />
                    </div>
                    <p className="text-[11px] font-bold text-[#22C55E] uppercase tracking-widest mt-0.5">Verified Primary Email</p>
                  </div>
                </div>

                <div className="pt-4 flex items-center justify-end gap-3 border-t border-gray-50">
                  <Button variant="outline" className="h-10 border-gray-300 text-gray-600 font-bold text-xs uppercase tracking-widest hover:bg-gray-50">Reset Changes</Button>
                  <Button type="submit" className="h-10 bg-[#4F46E5] hover:bg-[#4338CA] text-white font-bold text-xs uppercase tracking-widest shadow-md px-6" disabled={saving}>
                    {saving ? "Saving..." : "Save Changes"}
                  </Button>
                </div>
              </form>
            </div>
          </div>

          {/* Security Summary Section */}
          <div className="bg-white rounded-2xl border border-gray-200 shadow-sm overflow-hidden">
             <div className="p-6 sm:p-8 flex flex-col gap-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="h-10 w-10 rounded-xl bg-amber-50 flex items-center justify-center text-amber-600">
                      <Lock className="h-5 w-5" />
                    </div>
                    <div>
                      <h2 className="text-lg font-bold text-gray-900 leading-tight">Security & Access</h2>
                      <p className="text-xs font-semibold text-gray-400 uppercase tracking-widest mt-0.5">Protection</p>
                    </div>
                  </div>
                  <Button variant="outline" size="sm" className="h-8 border-gray-200 text-gray-600 font-bold text-[10px] uppercase tracking-widest">Update Security</Button>
                </div>
                <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="flex items-center gap-3 p-4 bg-gray-50/50 rounded-xl border border-gray-100">
                    <Shield className="h-5 w-5 text-[#22C55E]" />
                    <div className="flex flex-col">
                      <span className="text-sm font-bold text-gray-900">Two-Factor Auth</span>
                      <span className="text-xs font-medium text-gray-500">Enabled - SMS & Authenticator</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 p-4 bg-gray-50/50 rounded-xl border border-gray-100">
                    <Smartphone className="h-5 w-5 text-gray-400" />
                    <div className="flex flex-col">
                      <span className="text-sm font-bold text-gray-900">Active Sessions</span>
                      <span className="text-xs font-medium text-gray-500">3 active logins detected</span>
                    </div>
                  </div>
                </div>
             </div>
          </div>
        </div>

        {/* Sidebar Nav */}
        <div className="lg:col-span-4 flex flex-col gap-4">
          <div className="bg-white rounded-2xl border border-gray-200 p-2 shadow-sm flex flex-col gap-1">
            <h3 className="px-4 pt-4 pb-2 text-[11px] font-bold text-gray-400 uppercase tracking-widest">Quick Navigation</h3>
            <button className="flex items-center gap-3 px-4 py-3 rounded-xl bg-[#4F46E5]/5 text-[#4F46E5] font-bold text-sm transition-all group">
              <User className="h-4 w-4" />
              Profile Information
              <CheckCircle2 className="ml-auto h-4 w-4 opacity-50" />
            </button>
            <button className="flex items-center gap-3 px-4 py-3 rounded-xl text-gray-600 hover:bg-gray-50 font-bold text-sm transition-all group">
              <Lock className="h-4 w-4" />
              Password & Security
            </button>
            <button className="flex items-center gap-3 px-4 py-3 rounded-xl text-gray-600 hover:bg-gray-50 font-bold text-sm transition-all group">
              <Bell className="h-4 w-4" />
              Notifications
            </button>
            <button className="flex items-center gap-3 px-4 py-3 rounded-xl text-gray-600 hover:bg-gray-50 font-bold text-sm transition-all group">
              <Globe className="h-4 w-4" />
              Connected Apps
            </button>
          </div>
          
          <div className="bg-red-50/50 rounded-2xl border border-red-100 p-6 flex flex-col gap-4">
            <h4 className="text-sm font-bold text-[#EF4444] uppercase tracking-widest border-b border-red-50 pb-2">Danger Zone</h4>
            <p className="text-xs text-red-600/70 font-medium leading-relaxed">
              Deleting your account is permanent and will remove all your access to organizations and projects.
            </p>
            <Button variant="ghost" className="w-full justify-start h-10 text-[#EF4444] hover:bg-red-100 hover:text-[#EF4444] font-bold text-xs uppercase tracking-widest transition-all">
              Delete My Account
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
