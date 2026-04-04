export interface ValidationError {
  field: string;
  message: string;
}

export interface ValidationResult {
  isValid: boolean;
  errors: ValidationError[];
}

export const validators = {
  email: (email: string): ValidationError | null => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return { field: 'email', message: 'Invalid email address' };
    }
    return null;
  },

  password: (password: string): ValidationError | null => {
    if (password.length < 8) {
      return { field: 'password', message: 'Password must be at least 8 characters' };
    }
    if (!/[A-Z]/.test(password)) {
      return { field: 'password', message: 'Password must contain at least one uppercase letter' };
    }
    if (!/[a-z]/.test(password)) {
      return { field: 'password', message: 'Password must contain at least one lowercase letter' };
    }
    if (!/[0-9]/.test(password)) {
      return { field: 'password', message: 'Password must contain at least one number' };
    }
    return null;
  },

  required: (value: string, fieldName: string = 'Field'): ValidationError | null => {
    if (!value || value.trim().length === 0) {
      return { field: 'required', message: `${fieldName} is required` };
    }
    return null;
  },

  minLength: (value: string, min: number, fieldName: string = 'Field'): ValidationError | null => {
    if (value.length < min) {
      return { field: 'minLength', message: `${fieldName} must be at least ${min} characters` };
    }
    return null;
  },

  maxLength: (value: string, max: number, fieldName: string = 'Field'): ValidationError | null => {
    if (value.length > max) {
      return { field: 'maxLength', message: `${fieldName} must not exceed ${max} characters` };
    }
    return null;
  },

  passwordMatch: (password: string, confirmPassword: string): ValidationError | null => {
    if (password !== confirmPassword) {
      return { field: 'passwordMatch', message: 'Passwords do not match' };
    }
    return null;
  },

  url: (url: string): ValidationError | null => {
    try {
      new URL(url);
      return null;
    } catch {
      return { field: 'url', message: 'Invalid URL' };
    }
  },
};

export function validateForm(
  data: Record<string, any>,
  rules: Record<string, Array<(value: any) => ValidationError | null>>
): ValidationResult {
  const errors: ValidationError[] = [];

  Object.entries(rules).forEach(([field, fieldRules]) => {
    const value = data[field];
    for (const rule of fieldRules) {
      const error = rule(value);
      if (error) {
        errors.push({ ...error, field });
        break; // Stop at first error for this field
      }
    }
  });

  return {
    isValid: errors.length === 0,
    errors,
  };
}

export function getFieldError(errors: ValidationError[], fieldName: string): string | null {
  const error = errors.find((e) => e.field === fieldName);
  return error?.message || null;
}

export function passwordStrength(password: string): 'weak' | 'medium' | 'strong' {
  let strength = 0;

  if (password.length >= 8) strength++;
  if (password.length >= 12) strength++;
  if (/[a-z]/.test(password)) strength++;
  if (/[A-Z]/.test(password)) strength++;
  if (/[0-9]/.test(password)) strength++;
  if (/[^a-zA-Z0-9]/.test(password)) strength++;

  if (strength <= 2) return 'weak';
  if (strength <= 4) return 'medium';
  return 'strong';
}
