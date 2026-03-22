'use client';

import Link from 'next/link';
import { ArrowLeft, Save, X } from 'lucide-react';
import { Button } from '../../ui/button';
import { Input } from '../../ui/input';

export default function NewProjectPage() {
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
        <div className="flex items-center gap-2">
          <Link href="/projects">
            <Button variant="outline" size="sm" className="h-8 border-gray-200 text-gray-700 bg-white hover:bg-gray-50">
              <X className="h-4 w-4 mr-2" />
              Cancel
            </Button>
          </Link>
          <Button className="bg-[#4F46E5] hover:bg-[#4338CA] text-white shadow-sm font-medium">
            <Save className="h-4 w-4 mr-2" />
            Create Project
          </Button>
        </div>
      </div>

      {/* Form Content */}
      <div className="flex flex-col gap-8">
        <div>
          <h1 className="text-3xl font-semibold tracking-tight text-gray-900 mb-2">
            Create New Project
          </h1>
          <p className="text-gray-600">Set up a new project and start collaborating with your team.</p>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex flex-col gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-900 mb-2">
                Project Name *
              </label>
              <Input 
                placeholder="Enter project name"
                className="focus:ring-2 focus:ring-[#4F46E5]"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-900 mb-2">
                Description
              </label>
              <textarea 
                className="w-full h-32 px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#4F46E5] focus:border-transparent"
                placeholder="Enter project description"
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-900 mb-2">
                  Status
                </label>
                <select className="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#4F46E5] focus:border-transparent">
                  <option value="draft">Draft</option>
                  <option value="active">Active</option>
                  <option value="archived">Archived</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-900 mb-2">
                  Organization
                </label>
                <select className="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#4F46E5] focus:border-transparent">
                  <option value="">Select organization</option>
                  <option value="org1">NOX Team</option>
                  <option value="org2">Marketing Dept</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-900 mb-2">
                Tags
              </label>
              <Input 
                placeholder="Enter tags separated by commas"
                className="focus:ring-2 focus:ring-[#4F46E5]"
              />
              <p className="text-xs text-gray-500 mt-1">e.g., web, frontend, dashboard</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
