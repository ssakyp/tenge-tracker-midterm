import { useCallback, useEffect, useRef, useState } from 'react';

/**
 * Loads data on mount and exposes { data, loading, error, reload, setData }.
 * `loader` receives an AbortSignal so leaving the page cancels the request.
 */
export function useApi(loader, deps = []) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const loaderRef = useRef(loader);
  loaderRef.current = loader;
  const controllerRef = useRef(null);

  const reload = useCallback(async () => {
    controllerRef.current?.abort();
    const controller = new AbortController();
    controllerRef.current = controller;
    setLoading(true);
    setError(null);
    try {
      const result = await loaderRef.current(controller.signal);
      if (!controller.signal.aborted) setData(result);
    } catch (err) {
      if (err.name !== 'AbortError' && !controller.signal.aborted) setError(err);
    } finally {
      if (!controller.signal.aborted) setLoading(false);
    }
  }, []);

  useEffect(() => {
    reload();
    return () => controllerRef.current?.abort();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  return { data, loading, error, reload, setData };
}
