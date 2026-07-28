import { useCallback, useRef, useState } from "react";
import { confirmUpload, createUpload, uploadToS3 } from "../api/client";

type Phase =
  | "queued"
  | "creating"
  | "uploading"
  | "confirming"
  | "done"
  | "error";

interface UploadItem {
  key: string;
  file: File;
  phase: Phase;
  progress: number;
  documentId?: number;
  status?: string;
  error?: string;
}

const PHASE_LABEL: Record<Phase, string> = {
  queued: "Queued",
  creating: "Reserving…",
  uploading: "Uploading",
  confirming: "Confirming…",
  done: "Uploaded",
  error: "Failed",
};

function formatSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export function FileUpload({
  onAuthError,
  onUploaded,
  compact = false,
}: {
  onAuthError?: () => void;
  onUploaded?: () => void;
  compact?: boolean;
}) {
  const [items, setItems] = useState<UploadItem[]>([]);
  const [dragging, setDragging] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const patch = useCallback((key: string, changes: Partial<UploadItem>) => {
    setItems((prev) =>
      prev.map((it) => (it.key === key ? { ...it, ...changes } : it))
    );
  }, []);

  const runUpload = useCallback(
    async (item: UploadItem) => {
      const contentType = item.file.type || "application/octet-stream";
      try {
        patch(item.key, { phase: "creating" });
        const created = await createUpload({
          filename: item.file.name,
          contentType,
        });

        patch(item.key, {
          phase: "uploading",
          documentId: created.documentId,
          progress: 0,
        });
        await uploadToS3(created.uploadUrl, item.file, contentType, (pct) =>
          patch(item.key, { progress: pct })
        );

        patch(item.key, { phase: "confirming" });
        const confirmed = await confirmUpload(created.documentId);

        patch(item.key, { phase: "done", status: confirmed.status });
        onUploaded?.(); // tell the app to refresh the documents list
      } catch (e) {
        const msg = e instanceof Error ? e.message : String(e);
        patch(item.key, { phase: "error", error: msg });
        if (msg.includes("Not authorized")) onAuthError?.();
      }
    },
    [patch, onAuthError, onUploaded]
  );

  const addFiles = useCallback(
    (files: FileList | File[]) => {
      const newItems: UploadItem[] = Array.from(files).map((file) => ({
        key: `${file.name}-${file.size}-${Date.now()}-${Math.random()}`,
        file,
        phase: "queued",
        progress: 0,
      }));
      setItems((prev) => [...newItems, ...prev]);
      newItems.forEach(runUpload);
    },
    [runUpload]
  );

  const onDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      setDragging(false);
      if (e.dataTransfer.files.length) addFiles(e.dataTransfer.files);
    },
    [addFiles]
  );

  // In compact (sidebar) mode, finished uploads disappear from here — they show up
  // in the live Documents list instead. Only in-flight/failed items stay visible.
  const visibleItems = compact
    ? items.filter((it) => it.phase !== "done")
    : items;

  return (
    <div className={`uploader${compact ? " uploader--compact" : ""}`}>
      <div
        className={`dropzone${dragging ? " dropzone--active" : ""}`}
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => {
          e.preventDefault();
          setDragging(true);
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={onDrop}
        role="button"
        tabIndex={0}
      >
        <div className="dropzone__icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path
              d="M12 16V4m0 0L7 9m5-5 5 5M4 17v2a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-2"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            />
          </svg>
        </div>
        <p className="dropzone__title">Drop files here or click to browse</p>
        <p className="dropzone__hint">PDFs and text documents</p>
        <input
          ref={inputRef}
          type="file"
          multiple
          hidden
          onChange={(e) => {
            if (e.target.files?.length) addFiles(e.target.files);
            e.target.value = "";
          }}
        />
      </div>

      {visibleItems.length > 0 && (
        <ul className="filelist">
          {visibleItems.map((it) => (
            <li key={it.key} className="fileitem">
              <div className="fileitem__row">
                <div className="fileitem__meta">
                  <span className="fileitem__name">{it.file.name}</span>
                  <span className="fileitem__size">
                    {formatSize(it.file.size)}
                  </span>
                </div>
                <span className={`badge badge--${it.phase}`}>
                  {it.phase === "done" && it.status
                    ? it.status
                    : PHASE_LABEL[it.phase]}
                </span>
              </div>

              {(it.phase === "uploading" || it.phase === "confirming") && (
                <div className="progress">
                  <div
                    className="progress__bar"
                    style={{
                      width:
                        it.phase === "confirming" ? "100%" : `${it.progress}%`,
                    }}
                  />
                </div>
              )}

              {it.phase === "done" && it.documentId != null && (
                <p className="fileitem__note">
                  Document #{it.documentId} stored in S3 ✓
                </p>
              )}
              {it.phase === "error" && (
                <p className="fileitem__error">{it.error}</p>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
