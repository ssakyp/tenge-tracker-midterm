import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { useApi } from '../api/useApi';
import ErrorMessage, { SuccessMessage } from '../components/ErrorMessage';
import Modal from '../components/Modal';
import Spinner from '../components/Spinner';
import TagChip from '../components/TagChip';
import { formatDate, formatMoney, todayIso } from '../components/format';

const TYPES = ['EXPENSE', 'INCOME', 'TRANSFER'];

/**
 * Main protected feature: full CRUD for the user's transactions.
 * GET /transactions, POST /transactions, PUT /transactions/{id}, DELETE /transactions/{id}
 */
export default function TransactionsPage() {
  const [typeFilter, setTypeFilter] = useState('');
  const [tagFilter, setTagFilter] = useState(null);
  const [editing, setEditing] = useState(null); // null | 'new' | transaction
  const [notice, setNotice] = useState(null);
  const [actionError, setActionError] = useState(null);
  const [deletingId, setDeletingId] = useState(null);

  const { data, loading, error, reload } = useApi(async (signal) => {
    const [transactions, accounts, categories, tags] = await Promise.all([
      api.get('/transactions', { signal }),
      api.get('/accounts', { signal }),
      api.get('/categories', { signal }),
      api.get('/tags', { signal }),
    ]);
    return { transactions, accounts, categories, tags };
  });

  const visible = useMemo(() => {
    if (!data) return [];
    return data.transactions
      .filter((t) => !typeFilter || t.type === typeFilter)
      .filter((t) => !tagFilter || t.tags.some((tag) => tag.id === tagFilter));
  }, [data, typeFilter, tagFilter]);

  if (loading && !data) return <Spinner label="Loading transactions…" />;
  if (error && !data) return <ErrorMessage error={error} onRetry={reload} />;

  const { accounts, categories, tags } = data;
  const accountById = (id) => accounts.find((a) => a.id === id);
  const categoryById = (id) => categories.find((c) => c.id === id);

  const onSaved = async (message) => {
    setEditing(null);
    setNotice(message);
    setActionError(null);
    await reload();
  };

  const remove = async (t) => {
    if (!window.confirm(`Delete "${t.description || t.type}"? The account balance will be adjusted back.`)) return;
    setDeletingId(t.id);
    setActionError(null);
    setNotice(null);
    try {
      await api.del(`/transactions/${t.id}`);
      setNotice('Transaction deleted and balances restored.');
      await reload();
    } catch (err) {
      setActionError(err);
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Transactions</h1>
          <p className="muted">Income, expenses and transfers between your accounts. Balances update automatically.</p>
        </div>
        <button type="button" className="btn btn--primary" onClick={() => setEditing('new')} disabled={accounts.length === 0}>
          + New transaction
        </button>
      </div>

      {accounts.length === 0 && (
        <div className="alert alert--info">You need an account first. <Link to="/accounts">Create one →</Link></div>
      )}
      <SuccessMessage>{notice}</SuccessMessage>
      <ErrorMessage error={actionError} />
      <ErrorMessage error={error} onRetry={reload} />

      <div className="toolbar card">
        <div className="segmented" role="group" aria-label="Filter by type">
          {['', ...TYPES].map((t) => (
            <button key={t || 'all'} type="button" className={typeFilter === t ? 'active' : ''} onClick={() => setTypeFilter(t)}>
              {t ? t.charAt(0) + t.slice(1).toLowerCase() : 'All'}
            </button>
          ))}
        </div>
        <div className="tag-list">
          {tags.map((tag) => (
            <TagChip key={tag.id} tag={tag} selected={tagFilter === tag.id}
              onClick={() => setTagFilter(tagFilter === tag.id ? null : tag.id)} />
          ))}
        </div>
        {loading && <Spinner small label="Refreshing" />}
      </div>

      {visible.length === 0 ? (
        <div className="empty card"><p>No transactions match.</p></div>
      ) : (
        <div className="card table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Description</th>
                <th>Account</th>
                <th>Category</th>
                <th>Tags</th>
                <th className="right">Amount</th>
                <th aria-label="Actions" />
              </tr>
            </thead>
            <tbody>
              {visible.map((t) => {
                const account = accountById(t.accountId);
                const target = t.targetAccountId ? accountById(t.targetAccountId) : null;
                return (
                  <tr key={t.id}>
                    <td className="nowrap">{formatDate(t.date)}</td>
                    <td>{t.description || <span className="muted">—</span>}</td>
                    <td>
                      {account?.name ?? `#${t.accountId}`}
                      {target && <span className="muted"> → {target.name}</span>}
                    </td>
                    <td>{t.categoryId ? categoryById(t.categoryId)?.name : <span className="badge badge--transfer">TRANSFER</span>}</td>
                    <td><div className="tag-list">{t.tags.map((tag) => <TagChip key={tag.id} tag={tag} />)}</div></td>
                    <td className={`right nowrap amount amount--${t.type.toLowerCase()}`}>
                      {t.type === 'EXPENSE' ? '−' : t.type === 'INCOME' ? '+' : ''}
                      {formatMoney(t.amount, account?.currency || 'KZT')}
                    </td>
                    <td className="nowrap right">
                      <button type="button" className="btn btn--ghost btn--sm" onClick={() => setEditing(t)}>Edit</button>
                      <button type="button" className="btn btn--danger btn--sm" onClick={() => remove(t)} disabled={deletingId === t.id}>
                        {deletingId === t.id ? '…' : 'Delete'}
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {editing && (
        <TransactionForm
          initial={editing === 'new' ? null : editing}
          accounts={accounts}
          categories={categories}
          tags={tags}
          onCancel={() => setEditing(null)}
          onSaved={onSaved}
        />
      )}
    </>
  );
}

function TransactionForm({ initial, accounts, categories, tags, onCancel, onSaved }) {
  const [form, setForm] = useState(() => ({
    type: initial?.type ?? 'EXPENSE',
    accountId: initial?.accountId ?? accounts[0]?.id ?? '',
    targetAccountId: initial?.targetAccountId ?? '',
    categoryId: initial?.categoryId ?? '',
    amount: initial?.amount ?? '',
    date: initial?.date ?? todayIso(),
    description: initial?.description ?? '',
    tagIds: initial?.tags?.map((t) => t.id) ?? [],
  }));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });
  const matchingCategories = categories.filter((c) => c.type === form.type);
  const isTransfer = form.type === 'TRANSFER';

  const changeType = (type) => {
    // a category of the wrong type would be rejected by the API, so reset it
    setForm({ ...form, type, categoryId: '', targetAccountId: type === 'TRANSFER' ? form.targetAccountId : '' });
  };

  const toggleTag = (id) => setForm({
    ...form,
    tagIds: form.tagIds.includes(id) ? form.tagIds.filter((x) => x !== id) : [...form.tagIds, id],
  });

  const onSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    const body = {
      type: form.type,
      accountId: Number(form.accountId),
      targetAccountId: isTransfer && form.targetAccountId ? Number(form.targetAccountId) : null,
      categoryId: !isTransfer && form.categoryId ? Number(form.categoryId) : null,
      amount: Number(form.amount),
      date: form.date,
      description: form.description.trim() || null,
      tagIds: form.tagIds,
    };
    try {
      if (initial) {
        await api.put(`/transactions/${initial.id}`, body);
        await onSaved('Transaction updated.');
      } else {
        await api.post('/transactions', body);
        await onSaved('Transaction added.');
      }
    } catch (err) {
      setError(err);
      setSaving(false);
    }
  };

  return (
    <Modal title={initial ? 'Edit transaction' : 'New transaction'} onClose={onCancel}>
      <ErrorMessage error={error} />
      <form className="form" onSubmit={onSubmit}>
        <div className="segmented segmented--full" role="group" aria-label="Type">
          {TYPES.map((t) => (
            <button key={t} type="button" className={form.type === t ? 'active' : ''} onClick={() => changeType(t)}>
              {t.charAt(0) + t.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
        <div className="form__row">
          <label>
            {isTransfer ? 'From account' : 'Account'}
            <select value={form.accountId} onChange={set('accountId')} required>
              {accounts.map((a) => <option key={a.id} value={a.id}>{a.name} ({formatMoney(a.balance, a.currency)})</option>)}
            </select>
          </label>
          {isTransfer ? (
            <label>
              To account
              <select value={form.targetAccountId} onChange={set('targetAccountId')} required>
                <option value="">Choose…</option>
                {accounts.filter((a) => String(a.id) !== String(form.accountId)).map((a) => (
                  <option key={a.id} value={a.id}>{a.name}</option>
                ))}
              </select>
            </label>
          ) : (
            <label>
              Category
              <select value={form.categoryId} onChange={set('categoryId')}>
                <option value="">No category</option>
                {matchingCategories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </label>
          )}
        </div>
        <div className="form__row">
          <label>
            Amount
            <input type="number" min="0.01" step="0.01" value={form.amount} onChange={set('amount')} required />
          </label>
          <label>
            Date
            <input type="date" value={form.date} onChange={set('date')} required />
          </label>
        </div>
        <label>
          Description
          <input value={form.description} onChange={set('description')} maxLength={255} placeholder="e.g. Groceries at Magnum" />
        </label>
        <div>
          <span className="label">Tags</span>
          <div className="tag-list">
            {tags.map((tag) => (
              <TagChip key={tag.id} tag={tag} selected={form.tagIds.includes(tag.id)} onClick={() => toggleTag(tag.id)} />
            ))}
          </div>
        </div>
        <div className="form__actions">
          <button type="button" className="btn btn--ghost" onClick={onCancel}>Cancel</button>
          <button className="btn btn--primary" disabled={saving}>
            {saving ? <><Spinner small /> Saving…</> : initial ? 'Save changes' : 'Add transaction'}
          </button>
        </div>
      </form>
    </Modal>
  );
}
