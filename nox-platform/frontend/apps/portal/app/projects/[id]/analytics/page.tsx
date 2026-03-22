'use client';

import { useParams } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, TrendingUp, Users, Activity, Clock } from 'lucide-react';
import { Button } from '../../../ui/button';

export default function AnalyticsPage() {
  const params = useParams();
  const id = params.id as string;

  const analyticsData = [
    { label: "Total Views", value: "1,234", change: "+12%", icon: TrendingUp, color: "text-blue-500" },
    { label: "Active Members", value: "5", change: "+1", icon: Users, color: "text-green-500" },
    { label: "Tasks Completed", value: "12", change: "+3", icon: Activity, color: "text-purple-500" },
    { label: "Avg. Response Time", value: "2.5h", change: "-0.5h", icon: Clock, color: "text-orange-500" }
  ];

  return (
    <div className="flex flex-col w-full max-w-5xl mx-auto pb-16 p-6">
      {/* Top Navigation Bar */}
      <div className="flex items-center justify-between mb-8">
        <Link 
          href={`/projects/${id}`}
          className="inline-flex items-center text-sm font-medium text-gray-500 hover:text-gray-900 transition-colors"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back to Project
        </Link>
        <Button variant="outline" className="border-gray-200 text-gray-700 bg-white hover:bg-gray-50">
          Export Report
        </Button>
      </div>

      {/* Header */}
      <div className="flex flex-col gap-4 mb-8">
        <h1 className="text-3xl font-semibold tracking-tight text-gray-900">
          Project Analytics
        </h1>
        <p className="text-gray-600">Track your project performance and engagement metrics.</p>
      </div>

      {/* Analytics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        {analyticsData.map((item, index) => {
          const Icon = item.icon;
          return (
            <div key={index} className="bg-white rounded-xl border border-gray-200 p-6">
              <div className="flex items-center justify-between mb-4">
                <Icon className={`h-6 w-6 ${item.color}`} />
                <span className={`text-sm font-medium ${
                  item.change.startsWith('+') ? 'text-green-600' : 'text-orange-600'
                }`}>
                  {item.change}
                </span>
              </div>
              <div className="flex flex-col gap-1">
                <span className="text-2xl font-bold text-gray-900">{item.value}</span>
                <span className="text-sm text-gray-500">{item.label}</span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Activity Overview</h3>
          <div className="h-64 bg-gray-50 rounded-lg flex items-center justify-center">
            <p className="text-gray-500">Chart placeholder</p>
          </div>
        </div>
        
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Team Performance</h3>
          <div className="h-64 bg-gray-50 rounded-lg flex items-center justify-center">
            <p className="text-gray-500">Chart placeholder</p>
          </div>
        </div>
      </div>
    </div>
  );
}
