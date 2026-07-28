import { useCallback, useEffect, useState } from "react";
import { AuthPanel } from "./components/AuthPanel";
import { DocumentsSidebar } from "./components/DocumentsSidebar";
import { AskPanel } from "./components/AskPanel";
import { listDocuments } from "./api/client";
import type { DocumentSummary } from "./api/types";
import { clearToken, getToken, setToken, usernameFromToken } from "./auth";
import "./App.css";

const TERMINAL = new Set(["INDEXED", "FAILED"]);

export default function App() {
  const [token, setTokenState] = useState<string | null>(() => {
    const existing = getToken();
    if (existing && usernameFromToken(existing)) return existing;
    if (existing) clearToken(); // expired / malformed
    return null;
  });
  const [documents, setDocuments] = useState<DocumentSummary[]>([]);

  const username = token ? usernameFromToken(token) : null;

  const logout = useCallback(() => {
    clearToken();
    setTokenState(null);
    setDocuments([]);
  }, []);

  const loadDocuments = useCallback(async () => {
    try {
      setDocuments(await listDocuments());
    } catch (e) {
      if (e instanceof Error && e.message.includes("Not authorized")) logout();
    }
  }, [logout]);

  // Load documents once we have a session.
  useEffect(() => {
    if (token) loadDocuments();
  }, [token, loadDocuments]);

  // While anything is still indexing, poll so the sidebar status updates live.
  useEffect(() => {
    if (!token) return;
    const pending = documents.some((d) => !TERMINAL.has(d.status));
    if (!pending) return;
    const id = setInterval(loadDocuments, 2500);
    return () => clearInterval(id);
  }, [token, documents, loadDocuments]);

  function handleAuthenticated(newToken: string) {
    setToken(newToken);
    setTokenState(newToken);
  }

  if (!token || !username) {
    return <AuthPanel onAuthenticated={handleAuthenticated} />;
  }

  return (
    <div className="shell">
      <div className="shell__glow" aria-hidden="true" />
      <DocumentsSidebar
        username={username}
        documents={documents}
        onLogout={logout}
        onUploaded={loadDocuments}
        onAuthError={logout}
      />
      <main className="workspace">
        <div className="workspace__inner">
          <AskPanel onAuthError={logout} />
        </div>
      </main>
    </div>
  );
}
