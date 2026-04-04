'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';

type ThemeMode = 'light' | 'dark';

interface ThemeContextType {
  mode: ThemeMode;
  toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [mode, setMode] = useState<ThemeMode>('light');

  useEffect(() => {
    const stored = window.localStorage.getItem('nox-theme') as ThemeMode | null;
    if (stored === 'light' || stored === 'dark') {
      setMode(stored);
      return;
    }

    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    setMode(prefersDark ? 'dark' : 'light');
  }, []);

  useEffect(() => {
    window.localStorage.setItem('nox-theme', mode);
    document.documentElement.classList.toggle('dark', mode === 'dark');
  }, [mode]);

  const toggleTheme = () => {
    setMode((prev) => (prev === 'light' ? 'dark' : 'light'));
  };

  return <ThemeContext.Provider value={{ mode, toggleTheme }}>{children}</ThemeContext.Provider>;
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within ThemeProvider');
  }
  return context;
};
