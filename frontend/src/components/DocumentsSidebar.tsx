import { FileUpload } from "./FileUpload";
import type { DocumentStatus, DocumentSummary } from "../api/types";

type StatusMeta = { label: string; tone: "ready" | "working" | "failed" };

function statusMeta(status: DocumentStatus): StatusMeta {
  switch (status) {
    case "INDEXED":
      return { label: "Ready", tone: "ready" };
    case "FAILED":
      return { label: "Failed", tone: "failed" };
    default:
      return { label: "Indexing", tone: "working" };
  }
}

function FileGlyph({ mimetype }: { mimetype: string }) {
  const isPdf = mimetype.includes("pdf");
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8l-5-5Z"
        stroke="currentColor"
        strokeWidth="1.6"
        strokeLinejoin="round"
      />
      <path d="M14 3v5h5" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
      {isPdf ? (
        <path d="M8.5 15.5h1M8.5 13h3M8.5 18h4" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
      ) : (
        <path d="M8.5 13h7M8.5 15.5h7M8.5 18h4" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" />
      )}
    </svg>
  );
}

export function DocumentsSidebar({
  username,
  documents,
  onLogout,
  onUploaded,
  onAuthError,
}: {
  username: string;
  documents: DocumentSummary[];
  onLogout: () => void;
  onUploaded: () => void;
  onAuthError: () => void;
}) {
  const initial = username.charAt(0).toUpperCase();

  return (
    <aside className="side">
      <div className="side__brand">
        <span className="logo logo--sm">IH</span>
        <div>
          <span className="brand__name">InsightHub</span>
          <span className="brand__tag">Document Q&amp;A</span>
        </div>
      </div>

      <div className="side__section">
        <div className="side__heading">
          <span>Documents</span>
          <span className="side__count tabular">{documents.length}</span>
        </div>

        <ul className="doclist">
          {documents.length === 0 && (
            <li className="doclist__empty">No documents yet — add one below.</li>
          )}
          {documents.map((d, i) => {
            const meta = statusMeta(d.status);
            return (
              <li
                key={d.id}
                className="doc"
                style={{ "--i": i } as React.CSSProperties}
              >
                <span className="doc__glyph">
                  <FileGlyph mimetype={d.mimetype} />
                </span>
                <span className="doc__name">{d.filename}</span>
                <span className={`dot dot--${meta.tone}`} title={meta.label} />
              </li>
            );
          })}
        </ul>
      </div>

      <div className="side__upload">
        <FileUpload compact onUploaded={onUploaded} onAuthError={onAuthError} />
      </div>

      <div className="side__user">
        <span className="userchip__avatar">{initial}</span>
        <span className="userchip__name">{username}</span>
        <button className="btn btn--ghost" onClick={onLogout}>
          Log out
        </button>
      </div>
    </aside>
  );
}
