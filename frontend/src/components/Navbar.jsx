import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <NavLink to={isAuthenticated ? '/dashboard' : '/'} className="brand">
        <span className="brand__mark">₸</span> Tenge Tracker
      </NavLink>
      <nav className="navbar__links">
        {isAuthenticated ? (
          <>
            <NavLink to="/dashboard">Dashboard</NavLink>
            <NavLink to="/transactions">Transactions</NavLink>
            <NavLink to="/accounts">Accounts</NavLink>
            <NavLink to="/budgets">Budgets</NavLink>
            <NavLink to="/catalog">Categories &amp; tags</NavLink>
          </>
        ) : (
          <>
            <NavLink to="/" end>Home</NavLink>
            <NavLink to="/catalog">Categories &amp; tags</NavLink>
          </>
        )}
      </nav>
      <div className="navbar__user">
        {isAuthenticated ? (
          <>
            <NavLink to="/profile" className="user-pill" title="Profile">
              <span className="avatar">{user.username.slice(0, 1).toUpperCase()}</span>
              {user.username}
            </NavLink>
            <button type="button" className="btn btn--ghost btn--sm" onClick={handleLogout}>Log out</button>
          </>
        ) : (
          <>
            <NavLink to="/login" className="btn btn--ghost btn--sm">Log in</NavLink>
            <NavLink to="/signup" className="btn btn--primary btn--sm">Sign up</NavLink>
          </>
        )}
      </div>
    </header>
  );
}
