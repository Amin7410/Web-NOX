'use client';

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "./ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";

export function Header() {
  const pathname = usePathname();
  
  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
      <div className="max-w-[1440px] mx-auto px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Left: Logo */}
          <div className="flex items-center">
            <Link href="/" className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-[#4F46E5] rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-lg">N</span>
              </div>
              <span className="text-xl font-bold text-gray-900">NOX</span>
            </Link>
          </div>

          {/* Center: Navigation */}
          <nav className="hidden md:flex items-center gap-1">
            <Link href="/projects">
              <Button 
                variant="ghost" 
                className={`text-sm font-medium transition-all ${
                  pathname === '/projects' || pathname === '/'
                    ? 'bg-gray-100 text-gray-900 shadow-sm' 
                    : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                }`}
              >
                Projects
              </Button>
            </Link>
            <Link href="/organizations">
              <Button 
                variant="ghost" 
                className={`text-sm font-medium transition-all ${
                  pathname === '/organizations'
                    ? 'bg-gray-100 text-gray-900 shadow-sm' 
                    : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                }`}
              >
                Organizations
              </Button>
            </Link>
          </nav>

          {/* Right: User Menu */}
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" className="h-8 w-8 text-gray-500 hover:text-gray-900">
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158 11.016 11.016 0 009.09 9.754l-5.405-5.405a2.032 2.032 0 00-2.828 0l-5.405 5.405a2.032 2.032 0 00-.828 2.828l5.405 5.405a2.032 2.032 0 002.828 0l1.405 1.405M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </Button>
            <div className="relative">
              <Avatar className="h-9 w-9 cursor-pointer hover:ring-2 hover:ring-[#4F46E5]/20 transition-all">
                <AvatarImage 
                  src="https://images.unsplash.com/photo-1655249481446-25d575f1c054?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwcm9mZXNzaW9uYWwlMjBidXNpbmVzcyUyMHBlcnNvbiUyMHBvcnRyYWl0fGVufDF8fHx8MTc3MzgyODg4OXww&ixlib=rb-4.1.0&q=80&w=1080"
                  alt="User avatar" 
                />
                <AvatarFallback className="bg-[#4F46E5] text-white">
                  U
                </AvatarFallback>
              </Avatar>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
