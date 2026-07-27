import { useState } from "react";
import { AuthPanel } from "./components/AuthPanel";
import { Header } from "./components/Header";
import { FileUpload } from "./components/FileUpload";
import { clearToken, getToken, setToken, usernameFromToken } from "./auth";
import "./App.css";

export default function App() {
  const [token, setTokenState] = useState<string | null>(() => {
    const existing = getToken();
    if (existing && usernameFromToken(existing)) return existing;
    if (existing) clearToken(); // expired / malformed
    return null;
  });

  const username = token ? usernameFromToken(token) : null;

  function handleAuthenticated(newToken: string) {
    setToken(newToken);
    setTokenState(newToken);
  }

  function logout() {
    clearToken();
    setTokenState(null);
  }

  if (!token || !username) {
    return <AuthPanel onAuthenticated={handleAuthenticated} />;
  }

  return (
    <div className="app">
      <Header username={username} onLogout={logout} />
      <main className="app__main">
        <section className="card">
          <div className="card__head">
            <h2>Upload documents</h2>
            <p>
              Files upload straight to S3 via a presigned URL — authenticated as{" "}
              <strong>{username}</strong>. The backend never touches the bytes.
            </p>
          </div>
          <FileUpload onAuthError={logout} />
        </section>
      </main>
      <footer className="app__footer">Spring Boot · JWT · S3 · React</footer>
    </div>
  );
}
