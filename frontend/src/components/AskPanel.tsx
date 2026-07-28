import { useRef, useState } from "react";
import { ask } from "../api/client";
import type { AskResponse } from "../api/types";

type Status = "idle" | "asking" | "answered" | "error";

const SUGGESTIONS = [
  "What is this document about?",
  "Summarize the key points.",
  "How does the ingestion pipeline work?",
];

export function AskPanel({ onAuthError }: { onAuthError?: () => void }) {
  const [question, setQuestion] = useState("");
  const [status, setStatus] = useState<Status>("idle");
  const [result, setResult] = useState<AskResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);

  async function submit(q: string) {
    const trimmed = q.trim();
    if (!trimmed || status === "asking") return;
    setStatus("asking");
    setError(null);
    setResult(null);
    try {
      const res = await ask({ question: trimmed });
      setResult(res);
      setStatus("answered");
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      setError(msg);
      setStatus("error");
      if (msg.includes("Not authorized")) onAuthError?.();
    }
  }

  function onKeyDown(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    // Enter submits; Shift+Enter for a newline.
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      submit(question);
    }
  }

  const asking = status === "asking";

  return (
    <section className="ask">
      <div className="ask__head">
        <h2 className="ask__title">Ask your documents</h2>
        <p className="ask__subtitle">
          Answers are grounded in your uploaded files and cite the exact passages they came from.
        </p>
      </div>

      <form
        className="ask__form"
        onSubmit={(e) => {
          e.preventDefault();
          submit(question);
        }}
      >
        <textarea
          ref={inputRef}
          className="ask__input"
          placeholder="Ask anything about your documents…"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          onKeyDown={onKeyDown}
          rows={2}
        />
        <button
          className="btn btn--primary ask__submit"
          type="submit"
          disabled={asking || !question.trim()}
        >
          {asking ? (
            <>
              <span className="spinner" aria-hidden="true" />
              Thinking…
            </>
          ) : (
            <>
              Ask
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                aria-hidden="true"
              >
                <path
                  d="M5 12h14m0 0-6-6m6 6-6 6"
                  stroke="currentColor"
                  strokeWidth="1.75"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </svg>
            </>
          )}
        </button>
      </form>

      {status === "idle" && (
        <div className="suggest">
          {SUGGESTIONS.map((s) => (
            <button
              key={s}
              type="button"
              className="suggest__chip"
              onClick={() => {
                setQuestion(s);
                submit(s);
              }}
            >
              {s}
            </button>
          ))}
        </div>
      )}

      {asking && (
        <div className="answer answer--loading" aria-live="polite">
          <div className="skeleton skeleton--line" />
          <div className="skeleton skeleton--line" />
          <div className="skeleton skeleton--line skeleton--short" />
        </div>
      )}

      {status === "error" && error && (
        <p className="ask__error" role="alert">
          {error}
        </p>
      )}

      {status === "answered" && result && (
        <div className="answer" aria-live="polite">
          <p className="answer__text">{result.answer}</p>

          {result.citations.length > 0 && (
            <div className="citations">
              <div className="citations__label">
                <svg
                  width="15"
                  height="15"
                  viewBox="0 0 24 24"
                  fill="none"
                  aria-hidden="true"
                >
                  <path
                    d="M7 8h10M7 12h10M7 16h6M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2Z"
                    stroke="currentColor"
                    strokeWidth="1.75"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                </svg>
                {result.citations.length}{" "}
                {result.citations.length === 1 ? "source" : "sources"}
              </div>
              <ul className="citations__list">
                {result.citations.map((c, i) => (
                  <li
                    key={`${c.documentId}-${c.chunkIndex}-${i}`}
                    className="cite"
                    style={{ "--i": i } as React.CSSProperties}
                  >
                    <div className="cite__meta">
                      <span className="cite__doc">Document #{c.documentId}</span>
                      <span className="cite__chunk">chunk {c.chunkIndex}</span>
                    </div>
                    <blockquote className="cite__quote">{c.quotedText.trim()}</blockquote>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </section>
  );
}
