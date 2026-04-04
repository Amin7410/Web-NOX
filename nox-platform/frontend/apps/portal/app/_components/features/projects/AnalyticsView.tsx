'use client';

import { useParams } from 'next/navigation';
import Link from 'next/link';
import { ArrowLeft, TrendingUp, Users, Activity, Clock } from 'lucide-react';
import { Button } from '../../../ui/button';
import { useState, useEffect } from 'react';

interface AnalyticsData {
  type: 'trending' | 'users' | 'activity' | 'clock';
  value: string | number;
  label: string;
  change: string;
  color: string;
}

export function AnalyticsView() {
  const params = useParams();
  const projectId = params.projectId as string;
  
  const [analytics, setAnalytics] = useState<AnalyticsData[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        const response = await fetch(`/api/v1/projects/${projectId}/analytics`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        });

        if (response.ok) {
          const data = await response.json();
          setAnalytics(data.data || [
            { type: 'trending', value: '0', label: 'Total Views', change: '+0%', color: 'text-blue-500' },
            { type: 'users', value: '0', label: 'Active Users', change: '+0%', color: 'text-green-500' },
            { type: 'activity', value: '0', label: 'Total Actions', change: '+0%', color: 'text-purple-500' },
            { type: 'clock', value: '0h', label: 'Time Spent', change: '+0%', color: 'text-orange-500' },
          ]);
        } else {
          // Set placeholder data if API fails
          setAnalytics([
            { type: 'trending', value: '--', label: 'Total Views', change: '--', color: 'text-blue-500' },
            { type: 'users', value: '--', label: 'Active Users', change: '--', color: 'text-green-500' },
            { type: 'activity', value: '--', label: 'Total Actions', change: '--', color: 'text-purple-500' },
            { type: 'clock', value: '--', label: 'Time Spent', change: '--', color: 'text-orange-500' },
          ]);
        }
      } catch (err) {
        console.error('Failed to fetch analytics:', err);
        // Set placeholder data on error
        setAnalytics([
          { type: 'trending', value: '--', label: 'Total Views', change: '--', color: 'text-blue-500' },
          { type: 'users', value: '--', label: 'Active Users', change: '--', color: 'text-green-500' },
          { type: 'activity', value: '--', label: 'Total Actions', change: '--', color: 'text-purple-500' },
          { type: 'clock', value: '--', label: 'Time Spent', change: '--', color: 'text-orange-500' },
        ]);
        setError('Unable to load analytics data');
      } finally {
        setLoading(false);
      }
    };

    fetchAnalytics();
  }, [projectId]);

  return (
    <div className="flex flex-col w-full max-w-5xl mx-auto pb-16 p-6">
      {/* Top Navigation Bar */}
      <div className="flex items-center justify-between mb-8">
        <Link 
          href={`/projects/${projectId}`}
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

      {error && (
        <div className="mb-6 p-4 bg-amber-50 border border-amber-200 rounded-lg text-amber-700 text-sm">
          {error}
        </div>
      )}

      {/* Analytics Cards */}
      {loading ? (
        <div className="text-gray-500 py-12">Loading analytics...</div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            {analytics.map((item, index) => {
              let Icon = TrendingUp;
              if (item.type === 'users') Icon = Users;
              if (item.type === 'activity') Icon = Activity;
              if (item.type === 'clock') Icon = Clock;

              return (
                <div key={index} className="bg-white rounded-xl border border-gray-200 p-6">
                  <div className="flex items-center justify-between mb-4">
                    <Icon className={`h-6 w-6 ${item.color}`} />
                    <span className={`text-sm font-medium ${
                      typeof item.change === 'string' && item.change.startsWith('+') ? 'text-green-600' : 'text-orange-600'
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
                <p className="text-gray-500">Chart implementation pending - backend data incoming</p>
              </div>
            </div>
            
            <div className="bg-white rounded-xl border border-gray-200 p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Team Performance</h3>
              <div className="h-64 bg-gray-50 rounded-lg flex items-center justify-center">
                <p className="text-gray-500">Chart implementation pending - backend data incoming</p>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
