export default function TagChip({ tag, selected, onClick }) {
  const style = { '--tag-color': tag.color || '#64748b' };
  if (onClick) {
    return (
      <button type="button" className={`tag tag--button${selected ? ' tag--selected' : ''}`} style={style}
        onClick={onClick} aria-pressed={selected}>
        #{tag.name}
      </button>
    );
  }
  return <span className="tag" style={style}>#{tag.name}</span>;
}
