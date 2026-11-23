import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import ErrorMessage from '../components/ErrorMessage';
import Spinner from '../components/Spinner';

export default function LoginPage() {
  const { login, sessionMessage, clearSessionMessage } = useAuth();
  const location = useLocation();

  const [form, setForm] = useState({ username: '', password: '' });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    clearSessionMessage();
    try {
      // on success GuestRoute redirects to location.state.from (or /dashboard)
      await login(form.username.trim(), form.password);
    } catch (err) {
      setError(err);
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-card card">
      <h1>Log in</h1>
      <p className="muted">Demo account: <code>demo</code> / <code>password123</code></p>
      {sessionMessage && <div className="alert alert--warning">{sessionMessage}</div>}
      {location.state?.from && !sessionMessage && (
        <div className="alert alert--warning">Please log in to open <code>{location.state.from}</code>.</div>
      )}
      <ErrorMessage error={error} />
      <form onSubmit={onSubmit} className="form">
        <label>
          Username
          <input name="username" value={form.username} onChange={onChange} autoComplete="username" required autoFocus />
        </label>
        <label>
          Password
          <input name="password" type="password" value={form.password} onChange={onChange} autoComplete="current-password" required />
        </label>
        <button className="btn btn--primary btn--block" disabled={submitting}>
          {submitting ? <><Spinner small /> Logging in…</> : 'Log in'}
        </button>
      </form>
      <p className="muted center">No account? <Link to="/signup">Sign up</Link></p>
    </div>
  );
}
