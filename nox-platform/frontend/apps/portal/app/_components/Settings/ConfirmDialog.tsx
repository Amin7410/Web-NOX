'use client';

import { Button } from '../../ui/button';
import React from 'react';

interface ConfirmDialogProps {
  isOpen: boolean;
  title: string;
  description: string;
  confirmText?: string;
  cancelText?: string;
  isDangerous?: boolean;
  isLoading?: boolean;
  onConfirm: () => void | Promise<void>;
  onCancel: () => void;
}

export function ConfirmDialog({
  isOpen,
  title,
  description,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  isDangerous = false,
  isLoading = false,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-[rgb(var(--card))] rounded-lg border border-[rgb(var(--border))] p-6 max-w-sm w-full mx-4 shadow-lg">
        <h2 className="text-[18px] font-semibold text-[rgb(var(--text-main))] mb-2">
          {title}
        </h2>
        <p className="text-[14px] text-[rgb(var(--text-sub))] mb-6">{description}</p>

        <div className="flex gap-3 justify-end">
          <Button
            variant="outline"
            onClick={onCancel}
            disabled={isLoading}
            className="border-[rgb(var(--border))] text-[rgb(var(--text-main))]"
          >
            {cancelText}
          </Button>
          <Button
            onClick={onConfirm}
            disabled={isLoading}
            className={
              isDangerous
                ? 'bg-[#EF4444] hover:bg-[#DC2626] text-white'
                : 'bg-[rgb(var(--accent))] hover:bg-[rgb(var(--accent))]/90 text-[rgb(var(--accent-foreground))]'
            }
          >
            {isLoading ? 'Loading...' : confirmText}
          </Button>
        </div>
      </div>
    </div>
  );
}
