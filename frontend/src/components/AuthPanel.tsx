import { useState } from "react";
import { login, register } from "../api/client";

type Mode = "login" | "register";

export function AuthPanel({
  onAuthenticated,
}: {
  onAuthenticated: (token: string) => void;
}) {
  const [mode, setMode] = useState<Mode>("login");
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setBusy(true);
    try {
      if (mode === "register") {
        await register({ username, email, password });
      }
      // Log in (for register too, so the user lands straight in).
      const { token } = await login({ username, password });
      onAuthenticated(token);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth">
      <div className="auth__card">
        <div className="auth__brand">
          <span className="logo">IH</span>
          <h1>InsightHub</h1>
          <p>Upload documents, ask questions, get cited answers.</p>
        </div>

        <div className="tabs">
          <button
            type="button"
            className={mode === "login" ? "tabs__btn tabs__btn--on" : "tabs__btn"}
            onClick={() => {
              setMode("login");
              setError(null);
            }}
          >
            Log in
          </button>
          <button
            type="button"
            className={
              mode === "register" ? "tabs__btn tabs__btn--on" : "tabs__btn"
            }
            onClick={() => {
              setMode("register");
              setError(null);
            }}
          >
            Create account
          </button>
        </div>

        <form className="form" onSubmit={submit}>
          <label className="field">
            <span>Username</span>
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              required
            />
          </label>

          {mode === "register" && (
            <label className="field">
              <span>Email</span>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
                required
              />
            </label>
          )}

          <label className="field">
            <span>Password</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete={
                mode === "login" ? "current-password" : "new-password"
              }
              required
            />
          </label>

          {error && <p className="form__error">{error}</p>}

          <button className="btn btn--primary" type="submit" disabled={busy}>
            {busy
              ? "Please wait…"
              : mode === "login"
                ? "Log in"
                : "Create account & continue"}
          </button>
        </form>
      </div>
      <p className="auth__foot">Spring Boot · JWT · S3 · React</p>
    </div>
  );
}
