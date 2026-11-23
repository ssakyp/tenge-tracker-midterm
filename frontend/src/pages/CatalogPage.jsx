import { useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { useApi } from '../api/useApi';
import { useAuth } from '../auth/AuthContext';
import ErrorMessage from '../components/ErrorMessage';
import Spinner from '../components/Spinner';
import TagChip from '../components/TagChip';

/**
 * Public page: anyone can read categories and tags (public GET endpoints).
 * Editing controls appear only when logged in, because POST/PATCH/DELETE need a JWT.
 */
export default function CatalogPage() {
  const { isAuthenticated } = useAuth();
  const { data, loading, error, reload } = useApi(async (signal) => {
    const [categories, tags] = await Promise.all([
      api.get('/categories', { signal }),
      api.get('/tags', { signal }),
    ]);
    return { categories, tags };
  });

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Categories &amp; tags</h1>
          <p className="muted">Shared by all users. Reading is public; changing requires login.</p>
        </div>
      </div>
      {!isAuthenticated && (
        <div className="alert alert--info">
          You are browsing as a guest. <Link to="/login" state={{ from: '/catalog' }}>Log in</Link> to add, rename or delete items.
        </div>
      )}
      {loading && !data && <Spinner />}
      <ErrorMessage error={error} onRetry={reload} />
      {data && (
        <div className="grid-2">
          <CategoriesPanel categories={data.categories} canEdit={isAuthenticated} onChanged={reload} />
          <TagsPanel tags={data.tags} canEdit={isAuthenticated} onChanged={reload} />
        </div>
      )}
    </>
  );
}

function CategoriesPanel({ categories, canEdit, onChanged }) {
  const [form, setForm] = useState({ name: '', type: 'EXPENSE' });
  const [editing, setEditing] = useState(null); // { id, name }
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);

  const run = async (fn) => {
    setBusy(true);
    setError(null);
    try {
      await fn();
      await onChanged();
      return true;
    } catch (err) {
      setError(err);
      return false;
    } finally {
      setBusy(false);
    }
  };

  const create = (e) => {
    e.preventDefault();
    run(async () => {
      await api.post('/categories', { name: form.name.trim(), type: form.type });
      setForm({ name: '', type: form.type });
    });
  };

  const saveEdit = (e) => {
    e.preventDefault();
    run(async () => {
      await api.patch(`/categories/${editing.id}`, { name: editing.name.trim() });
      setEditing(null);
    });
  };

  const remove = (category) => {
    if (!window.confirm(`Delete category "${category.name}"?`)) return;
    run(() => api.del(`/categories/${category.id}`));
  };

  return (
    <section className="card">
      <h2>Categories <span className="count">{categories.length}</span></h2>
      <ErrorMessage error={error} />
      {canEdit && (
        <form className="inline-form" onSubmit={create}>
          <input placeholder="New category name" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>
          <button className="btn btn--primary btn--sm" disabled={busy}>Add</button>
        </form>
      )}
      <ul className="list">
        {categories.map((c) => (
          <li key={c.id} className="list__item">
            {editing?.id === c.id ? (
              <form className="inline-form grow" onSubmit={saveEdit}>
                <input value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} autoFocus required />
                <button className="btn btn--primary btn--sm" disabled={busy}>Save</button>
                <button type="button" className="btn btn--ghost btn--sm" onClick={() => setEditing(null)}>Cancel</button>
              </form>
            ) : (
              <>
                <span className="grow">{c.name}</span>
                <span className={`badge badge--${c.type.toLowerCase()}`}>{c.type}</span>
                {canEdit && (
                  <span className="row-actions">
                    <button type="button" className="btn btn--ghost btn--sm" onClick={() => setEditing({ id: c.id, name: c.name })}>Rename</button>
                    <button type="button" className="btn btn--danger btn--sm" onClick={() => remove(c)} disabled={busy}>Delete</button>
                  </span>
                )}
              </>
            )}
          </li>
        ))}
      </ul>
    </section>
  );
}

function TagsPanel({ tags, canEdit, onChanged }) {
  const [form, setForm] = useState({ name: '', color: '#0f766e' });
  const [editing, setEditing] = useState(null); // { id, name, color }
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);

  const run = async (fn) => {
    setBusy(true);
    setError(null);
    try {
      await fn();
      await onChanged();
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  };

  const create = (e) => {
    e.preventDefault();
    run(async () => {
      await api.post('/tags', { name: form.name.trim(), color: form.color });
      setForm({ ...form, name: '' });
    });
  };

  const saveEdit = (e) => {
    e.preventDefault();
    run(async () => {
      await api.patch(`/tags/${editing.id}`, { name: editing.name.trim(), color: editing.color });
      setEditing(null);
    });
  };

  const remove = (tag) => {
    if (!window.confirm(`Delete tag #${tag.name}? It will be removed from all transactions.`)) return;
    run(() => api.del(`/tags/${tag.id}`));
  };

  return (
    <section className="card">
      <h2>Tags <span className="count">{tags.length}</span></h2>
      <ErrorMessage error={error} />
      {canEdit && (
        <form className="inline-form" onSubmit={create}>
          <input placeholder="New tag" value={form.name} maxLength={30} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          <input type="color" value={form.color} onChange={(e) => setForm({ ...form, color: e.target.value })} aria-label="Tag color" />
          <button className="btn btn--primary btn--sm" disabled={busy}>Add</button>
        </form>
      )}
      <ul className="list">
        {tags.map((t) => (
          <li key={t.id} className="list__item">
            {editing?.id === t.id ? (
              <form className="inline-form grow" onSubmit={saveEdit}>
                <input value={editing.name} maxLength={30} onChange={(e) => setEditing({ ...editing, name: e.target.value })} autoFocus required />
                <input type="color" value={editing.color} onChange={(e) => setEditing({ ...editing, color: e.target.value })} aria-label="Tag color" />
                <button className="btn btn--primary btn--sm" disabled={busy}>Save</button>
                <button type="button" className="btn btn--ghost btn--sm" onClick={() => setEditing(null)}>Cancel</button>
              </form>
            ) : (
              <>
                <span className="grow"><TagChip tag={t} /></span>
                {canEdit && (
                  <span className="row-actions">
                    <button type="button" className="btn btn--ghost btn--sm" onClick={() => setEditing({ id: t.id, name: t.name, color: t.color || '#64748b' })}>Edit</button>
                    <button type="button" className="btn btn--danger btn--sm" onClick={() => remove(t)} disabled={busy}>Delete</button>
                  </span>
                )}
              </>
            )}
          </li>
        ))}
      </ul>
    </section>
  );
}
