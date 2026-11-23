import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { useApi } from '../api/useApi';
import { useAuth } from '../auth/AuthContext';
import ErrorMessage from '../components/ErrorMessage';
import Spinner from '../components/Spinner';
import TagChip from '../components/TagChip';

/** Public landing page. Uses the public GET endpoints, so it works without a token. */
export default function HomePage() {
  const { isAuthenticated, user } = useAuth();
  const { data, loading, error, reload } = useApi(async (signal) => {
    const [categories, tags] = await Promise.all([
      api.get('/categories', { auth: false, signal }),
      api.get('/tags', { auth: false, signal }),
    ]);
    return { categories, tags };
  });

  return (
    <>
      <section className="hero">
        <div>
          <h1>Know where every tenge goes.</h1>
          <p className="hero__lead">
            Track accounts, income, expenses and transfers, tag them, and stay within monthly budgets.
          </p>
          <div className="hero__actions">
            {isAuthenticated ? (
              <Link to="/dashboard" className="btn btn--primary">Open dashboard, {user.username}</Link>
            ) : (
              <>
                <Link to="/signup" className="btn btn--primary">Create a free account</Link>
                <Link to="/login" className="btn btn--ghost">Log in</Link>
              </>
            )}
          </div>
        </div>
        <div className="hero__card card">
          <p className="eyebrow">Public data — no login needed</p>
          {loading && <Spinner label="Loading categories and tags…" />}
          <ErrorMessage error={error} onRetry={reload} />
          {data && (
            <>
              <p><strong>{data.categories.length}</strong> categories ·{' '}
                <strong>{data.tags.length}</strong> tags</p>
              <div className="tag-list">
                {data.tags.map((tag) => <TagChip key={tag.id} tag={tag} />)}
              </div>
              <Link to="/catalog" className="link">Browse all categories and tags →</Link>
            </>
          )}
        </div>
      </section>

      <section className="features">
        <div className="card"><h3>Private by design</h3><p>Your accounts, transactions and budgets are visible only to you. The API checks your JWT on every request.</p></div>
        <div className="card"><h3>Balances that add up</h3><p>Every income, expense and transfer updates account balances in one database transaction.</p></div>
        <div className="card"><h3>Budgets that warn you</h3><p>Set monthly limits per category and see what is spent and what is left.</p></div>
      </section>
    </>
  );
}
