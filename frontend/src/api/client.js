import { getToken } from '../auth/token';

// All requests go to the same origin under /api (nginx or the Vite dev proxy forwards them).
const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api';

/** Error thrown for every failed request, with the server's message and field errors. */
export class ApiError extends Error {
  constructor(status, message, fieldErrors = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

let unauthorizedHandler = null;

/** AuthContext registers a callback that logs the user out when a token is rejected. */
export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

async function parseBody(response) {
  if (response.status === 204) return null;
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function messageFor(status, body) {
  if (body && typeof body === 'object') {
    if (body.error) return body.error;
    if (body.errors) return 'Please fix the highlighted fields';
    if (body.message) return body.message;
  }
  switch (status) {
    case 400: return 'The request was not valid';
    case 401: return 'Please log in to continue';
    case 403: return 'You do not have access to this resource';
    case 404: return 'Not found';
    case 409: return 'Conflict with existing data';
    default: return `Request failed (HTTP ${status})`;
  }
}

/**
 * fetch() wrapper:
 *  - prefixes /api and sends/parses JSON
 *  - adds "Authorization: Bearer <token>" when a token exists
 *  - turns HTTP errors into ApiError
 *  - on 401 for a request that carried a token, calls the logout handler (expired/revoked token)
 */
export async function apiFetch(path, { method = 'GET', body, auth = true, signal } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  const token = auth ? getToken() : null;
  if (token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal,
    });
  } catch (err) {
    if (err.name === 'AbortError') throw err;
    throw new ApiError(0, 'Cannot reach the server. Is the backend running?');
  }

  const data = await parseBody(response);

  if (!response.ok) {
    const error = new ApiError(response.status, messageFor(response.status, data), data?.errors ?? null);
    if (response.status === 401 && token && unauthorizedHandler) {
      unauthorizedHandler(error.message);
    }
    throw error;
  }
  return data;
}

export const api = {
  get: (path, opts) => apiFetch(path, { ...opts, method: 'GET' }),
  post: (path, body, opts) => apiFetch(path, { ...opts, method: 'POST', body }),
  put: (path, body, opts) => apiFetch(path, { ...opts, method: 'PUT', body }),
  patch: (path, body, opts) => apiFetch(path, { ...opts, method: 'PATCH', body }),
  del: (path, opts) => apiFetch(path, { ...opts, method: 'DELETE' }),
};
