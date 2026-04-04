'use client';

import { Input } from '../../ui/input';
import React from 'react';

interface FormFieldProps {
  name: string;
  label: string;
  type?: 'text' | 'email' | 'password' | 'number' | 'textarea' | 'select';
  placeholder?: string;
  value: string | number;
  onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
  onBlur?: (e: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  error?: string | null;
  required?: boolean;
  disabled?: boolean;
  description?: string;
  children?: React.ReactNode;
  helperText?: string;
}

export function FormField({
  name,
  label,
  type = 'text',
  placeholder,
  value,
  onChange,
  onBlur,
  error,
  required,
  disabled,
  description,
  children,
  helperText,
}: FormFieldProps) {
  const isTextarea = type === 'textarea';
  const isSelect = type === 'select';

  return (
    <div className="flex flex-col gap-2">
      <div className="flex items-baseline justify-between">
        <label
          htmlFor={name}
          className="text-[14px] font-medium text-[rgb(var(--text-main))] block"
        >
          {label}
          {required && <span className="text-[#EF4444] ml-1">*</span>}
        </label>
        {description && (
          <span className="text-[12px] text-[rgb(var(--text-sub))]">{description}</span>
        )}
      </div>

      {isTextarea ? (
        <textarea
          id={name}
          name={name}
          value={value}
          onChange={onChange as any}
          onBlur={onBlur as any}
          placeholder={placeholder}
          disabled={disabled}
          className={`px-3 py-2 rounded-lg border bg-[rgb(var(--card))] text-[rgb(var(--text-main))] placeholder:text-[rgb(var(--text-sub))] focus:outline-none focus:ring-2 transition-all ${
            error
              ? 'border-[#EF4444] focus:ring-[#EF4444]/30'
              : 'border-[rgb(var(--border))] focus:ring-[rgb(var(--accent))]/30 focus:border-[rgb(var(--accent))]'
          } disabled:opacity-50 disabled:cursor-not-allowed min-h-[100px]`}
        />
      ) : isSelect ? (
        <select
          id={name}
          name={name}
          value={value}
          onChange={onChange as any}
          onBlur={onBlur as any}
          disabled={disabled}
          className={`px-3 py-2 rounded-lg border bg-[rgb(var(--card))] text-[rgb(var(--text-main))] focus:outline-none focus:ring-2 transition-all ${
            error
              ? 'border-[#EF4444] focus:ring-[#EF4444]/30'
              : 'border-[rgb(var(--border))] focus:ring-[rgb(var(--accent))]/30 focus:border-[rgb(var(--accent))]'
          } disabled:opacity-50 disabled:cursor-not-allowed`}
        >
          {children}
        </select>
      ) : (
        <Input
          id={name}
          name={name}
          type={type}
          value={value}
          onChange={onChange}
          onBlur={onBlur}
          placeholder={placeholder}
          disabled={disabled}
          className={`border ${
            error ? 'border-[#EF4444] focus-visible:ring-[#EF4444]/30' : ''
          }`}
        />
      )}

      {error && <p className="text-[12px] text-[#EF4444]">{error}</p>}
      {helperText && !error && <p className="text-[12px] text-[rgb(var(--text-sub))]">{helperText}</p>}
    </div>
  );
}
