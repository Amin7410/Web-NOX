'use client';

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "./ui/button";
import { Avatar, AvatarFallback, AvatarImage } from "./ui/avatar";
import { Moon, Sun } from "lucide-react";
import { useTheme } from "./ThemeProvider";

export function Header() {
  const pathname = usePathname();
  const { mode, toggleTheme } = useTheme();
  
  return (
    <header className="bg-[rgb(var(--surface))] border-b border-[rgb(var(--border))] sticky top-0 z-50 shadow-sm">
      <div className="max-w-[1440px] mx-auto px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Left: Logo */}
          <div className="flex items-center">
            <Link href="/" className="flex items-center space-x-3">
              <div className="w-8 h-8 bg-[rgb(var(--accent))] rounded-lg flex items-center justify-center">
                <span className="text-[rgb(var(--accent-foreground))] font-bold text-lg">N</span>
              </div>
              <span className="text-xl font-bold text-[rgb(var(--text-main))]">NOX</span>
            </Link>
          </div>

          {/* Center: Navigation */}
          <nav className="hidden md:flex items-center gap-1">
            <Link href="/projects">
              <Button 
                variant="ghost" 
                className={`text-sm font-medium transition-all ${
                  pathname === '/projects' || pathname === '/'
                    ? 'bg-[rgb(var(--accent))] text-[rgb(var(--accent-foreground))] shadow-sm' 
                    : 'text-[rgb(var(--text-main))] hover:text-[rgb(var(--accent))] hover:bg-[rgb(var(--surface))]'
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
                    ? 'bg-[rgb(var(--accent))] text-[rgb(var(--accent-foreground))] shadow-sm' 
                    : 'text-[rgb(var(--text-main))] hover:text-[rgb(var(--accent))] hover:bg-[rgb(var(--surface))]'
                }`}
              >
                Organizations
              </Button>
            </Link>
          </nav>

          {/* Right: User Menu */}
          <div className="flex items-center gap-4">
            <Button
              variant="ghost"
              size="icon"
              className="h-9 w-9 text-[rgb(var(--text-main))] hover:text-[rgb(var(--accent))] transition-colors"
              onClick={toggleTheme}
              aria-label="Toggle theme"
            >
              {mode === "dark" ? <Sun className="h-[18px] w-[18px]" /> : <Moon className="h-[18px] w-[18px]" />}
            </Button>
            <Button variant="ghost" size="icon" className="h-9 w-9 text-[rgb(var(--text-main))] hover:text-[rgb(var(--accent))] transition-colors">
              <svg className="h-[18px] w-[18px]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158 11.016 11.016 0 009.09 9.754l-5.405-5.405a2.032 2.032 0 00-2.828 0l-5.405 5.405a2.032 2.032 0 00-.828 2.828l5.405 5.405a2.032 2.032 0 002.828 0l1.405 1.405M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </Button>
            <div className="flex items-center gap-3 pl-2 border-l border-[rgb(var(--border))]">
              <Avatar className="h-9 w-9 cursor-pointer hover:ring-2 hover:ring-[rgb(var(--accent))]/20 transition-all shadow-sm">
                <AvatarImage 
                  src="https://images.unsplash.com/photo-1655249481446-25d575f1c054?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxwcm9mZXNzaW9uYWwlMjBidXNpbmVzcyUyMHBlcnNvbiUyMHBvcnRyYWl0fGVufDF8fHx8MTc3MzgyODg4OXww&ixlib=rb-4.1.0&q=80&w=1080"
                  alt="User avatar" 
                />
                <AvatarFallback className="bg-[#4F46E5] text-white font-medium">
                  U
                </AvatarFallback>
              </Avatar>
              <Button variant="outline" size="sm" className="hidden lg:flex h-9 border-[rgb(var(--border))] text-[rgb(var(--muted-foreground))] font-medium hover:bg-[rgb(var(--surface))] hover:text-[rgb(var(--text-main))] shadow-sm px-4">
                Sign out
              </Button>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}
