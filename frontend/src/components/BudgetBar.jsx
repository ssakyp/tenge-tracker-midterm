import { formatMoney } from './format';

export default function BudgetBar({ status, label, currency = 'KZT' }) {
  const limit = Number(status.limitAmount);
  const spent = Number(status.spentAmount);
  const pct = limit > 0 ? Math.min(100, Math.round((spent / limit) * 100)) : 0;
  const tone = status.isExceeded ? 'over' : pct >= 80 ? 'warn' : 'ok';
  return (
    <div className="budget">
      <div className="budget__top">
        <span>{label}</span>
        <span className="muted">{formatMoney(spent, currency)} / {formatMoney(limit, currency)}</span>
      </div>
      <div className="bar" role="progressbar" aria-valuenow={pct} aria-valuemin={0} aria-valuemax={100}>
        <div className={`bar__fill bar__fill--${tone}`} style={{ width: `${pct}%` }} />
      </div>
      <div className={`budget__note budget__note--${tone}`}>
        {status.isExceeded
          ? `Over budget by ${formatMoney(-Number(status.remainingAmount), currency)}`
          : `${formatMoney(status.remainingAmount, currency)} left (${pct}% used)`}
      </div>
    </div>
  );
}
