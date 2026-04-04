'use client';

import React from 'react';

interface SettingsSectionProps {
  title: string;
  description?: string;
  children: React.ReactNode;
  border?: boolean;
}

export function SettingsSection({
  title,
  description,
  children,
  border = true,
}: SettingsSectionProps) {
  return (
    <div className={`${border ? 'border-b border-[rgb(var(--border))] pb-8 mb-8' : ''}`}>
      <div className="mb-6">
        <h2 className="text-[18px] font-semibold text-[rgb(var(--text-main))] mb-1">
          {title}
        </h2>
        {description && (
          <p className="text-[14px] text-[rgb(var(--text-sub))]">{description}</p>
        )}
      </div>
      <div>{children}</div>
    </div>
  );
}
