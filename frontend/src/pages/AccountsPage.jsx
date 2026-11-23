import { useState } from 'react';
import { api } from '../api/client';
import { useApi } from '../api/useApi';
import ErrorMessage, { SuccessMessage } from '../components/ErrorMessage';
import Spinner from '../components/Spinner';
import { formatMoney } from '../components/format';

export default function AccountsPage() {
  const { data: accounts, loading, error, reload } = useApi((signal) => api.get('/accounts', { signal }));
  const [form, setForm] = useState({ name: '', balance: '', currency: 'KZT' });
  const [editing, setEditing] = useState(null); // { id, name, currency }
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState(null);
  const [notice, setNotice] = useState(null);

  const run = async (fn, message) => {
    setBusy(true);
    setActionError(null);
    setNotice(null);
    try {
      await fn();
      setNotice(message);
      await reload();
    } catch (err) {
      setActionError(err);
    } finally {
      setBusy(false);
    }
  };

  const create = (e) => {
    e.preventDefault();
    run(async () => {
      await api.post('/accounts', {
        name: form.name.trim(),
        balance: form.balance === '' ? 0 : Number(form.balance),
        currency: form.currency.toUpperCase(),
      });
      setForm({ name: '', balance: '', currency: form.currency });
    }, 'Account created.');
  };

  // PATCH: send only the fields that changed
  const saveEdit = (e) => {
    e.preventDefault();
    run(async () => {
      await api.patch(`/accounts/${editing.id}`, { name: editing.name.trim(), currency: editing.currency.toUpperCase() });
      setEditing(null);
    }, 'Account updated.');
  };

  const remove = (account) => {
    if (!window.confirm(`Delete account "${account.name}"?`)) return;
    run(() => api.del(`/accounts/${account.id}`), 'Account deleted.');
  };

  if (loading && !accounts) return <Spinner label="Loading accounts…" />;

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Accounts</h1>
          <p className="muted">Cards, deposits and cash. Balances change only through transactions.</p>
        </div>
      </div>
      <ErrorMessage error={error} onRetry={reload} />
      <SuccessMessage>{notice}</SuccessMessage>
      <ErrorMessage error={actionError} />

      <form className="card inline-form" onSubmit={create}>
        <input placeholder="Account name, e.g. Kaspi Gold" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
        <input type="number" min="0" step="0.01" placeholder="Opening balance" value={form.balance}
          onChange={(e) => setForm({ ...form, balance: e.target.value })} />
        <input className="input--short" placeholder="KZT" value={form.currency} maxLength={3} pattern="[A-Za-z]{3}"
          onChange={(e) => setForm({ ...form, currency: e.target.value })} required />
        <button className="btn btn--primary" disabled={busy}>Add account</button>
      </form>

      {accounts && accounts.length === 0 && <div className="empty card"><p>No accounts yet. Add your first one above.</p></div>}

      <div className="account-grid">
        {accounts?.map((a) => (
          <div key={a.id} className="card account">
            {editing?.id === a.id ? (
              <form className="form" onSubmit={saveEdit}>
                <label>Name<input value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} required autoFocus /></label>
                <label>Currency<input value={editing.currency} maxLength={3} pattern="[A-Za-z]{3}"
                  onChange={(e) => setEditing({ ...editing, currency: e.target.value })} required /></label>
                <div className="form__actions">
                  <button type="button" className="btn btn--ghost btn--sm" onClick={() => setEditing(null)}>Cancel</button>
                  <button className="btn btn--primary btn--sm" disabled={busy}>Save</button>
                </div>
              </form>
            ) : (
              <>
                <span className="account__name">{a.name}</span>
                <span className="account__balance">{formatMoney(a.balance, a.currency)}</span>
                <span className="muted">#{a.id} · {a.currency}</span>
                <div className="row-actions">
                  <button type="button" className="btn btn--ghost btn--sm" onClick={() => setEditing({ id: a.id, name: a.name, currency: a.currency })}>Edit</button>
                  <button type="button" className="btn btn--danger btn--sm" onClick={() => remove(a)} disabled={busy}>Delete</button>
                </div>
              </>
            )}
          </div>
        ))}
      </div>
    </>
  );
}
