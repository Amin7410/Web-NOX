'use client';

import { FormField, SettingsSection } from '../Settings';
import { Button } from '@nox/ui';
import { useForm } from '@/lib/hooks';
import { validators } from '@/lib/utils/validators';
import type { Organization } from '@/lib/types';

interface OrgFormProps {
  organization?: Organization;
  isLoading?: boolean;
  onSubmit: (data: any) => Promise<void>;
}

export function OrgForm({ organization, isLoading = false, onSubmit }: OrgFormProps) {
  const form = useForm({
    initialValues: {
      name: organization?.name || '',
      description: organization?.description || '',
    },
    onSubmit,
    validate: (values: any) => {
      const errors: any = {};
      
      // Validate name
      const nameError = validators.required(values.name, 'Organization name') ||
        validators.minLength(values.name, 3, 'Organization name') ||
        validators.maxLength(values.name, 100, 'Organization name');
      if (nameError) errors.name = nameError;
      
      // Validate description
      if (values.description) {
        const descError = validators.maxLength(values.description, 500, 'Description');
        if (descError) errors.description = descError;
      }
      
      return errors;
    },
  });

  return (
    <form onSubmit={form.handleSubmit} className="max-w-2xl">
      <SettingsSection
        title="Organization Information"
        description="Update your organization details"
      >
        <div className="flex flex-col gap-6">
          <FormField
            name="name"
            label="Organization Name"
            placeholder="e.g., Acme Corporation"
            value={form.values.name}
            onChange={form.handleChange}
            onBlur={form.handleBlur}
            error={form.touched.name ? form.getFieldError('name') : null}
            required
          />

          <FormField
            name="description"
            label="Description"
            type="textarea"
            placeholder="What does your organization do?"
            value={form.values.description}
            onChange={form.handleChange}
            onBlur={form.handleBlur}
            error={form.touched.description ? form.getFieldError('description') : null}
            helperText="Optional. Up to 500 characters."
          />

          <div className="flex gap-3">
            <Button
              type="submit"
              disabled={!form.isDirty || form.isSubmitting || isLoading}
              className="bg-[rgb(var(--accent))] hover:bg-[rgb(var(--accent))]/90 text-[rgb(var(--accent-foreground))]"
            >
              {form.isSubmitting || isLoading ? 'Saving...' : 'Save Changes'}
            </Button>
            {form.isDirty && (
              <Button
                type="button"
                variant="outline"
                onClick={form.resetForm}
                disabled={form.isSubmitting}
              >
                Discard
              </Button>
            )}
          </div>
        </div>
      </SettingsSection>
    </form>
  );
}
