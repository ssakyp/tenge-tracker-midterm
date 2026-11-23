import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';
import Spinner from '../components/Spinner';

/**
 * Route guard for pages that need a logged-in user.
 * While the stored token is being checked we show a spinner; without a user we redirect
 * to /login and remember where the user wanted to go.
 * This only hides UI: the real protection is the backend returning 401/403.
 */
export default function ProtectedRoute() {
  const { isAuthenticated, initializing } = useAuth();
  const location = useLocation();

  if (initializing) return <Spinner label="Checking your session…" fullPage />;
  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname + location.search }} />;
  }
  return <Outlet />;
}

/**
 * The opposite guard: logged-in users do not need the login and signup pages.
 * Right after a successful login this also sends the user back to the page they
 * originally asked for (saved in location.state.from by ProtectedRoute).
 */
export function GuestRoute() {
  const { isAuthenticated, initializing } = useAuth();
  const location = useLocation();
  if (initializing) return <Spinner label="Checking your session…" fullPage />;
  if (isAuthenticated) return <Navigate to={location.state?.from || '/dashboard'} replace />;
  return <Outlet />;
}
