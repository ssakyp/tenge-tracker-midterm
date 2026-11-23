import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="empty card">
      <h1>404</h1>
      <p>This page does not exist.</p>
      <Link to="/" className="btn btn--primary">Go home</Link>
    </div>
  );
}
