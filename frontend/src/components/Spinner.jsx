export default function Spinner({ label = 'Loading…', fullPage = false, small = false }) {
  if (small) return <span className="spinner spinner--small" role="status" aria-label={label} />;
  return (
    <div className={fullPage ? 'loading loading--page' : 'loading'} role="status">
      <span className="spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  );
}
