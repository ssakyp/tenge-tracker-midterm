import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { api, setUnauthorizedHandler } from '../api/client';
import { clearToken, getToken, isTokenExpired, msUntilExpiry, saveToken } from './token';

const AuthContext = createContext(null);

/**
 * Holds the logged-in user and the JWT for the whole app (React Context).
 * Any component can call useAuth() to read the user or to log in/out.
 */
export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => getToken());
  const [user, setUser] = useState(null);
  const [initializing, setInitializing] = useState(true);
  const [sessionMessage, setSessionMessage] = useState(null);
  const expiryTimer = useRef(null);

  const logout = useCallback((message = null) => {
    clearToken();
    setToken(null);
    setUser(null);
    setSessionMessage(message);
  }, []);

  /** Stores the token + user returned by login, signup, username or password change. */
  const startSession = useCallback((authResponse) => {
    saveToken(authResponse.accessToken);
    setToken(authResponse.accessToken);
    setUser(authResponse.user);
    setSessionMessage(null);
  }, []);

  // Any 401 on a request that carried a token means it expired or was revoked.
  useEffect(() => {
    setUnauthorizedHandler((message) => logout(message || 'Your session has ended. Please log in again.'));
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  // Log out automatically at the moment the token expires.
  useEffect(() => {
    clearTimeout(expiryTimer.current);
    const ms = token ? msUntilExpiry(token) : null;
    if (ms !== null && ms > 0) {
      // setTimeout max is ~24.8 days; our tokens live minutes
      expiryTimer.current = setTimeout(
        () => logout('Your session expired. Please log in again.'),
        Math.min(ms, 2_147_000_000),
      );
    }
    return () => clearTimeout(expiryTimer.current);
  }, [token, logout]);

  // On first load: if a stored token is still valid, fetch the current user.
  useEffect(() => {
    let cancelled = false;
    async function restore() {
      const stored = getToken();
      if (!stored) {
        setInitializing(false);
        return;
      }
      if (isTokenExpired(stored)) {
        logout('Your session expired. Please log in again.');
        setInitializing(false);
        return;
      }
      try {
        const me = await api.get('/users/me');
        if (!cancelled) setUser(me);
      } catch (err) {
        if (!cancelled && err.status !== 401) {
          // server unreachable: keep the token, the user can retry
          setSessionMessage(err.message);
        }
      } finally {
        if (!cancelled) setInitializing(false);
      }
    }
    restore();
    return () => {
      cancelled = true;
    };
  }, [logout]);

  const login = useCallback(async (username, password) => {
    const res = await api.post('/auth/login', { username, password }, { auth: false });
    startSession(res);
    return res.user;
  }, [startSession]);

  const signup = useCallback(async (form) => {
    const res = await api.post('/auth/signup', form, { auth: false });
    startSession(res);
    return res.user;
  }, [startSession]);

  const refreshUser = useCallback(async () => {
    const me = await api.get('/users/me');
    setUser(me);
    return me;
  }, []);

  const updateUsername = useCallback(async (username) => {
    const res = await api.patch('/users/me', { username });
    startSession(res);
    return res.user;
  }, [startSession]);

  const changePassword = useCallback(async (currentPassword, newPassword) => {
    const res = await api.put('/users/me/password', { currentPassword, newPassword });
    startSession(res); // old tokens are now revoked server-side; keep the new one
    return res.user;
  }, [startSession]);

  const updateProfile = useCallback(async (profile) => {
    const me = await api.put('/users/me/profile', profile);
    setUser(me);
    return me;
  }, []);

  const value = useMemo(() => ({
    token,
    user,
    isAuthenticated: Boolean(user),
    initializing,
    sessionMessage,
    clearSessionMessage: () => setSessionMessage(null),
    login,
    signup,
    logout,
    refreshUser,
    updateUsername,
    changePassword,
    updateProfile,
  }), [token, user, initializing, sessionMessage, login, signup, logout, refreshUser, updateUsername, changePassword, updateProfile]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
