'use client';

import React, { useState } from 'react';
import { cn } from '../lib/utils';

interface ImageWithFallbackProps extends React.ImgHTMLAttributes<HTMLImageElement> {
  fallbackClassName?: string;
  fallbackIcon?: React.ReactNode;
}

export function ImageWithFallback({ 
  src, 
  alt, 
  className, 
  fallbackClassName,
  fallbackIcon,
  style,
  ...rest 
}: ImageWithFallbackProps) {
  const [hasError, setHasError] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const handleError = () => {
    setHasError(true);
    setIsLoading(false);
  };

  const handleLoad = () => {
    setIsLoading(false);
  };

  // Default fallback icon - simple and clean
  const defaultFallbackIcon = (
    <svg 
      className="w-8 h-8 text-gray-400" 
      fill="none" 
      stroke="currentColor" 
      viewBox="0 0 24 24"
    >
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" strokeWidth="2"/>
      <circle cx="8.5" cy="8.5" r="1.5" strokeWidth="2"/>
      <polyline points="21,15 16,10 5,21" strokeWidth="2"/>
    </svg>
  );

  if (hasError) {
    return (
      <div
        className={cn(
          "inline-flex items-center justify-center bg-gray-100 border border-gray-200 rounded-lg",
          className,
          fallbackClassName
        )}
        style={style}
      >
        {fallbackIcon || defaultFallbackIcon}
      </div>
    );
  }

  return (
    <div className="relative">
      {isLoading && (
        <div className="absolute inset-0 flex items-center justify-center bg-gray-100 rounded-lg">
          <div className="w-4 h-4 border-2 border-gray-300 border-t-[#4F46E5] rounded-full animate-spin"></div>
        </div>
      )}
      <img
        src={src}
        alt={alt}
        className={cn(
          "transition-opacity duration-200",
          isLoading ? "opacity-0" : "opacity-100",
          className
        )}
        style={style}
        onError={handleError}
        onLoad={handleLoad}
        {...rest}
      />
    </div>
  );
}
