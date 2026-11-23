/** Shows an ApiError (message + field errors) or any string. */
export default function ErrorMessage({ error, onRetry }) {
  if (!error) return null;
  const message = typeof error === 'string' ? error : error.message;
  const fieldErrors = typeof error === 'object' ? error.fieldErrors : null;
  return (
    <div className="alert alert--error" role="alert">
      <div>
        <strong>{error.status ? `Error ${error.status}: ` : ''}</strong>
        {message}
        {fieldErrors && (
          <ul className="alert__list">
            {Object.entries(fieldErrors).map(([field, msg]) => (
              <li key={field}><code>{field}</code>: {msg}</li>
            ))}
          </ul>
        )}
      </div>
      {onRetry && <button type="button" className="btn btn--ghost btn--sm" onClick={onRetry}>Retry</button>}
    </div>
  );
}

export function SuccessMessage({ children }) {
  if (!children) return null;
  return <div className="alert alert--success" role="status">{children}</div>;
}
