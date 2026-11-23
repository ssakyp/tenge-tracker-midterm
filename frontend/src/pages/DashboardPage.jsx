import { Link } from 'react-router-dom';
import { api } from '../api/client';
import { useApi } from '../api/useApi';
import { useAuth } from '../auth/AuthContext';
import ErrorMessage from '../components/ErrorMessage';
import Spinner from '../components/Spinner';
import { currentMonthRange, formatDate, formatMoney, MONTHS } from '../components/format';
import BudgetBar from '../components/BudgetBar';

export default function DashboardPage() {
  const { user } = useAuth();
  const range = currentMonthRange();
  const currency = user.profile?.preferredCurrency || 'KZT';

  const { data, loading, error, reload } = useApi(async (signal) => {
    const [summary, accounts, transactions, budgets, categories] = await Promise.all([
      api.get(`/summary?startDate=${range.start}&endDate=${range.end}`, { signal }),
      api.get('/accounts', { signal }),
      api.get('/transactions', { signal }),
      api.get(`/budgets?month=${range.month}&year=${range.year}`, { signal }),
      api.get('/categories', { signal }),
    ]);
    const statuses = await Promise.all(budgets.map((b) => api.get(`/budgets/${b.id}/status`, { signal })));
    return { summary, accounts, transactions: transactions.slice(0, 6), statuses, categories };
  });

  if (loading && !data) return <Spinner label="Loading your dashboard…" />;
  if (error && !data) return <ErrorMessage error={error} onRetry={reload} />;

  const { summary, accounts, transactions, statuses, categories } = data;
  const categoryName = (id) => categories.find((c) => c.id === id)?.name ?? '—';
  const accountById = (id) => accounts.find((a) => a.id === id);

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Hello, {user.profile?.fullName || user.username}</h1>
          <p className="muted">{MONTHS[range.month - 1]} {range.year} overview</p>
        </div>
        <Link to="/transactions" className="btn btn--primary">+ New transaction</Link>
      </div>
      <ErrorMessage error={error} onRetry={reload} />

      <section className="stats">
        <Stat label="Income this month" value={formatMoney(summary.totalIncome, currency)} tone="income" />
        <Stat label="Expenses this month" value={formatMoney(summary.totalExpenses, currency)} tone="expense" />
        <Stat label="Net" value={formatMoney(summary.netBalance, currency)} tone={summary.netBalance >= 0 ? 'income' : 'expense'} />
        <Stat label="Total in accounts" value={formatMoney(summary.totalAssets, currency)} hint="sum of balances, mixed currencies" />
      </section>

      <div className="grid-2">
        <section className="card">
          <div className="card__header">
            <h2>Accounts</h2>
            <Link to="/accounts" className="link">Manage →</Link>
          </div>
          {accounts.length === 0 ? <p className="muted">No accounts yet.</p> : (
            <ul className="list">
              {accounts.map((a) => (
                <li key={a.id} className="list__item">
                  <span className="grow">{a.name}</span>
                  <strong>{formatMoney(a.balance, a.currency)}</strong>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="card">
          <div className="card__header">
            <h2>Budgets this month</h2>
            <Link to="/budgets" className="link">Manage →</Link>
          </div>
          {statuses.length === 0 ? <p className="muted">No budgets for this month.</p> : (
            <div className="stack">
              {statuses.map((s) => (
                <BudgetBar key={s.budgetId} status={s} label={categoryName(s.categoryId)} currency={currency} />
              ))}
            </div>
          )}
        </section>
      </div>

      <section className="card">
        <div className="card__header">
          <h2>Recent transactions</h2>
          <Link to="/transactions" className="link">See all →</Link>
        </div>
        {transactions.length === 0 ? <p className="muted">No transactions yet.</p> : (
          <ul className="list">
            {transactions.map((t) => {
              const account = accountById(t.accountId);
              return (
                <li key={t.id} className="list__item">
                  <span className="muted nowrap">{formatDate(t.date)}</span>
                  <span className="grow">{t.description || (t.categoryId ? categoryName(t.categoryId) : t.type)}</span>
                  <span className={`amount amount--${t.type.toLowerCase()}`}>
                    {t.type === 'EXPENSE' ? '−' : t.type === 'INCOME' ? '+' : '⇄ '}
                    {formatMoney(t.amount, account?.currency || 'KZT')}
                  </span>
                </li>
              );
            })}
          </ul>
        )}
      </section>
    </>
  );
}

function Stat({ label, value, tone, hint }) {
  return (
    <div className={`stat card${tone ? ` stat--${tone}` : ''}`}>
      <span className="stat__label">{label}</span>
      <span className="stat__value">{value}</span>
      {hint && <span className="stat__hint">{hint}</span>}
    </div>
  );
}
