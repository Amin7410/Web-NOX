'use client';

import { useParams } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, Save, X } from 'lucide-react';
import { Button } from '../../../ui/button';
import { Input } from '../../../ui/input';

export default function EditProjectPage() {
  const params = useParams();
  const id = params.id as string;

  return (
    <div className="flex flex-col w-full max-w-4xl mx-auto pb-16 p-6">
      {/* Top Navigation Bar */}
      <div className="flex items-center justify-between mb-8">
        <Link 
          href={`/projects/${id}`}
          className="inline-flex items-center text-sm font-medium text-gray-500 hover:text-gray-900 transition-colors"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Project
        </Link>
        <div className="flex items-center gap-2">
          <Link href={`/projects/${id}`}>
            <Button variant="outline" size="sm" className="h-8 border-gray-200 text-gray-700 bg-white hover:bg-gray-50">
              <X className="h-4 w-4 mr-2" />
              Cancel
            </Button>
          </Link>
          <Button className="bg-[#4F46E5] hover:bg-[#4338CA] text-white shadow-sm font-medium">
            <Save className="h-4 w-4 mr-2" />
            Save Changes
          </Button>
        </div>
      </div>

      {/* Form Content */}
      <div className="flex flex-col gap-8">
        <div>
          <h1 className="text-3xl font-semibold tracking-tight text-gray-900 mb-2">
            Edit Project
          </h1>
          <p className="text-gray-600">Update your project information and settings.</p>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex flex-col gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-900 mb-2">
                Project Name
              </label>
              <Input 
                placeholder="Enter project name"
                defaultValue="Website Redesign"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-900 mb-2">
                Description
              </label>
              <textarea 
                className="w-full h-32 px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#4F46E5] focus:border-transparent"
                placeholder="Enter project description"
                defaultValue="A complete overhaul of our marketing website to improve conversion rates and update the brand visual language."
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-900 mb-2">
                Status
              </label>
              <select className="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-[#4F46E5] focus:border-transparent">
                <option value="active" selected>Active</option>
                <option value="archived">Archived</option>
                <option value="draft">Draft</option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
