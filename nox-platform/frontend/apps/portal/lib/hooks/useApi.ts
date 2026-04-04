'use client';

import { useState, useCallback } from 'react';
import { ApiException } from '../types/api';

export interface UseApiState<T> {
  data: T | null;
  loading: boolean;
  error: ApiException | null;
}

interface UseApiOptions {
  onSuccess?: (data: any) => void;
  onError?: (error: ApiException) => void;
}

export function useApi<T>(
  apiFn: (params?: any) => Promise<T>,
  options?: UseApiOptions
) {
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    loading: false,
    error: null,
  });

  const execute = useCallback(
    async (params?: any) => {
      setState({ data: null, loading: true, error: null });
      try {
        const result = await apiFn(params);
        setState({ data: result, loading: false, error: null });
        options?.onSuccess?.(result);
        return result;
      } catch (error) {
        const apiError = error instanceof ApiException ? error : new ApiException(500, 'UNKNOWN_ERROR', String(error));
        setState({ data: null, loading: false, error: apiError });
        options?.onError?.(apiError);
        throw apiError;
      }
    },
    [apiFn, options]
  );

  return {
    ...state,
    execute,
    reset: () => setState({ data: null, loading: false, error: null }),
  };
}

export function usePaginatedApi<T>(
  apiFn: (page: number, pageSize?: number) => Promise<any>,
  initialPage: number = 0,
  initialPageSize: number = 10
) {
  const [page, setPage] = useState(initialPage);
  const [pageSize, setPageSize] = useState(initialPageSize);
  const [data, setData] = useState<T[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiException | null>(null);

  const fetch = useCallback(async () => {
    setLoading(true);
    try {
      const result = await apiFn(page, pageSize);
      setData(result.content || []);
      setTotalPages(result.totalPages || 1);
      setError(null);
    } catch (err) {
      const apiError = err instanceof ApiException ? err : new ApiException(500, 'UNKNOWN_ERROR', String(err));
      setError(apiError);
    } finally {
      setLoading(false);
    }
  }, [apiFn, page, pageSize]);

  return {
    data,
    loading,
    error,
    page,
    pageSize,
    totalPages,
    setPage,
    setPageSize,
    fetch,
  };
}
