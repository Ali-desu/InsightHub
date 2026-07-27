export function Header({
  username,
  onLogout,
}: {
  username: string;
  onLogout: () => void;
}) {
  const initial = username.charAt(0).toUpperCase();
  return (
    <header className="header">
      <div className="header__inner">
        <div className="brand">
          <span className="logo logo--sm">IH</span>
          <div>
            <span className="brand__name">InsightHub</span>
            <span className="brand__tag">Document Q&amp;A</span>
          </div>
        </div>

        <div className="userchip">
          <span className="userchip__avatar">{initial}</span>
          <span className="userchip__name">{username}</span>
          <button className="btn btn--ghost" onClick={onLogout}>
            Log out
          </button>
        </div>
      </div>
    </header>
  );
}
