import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { decodeToken } from '../auth/token';
import ErrorMessage, { SuccessMessage } from '../components/ErrorMessage';
import Spinner from '../components/Spinner';
import { formatDate } from '../components/format';

/** Protected route: current user info, change username, change password, edit profile. */
export default function ProfilePage() {
  const { user, token, refreshUser, logout } = useAuth();
  const navigate = useNavigate();
  const [refreshing, setRefreshing] = useState(true);
  const [refreshError, setRefreshError] = useState(null);

  // always show fresh data from GET /api/users/me when the page opens
  useEffect(() => {
    refreshUser()
      .catch((err) => setRefreshError(err))
      .finally(() => setRefreshing(false));
  }, [refreshUser]);

  const decoded = token ? decodeToken(token) : null;

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Profile</h1>
          <p className="muted">Your account details and security settings.</p>
        </div>
        <button type="button" className="btn btn--ghost" onClick={() => { logout(); navigate('/login'); }}>Log out</button>
      </div>
      <ErrorMessage error={refreshError} />

      <div className="grid-2">
        <section className="card">
          <div className="profile-head">
            <span className="avatar avatar--lg">{user.username.slice(0, 1).toUpperCase()}</span>
            <div>
              <h2>{user.profile?.fullName || user.username}</h2>
              <p className="muted">@{user.username}</p>
            </div>
            {refreshing && <Spinner small label="Refreshing" />}
          </div>
          <dl className="details">
            <dt>User ID</dt><dd>{user.id}</dd>
            <dt>Email</dt><dd>{user.profile?.email || <span className="muted">not set</span>}</dd>
            <dt>Preferred currency</dt><dd>{user.profile?.preferredCurrency || 'KZT'}</dd>
            <dt>Bio</dt><dd>{user.profile?.bio || <span className="muted">not set</span>}</dd>
            <dt>Member since</dt><dd>{formatDate(user.createdAt)}</dd>
            <dt>Accounts</dt><dd>{user.accountsCount}</dd>
            <dt>Transactions</dt><dd>{user.transactionsCount}</dd>
          </dl>
        </section>

        <UsernameForm />
      </div>

      <div className="grid-2">
        <PasswordForm />
        <ProfileForm />
      </div>

      {decoded && (
        <section className="card">
          <h2>Your session token (JWT)</h2>
          <p className="muted">
            Decoded in the browser to show its claims. The signature is checked only by the server.
            Expires <strong>{new Date(decoded.payload.exp * 1000).toLocaleString()}</strong>.
          </p>
          <div className="grid-2">
            <pre className="code">{JSON.stringify(decoded.header, null, 2)}</pre>
            <pre className="code">{JSON.stringify(decoded.payload, null, 2)}</pre>
          </div>
        </section>
      )}
    </>
  );
}

function UsernameForm() {
  const { user, updateUsername } = useAuth();
  const [username, setUsername] = useState(user.username);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  const onSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setNotice(null);
    try {
      const updated = await updateUsername(username.trim());
      setNotice(`Username changed to @${updated.username}.`);
    } catch (err) {
      setError(err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="card">
      <h2>Change username</h2>
      <SuccessMessage>{notice}</SuccessMessage>
      <ErrorMessage error={error} />
      <form className="form" onSubmit={onSubmit}>
        <label>
          New username
          <input value={username} onChange={(e) => setUsername(e.target.value)} minLength={3} maxLength={30}
            pattern="[A-Za-z0-9_.]+" title="Letters, digits, _ and ." required />
        </label>
        <button className="btn btn--primary" disabled={saving || username.trim() === user.username}>
          {saving ? <><Spinner small /> Saving…</> : 'Update username'}
        </button>
      </form>
    </section>
  );
}

function PasswordForm() {
  const { changePassword } = useAuth();
  const empty = { currentPassword: '', newPassword: '', confirm: '' };
  const [form, setForm] = useState(empty);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setNotice(null);
    if (form.newPassword !== form.confirm) {
      setError('New passwords do not match');
      return;
    }
    setSaving(true);
    try {
      await changePassword(form.currentPassword, form.newPassword);
      setForm(empty);
      setNotice('Password changed. Sessions on other devices were signed out.');
    } catch (err) {
      setError(err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="card">
      <h2>Change password</h2>
      <SuccessMessage>{notice}</SuccessMessage>
      <ErrorMessage error={error} />
      <form className="form" onSubmit={onSubmit}>
        <label>
          Current password
          <input type="password" value={form.currentPassword} onChange={set('currentPassword')} autoComplete="current-password" required />
        </label>
        <label>
          New password <span className="muted">(8+ characters)</span>
          <input type="password" value={form.newPassword} onChange={set('newPassword')} minLength={8} maxLength={72} autoComplete="new-password" required />
        </label>
        <label>
          Confirm new password
          <input type="password" value={form.confirm} onChange={set('confirm')} autoComplete="new-password" required />
        </label>
        <button className="btn btn--primary" disabled={saving}>
          {saving ? <><Spinner small /> Saving…</> : 'Change password'}
        </button>
      </form>
    </section>
  );
}

function ProfileForm() {
  const { user, updateProfile } = useAuth();
  const [form, setForm] = useState({
    fullName: user.profile?.fullName ?? '',
    email: user.profile?.email ?? '',
    preferredCurrency: user.profile?.preferredCurrency ?? 'KZT',
    bio: user.profile?.bio ?? '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    setNotice(null);
    try {
      await updateProfile({ ...form, preferredCurrency: form.preferredCurrency.toUpperCase() });
      setNotice('Profile saved.');
    } catch (err) {
      setError(err);
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="card">
      <h2>Personal details</h2>
      <SuccessMessage>{notice}</SuccessMessage>
      <ErrorMessage error={error} />
      <form className="form" onSubmit={onSubmit}>
        <div className="form__row">
          <label>Full name<input value={form.fullName} onChange={set('fullName')} maxLength={100} /></label>
          <label>Email<input type="email" value={form.email} onChange={set('email')} maxLength={120} /></label>
        </div>
        <label>
          Preferred currency
          <input className="input--short" value={form.preferredCurrency} onChange={set('preferredCurrency')} maxLength={3} pattern="[A-Za-z]{3}" required />
        </label>
        <label>Bio<textarea value={form.bio} onChange={set('bio')} maxLength={255} rows={3} /></label>
        <button className="btn btn--primary" disabled={saving}>
          {saving ? <><Spinner small /> Saving…</> : 'Save details'}
        </button>
      </form>
    </section>
  );
}
