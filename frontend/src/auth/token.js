// JWT storage and decoding helpers.
//
// The access token is kept in localStorage so a page reload keeps the user logged in.
// Trade-off (explained in README): any script on the page could read it, so we rely on
// React escaping all output (no dangerouslySetInnerHTML) and on a short token lifetime.

const TOKEN_KEY = 'tengeTracker.accessToken';

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function saveToken(token) {
  try {
    localStorage.setItem(TOKEN_KEY, token);
  } catch {
    /* storage unavailable: token lives only in memory for this tab */
  }
}

export function clearToken() {
  try {
    localStorage.removeItem(TOKEN_KEY);
  } catch {
    /* ignore */
  }
}

function base64UrlDecode(part) {
  const base64 = part.replace(/-/g, '+').replace(/_/g, '/');
  const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4);
  const binary = atob(padded);
  const bytes = Uint8Array.from(binary, (c) => c.charCodeAt(0));
  return new TextDecoder().decode(bytes);
}

/**
 * Reads the header and payload of a JWT. This does NOT verify the signature:
 * only the server can do that. We use it just to show claims and to know the expiry time.
 */
export function decodeToken(token) {
  try {
    const [header, payload] = token.split('.');
    return {
      header: JSON.parse(base64UrlDecode(header)),
      payload: JSON.parse(base64UrlDecode(payload)),
    };
  } catch {
    return null;
  }
}

/** Milliseconds until the token expires (negative if already expired, null if unknown). */
export function msUntilExpiry(token) {
  const decoded = token ? decodeToken(token) : null;
  if (!decoded || typeof decoded.payload.exp !== 'number') return null;
  return decoded.payload.exp * 1000 - Date.now();
}

export function isTokenExpired(token) {
  const ms = msUntilExpiry(token);
  return ms === null || ms <= 0;
}
