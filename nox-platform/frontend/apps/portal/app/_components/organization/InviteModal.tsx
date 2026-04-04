'use client';

import { useState } from 'react';
import { Button } from '@nox/ui';
import { FormField } from '../Settings';
import { useForm } from '@/lib/hooks';
import { validators } from '@/lib/utils/validators';
import { X } from 'lucide-react';
import type { Role } from '@/lib/types';

interface InviteModalProps {
  isOpen: boolean;
  roles?: Role[];
  isLoading?: boolean;
  onSubmit: (email: string, roleId: string) => Promise<void>;
  onClose: () => void;
}

export function InviteModal({
  isOpen,
  roles = [],
  isLoading = false,
  onSubmit,
  onClose,
}: InviteModalProps) {
  const [selectedRole, setSelectedRole] = useState<string>('');

  const form = useForm({
    initialValues: {
      email: '',
    },
    onSubmit: async (values) => {
      if (!selectedRole) {
        console.warn('Please select a role');
        return;
      }
      await onSubmit(values.email, selectedRole);
      form.resetForm();
      setSelectedRole('');
      onClose();
    },
    validate: (values: any) => {
      const errors: any = {};
      
      const emailError = validators.required(values.email, 'Email') ||
        validators.email(values.email);
      if (emailError) errors.email = emailError;
      
      return errors;
    },
  });

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-[rgb(var(--card))] rounded-lg border border-[rgb(var(--border))] p-6 max-w-md w-full mx-4 shadow-lg">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-[18px] font-semibold text-[rgb(var(--text-main))]">
            Invite Member
          </h2>
          <button
            onClick={onClose}
            className="text-[rgb(var(--text-sub))] hover:text-[rgb(var(--text-main))] transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <form onSubmit={form.handleSubmit} className="flex flex-col gap-4">
          <FormField
            name="email"
            label="Email Address"
            type="email"
            placeholder="user@example.com"
            value={form.values.email}
            onChange={form.handleChange}
            onBlur={form.handleBlur}
            error={form.touched.email ? form.getFieldError('email') : null}
            required
          />

          <div className="flex flex-col gap-2">
            <label className="text-[14px] font-medium text-[rgb(var(--text-main))]">
              Role
              <span className="text-[#EF4444] ml-1">*</span>
            </label>
            <select
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
              className="px-3 py-2 rounded-lg border border-[rgb(var(--border))] bg-[rgb(var(--card))] text-[rgb(var(--text-main))] focus:outline-none focus:ring-2 focus:ring-[rgb(var(--accent))]/30"
            >
              <option value="">Select a role...</option>
              {roles.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.name}
                </option>
              ))}
            </select>
            {!selectedRole && form.isSubmitting && (
              <p className="text-[12px] text-[#EF4444]">Please select a role</p>
            )}
          </div>

          <div className="flex gap-3 justify-end pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={form.isSubmitting || isLoading}
              className="bg-[rgb(var(--accent))] hover:bg-[rgb(var(--accent))]/90 text-[rgb(var(--accent-foreground))]"
            >
              {form.isSubmitting || isLoading ? 'Sending...' : 'Send Invite'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
