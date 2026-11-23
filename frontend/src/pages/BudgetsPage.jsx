import { useState } from 'react';
import { api } from '../api/client';
import { useApi } from '../api/useApi';
import { useAuth } from '../auth/AuthContext';
import BudgetBar from '../components/BudgetBar';
import ErrorMessage, { SuccessMessage } from '../components/ErrorMessage';
import Spinner from '../components/Spinner';
import { currentMonthRange, MONTHS } from '../components/format';

export default function BudgetsPage() {
  const { user } = useAuth();
  const currency = user.profile?.preferredCurrency || 'KZT';
  const now = currentMonthRange();

  const { data, loading, error, reload } = useApi(async (signal) => {
    const [budgets, categories] = await Promise.all([
      api.get('/budgets', { signal }),
      api.get('/categories', { signal }),
    ]);
    const statuses = await Promise.all(budgets.map((b) => api.get(`/budgets/${b.id}/status`, { signal })));
    return { budgets, categories, statusById: Object.fromEntries(statuses.map((s) => [s.budgetId, s])) };
  });

  const [form, setForm] = useState({ categoryId: '', limitAmount: '', month: now.month, year: now.year });
  const [editing, setEditing] = useState(null); // { id, limitAmount }
  const [busy, setBusy] = useState(false);
  const [actionError, setActionError] = useState(null);
  const [notice, setNotice] = useState(null);

  if (loading && !data) return <Spinner label="Loading budgets…" />;
  if (error && !data) return <ErrorMessage error={error} onRetry={reload} />;

  const { budgets, categories, statusById } = data;
  const expenseCategories = categories.filter((c) => c.type === 'EXPENSE');
  const categoryName = (id) => categories.find((c) => c.id === id)?.name ?? `#${id}`;

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
      await api.post('/budgets', {
        categoryId: Number(form.categoryId),
        limitAmount: Number(form.limitAmount),
        month: Number(form.month),
        year: Number(form.year),
      });
      setForm({ ...form, categoryId: '', limitAmount: '' });
    }, 'Budget created.');
  };

  const saveLimit = (e) => {
    e.preventDefault();
    run(async () => {
      await api.patch(`/budgets/${editing.id}`, { limitAmount: Number(editing.limitAmount) });
      setEditing(null);
    }, 'Limit updated.');
  };

  const remove = (b) => {
    if (!window.confirm(`Delete the ${categoryName(b.categoryId)} budget for ${MONTHS[b.month - 1]} ${b.year}?`)) return;
    run(() => api.del(`/budgets/${b.id}`), 'Budget deleted.');
  };

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Budgets</h1>
          <p className="muted">Monthly spending limits per expense category.</p>
        </div>
      </div>
      <SuccessMessage>{notice}</SuccessMessage>
      <ErrorMessage error={actionError} />
      <ErrorMessage error={error} onRetry={reload} />

      <form className="card inline-form" onSubmit={create}>
        <select value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} required>
          <option value="">Category…</option>
          {expenseCategories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <input type="number" min="1" step="0.01" placeholder="Limit" value={form.limitAmount}
          onChange={(e) => setForm({ ...form, limitAmount: e.target.value })} required />
        <select value={form.month} onChange={(e) => setForm({ ...form, month: e.target.value })}>
          {MONTHS.map((m, i) => <option key={m} value={i + 1}>{m}</option>)}
        </select>
        <input className="input--short" type="number" min="2000" max="2100" value={form.year}
          onChange={(e) => setForm({ ...form, year: e.target.value })} required />
        <button className="btn btn--primary" disabled={busy}>Add budget</button>
      </form>

      {budgets.length === 0 ? <div className="empty card"><p>No budgets yet.</p></div> : (
        <div className="account-grid">
          {budgets.map((b) => (
            <div key={b.id} className="card">
              <p className="eyebrow">{MONTHS[b.month - 1]} {b.year}</p>
              {statusById[b.id] && <BudgetBar status={statusById[b.id]} label={categoryName(b.categoryId)} currency={currency} />}
              {editing?.id === b.id ? (
                <form className="inline-form" onSubmit={saveLimit}>
                  <input type="number" min="1" step="0.01" value={editing.limitAmount}
                    onChange={(e) => setEditing({ ...editing, limitAmount: e.target.value })} required autoFocus />
                  <button className="btn btn--primary btn--sm" disabled={busy}>Save</button>
                  <button type="button" className="btn btn--ghost btn--sm" onClick={() => setEditing(null)}>Cancel</button>
                </form>
              ) : (
                <div className="row-actions">
                  <button type="button" className="btn btn--ghost btn--sm" onClick={() => setEditing({ id: b.id, limitAmount: b.limitAmount })}>Change limit</button>
                  <button type="button" className="btn btn--danger btn--sm" onClick={() => remove(b)} disabled={busy}>Delete</button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </>
  );
}
