import { useCallback, useEffect, useMemo, useState } from "react";
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
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  const username = token ? usernameFromToken(token) : null;

  const logout = useCallback(() => {
    clearToken();
    setTokenState(null);
    setDocuments([]);
    setSelectedIds([]);
  }, []);

  const loadDocuments = useCallback(async () => {
    try {
      setDocuments(await listDocuments());
    } catch (e) {
      if (e instanceof Error && e.message.includes("Not authorized")) logout();
    }
  }, [logout]);

  useEffect(() => {
    if (token) loadDocuments();
  }, [token, loadDocuments]);

  // Poll while anything is still indexing so the sidebar status updates live.
  useEffect(() => {
    if (!token) return;
    const pending = documents.some((d) => !TERMINAL.has(d.status));
    if (!pending) return;
    const id = setInterval(loadDocuments, 2500);
    return () => clearInterval(id);
  }, [token, documents, loadDocuments]);

  // Drop any selected ids that no longer exist (e.g. after a reload).
  useEffect(() => {
    setSelectedIds((prev) => prev.filter((id) => documents.some((d) => d.id === id)));
  }, [documents]);

  const toggleDoc = useCallback((id: number) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
  }, []);

  const scopeNames = useMemo(
    () => documents.filter((d) => selectedIds.includes(d.id)).map((d) => d.filename),
    [documents, selectedIds]
  );

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
        selectedIds={selectedIds}
        onToggleDocument={toggleDoc}
        onLogout={logout}
        onUploaded={loadDocuments}
        onAuthError={logout}
      />
      <main className="workspace">
        <div className="workspace__inner">
          <AskPanel
            selectedIds={selectedIds}
            scopeNames={scopeNames}
            onClearScope={() => setSelectedIds([])}
            onAuthError={logout}
          />
        </div>
      </main>
    </div>
  );
}
