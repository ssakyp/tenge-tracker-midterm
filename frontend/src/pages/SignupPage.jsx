import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import ErrorMessage from '../components/ErrorMessage';
import Spinner from '../components/Spinner';

export default function SignupPage() {
  const { signup } = useAuth();
  const [form, setForm] = useState({ username: '', password: '', confirm: '', fullName: '', email: '' });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    if (form.password !== form.confirm) {
      setError('Passwords do not match');
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await signup({
        username: form.username.trim(),
        password: form.password,
        fullName: form.fullName.trim() || null,
        email: form.email.trim() || null,
      });
      // on success GuestRoute redirects to /dashboard
    } catch (err) {
      setError(err);
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-card card">
      <h1>Create an account</h1>
      <ErrorMessage error={error} />
      <form onSubmit={onSubmit} className="form">
        <label>
          Username
          <input name="username" value={form.username} onChange={onChange} minLength={3} maxLength={30}
            pattern="[A-Za-z0-9_.]+" title="Letters, digits, _ and ." autoComplete="username" required autoFocus />
        </label>
        <div className="form__row">
          <label>
            Full name <span className="muted">(optional)</span>
            <input name="fullName" value={form.fullName} onChange={onChange} maxLength={100} />
          </label>
          <label>
            Email <span className="muted">(optional)</span>
            <input name="email" type="email" value={form.email} onChange={onChange} maxLength={120} />
          </label>
        </div>
        <label>
          Password <span className="muted">(8+ characters)</span>
          <input name="password" type="password" value={form.password} onChange={onChange} minLength={8} maxLength={72}
            autoComplete="new-password" required />
        </label>
        <label>
          Confirm password
          <input name="confirm" type="password" value={form.confirm} onChange={onChange} autoComplete="new-password" required />
        </label>
        <button className="btn btn--primary btn--block" disabled={submitting}>
          {submitting ? <><Spinner small /> Creating account…</> : 'Sign up'}
        </button>
      </form>
      <p className="muted center">Already registered? <Link to="/login">Log in</Link></p>
    </div>
  );
}
