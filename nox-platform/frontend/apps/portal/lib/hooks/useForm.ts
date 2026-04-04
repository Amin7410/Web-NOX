'use client';

import { useState, useCallback, useRef } from 'react';
import { ValidationError, validateForm } from '../utils/validators';

interface UseFormConfig<T> {
  initialValues: T;
  onSubmit: (values: T) => Promise<void> | void;
  validate?: (values: T) => Record<string, Array<(value: any) => ValidationError | null>>;
}

interface UseFormState<T> {
  values: T;
  errors: ValidationError[];
  touched: Partial<Record<keyof T, boolean>>;
  isSubmitting: boolean;
  isDirty: boolean;
}

export function useForm<T extends Record<string, any>>({
  initialValues,
  onSubmit,
  validate,
}: UseFormConfig<T>) {
  const [state, setState] = useState<UseFormState<T>>({
    values: initialValues,
    errors: [],
    touched: {},
    isSubmitting: false,
    isDirty: false,
  });

  const initialValuesRef = useRef(initialValues);

  const handleChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
      const { name, value, type } = e.target;
      const fieldName = name as keyof T;

      setState((prev) => ({
        ...prev,
        values: {
          ...prev.values,
          [fieldName]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value,
        },
        isDirty: true,
      }));
    },
    []
  );

  const setFieldValue = useCallback((field: keyof T, value: any) => {
    setState((prev) => ({
      ...prev,
      values: {
        ...prev.values,
        [field]: value,
      },
      isDirty: true,
    }));
  }, []);

  const setFieldError = useCallback((field: keyof T, message: string) => {
    setState((prev) => ({
      ...prev,
      errors: [
        ...prev.errors.filter((e) => e.field !== String(field)),
        { field: String(field), message },
      ],
    }));
  }, []);

  const handleBlur = useCallback((e: React.FocusEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name } = e.target;
    const fieldName = name as keyof T;

    setState((prev) => ({
      ...prev,
      touched: {
        ...prev.touched,
        [fieldName]: true,
      },
    }));
  }, []);

  const handleSubmit = useCallback(
    async (e?: React.FormEvent<HTMLFormElement>) => {
      if (e) e.preventDefault();

      let validationErrors: ValidationError[] = [];

      if (validate) {
        const rules = validate(state.values);
        const result = validateForm(state.values, rules);
        validationErrors = result.errors;
      }

      setState((prev) => ({
        ...prev,
        errors: validationErrors,
        touched: Object.keys(prev.values).reduce((acc, key) => {
          acc[key as keyof T] = true;
          return acc;
        }, {} as Partial<Record<keyof T, boolean>>),
      }));

      if (validationErrors.length > 0) {
        return;
      }

      setState((prev) => ({
        ...prev,
        isSubmitting: true,
      }));

      try {
        await onSubmit(state.values);
      } finally {
        setState((prev) => ({
          ...prev,
          isSubmitting: false,
        }));
      }
    },
    [state.values, onSubmit, validate]
  );

  const resetForm = useCallback(() => {
    setState({
      values: initialValuesRef.current,
      errors: [],
      touched: {},
      isSubmitting: false,
      isDirty: false,
    });
  }, []);

  const getFieldError = useCallback(
    (field: keyof T): string | null => {
      const error = state.errors.find((e) => e.field === String(field));
      return error?.message || null;
    },
    [state.errors]
  );

  return {
    values: state.values,
    errors: state.errors,
    touched: state.touched,
    isSubmitting: state.isSubmitting,
    isDirty: state.isDirty,
    handleChange,
    handleBlur,
    handleSubmit,
    setFieldValue,
    setFieldError,
    resetForm,
    getFieldError,
  };
}
