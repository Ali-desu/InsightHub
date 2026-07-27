import { API_BASE_URL } from "../config";
import { getToken } from "../auth";
import type {
  ConfirmUploadResponse,
  CreateUploadRequest,
  CreateUploadResponse,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from "./types";

async function parse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    if (res.status === 401 || res.status === 403) {
      throw new Error("Not authorized — please log in again.");
    }
    // Prefer the backend's clean { message } from the ApiError JSON body.
    let message = `${res.status} ${res.statusText}`;
    try {
      const body = (await res.json()) as { message?: string };
      if (body && typeof body.message === "string") message = body.message;
    } catch {
      /* body wasn't JSON — keep the status line */
    }
    throw new Error(message);
  }
  return (await res.json()) as T;
}

/** Authorization header with the stored JWT, for protected endpoints. */
function authHeaders(): Record<string, string> {
  const token = getToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

// ---- Auth ----

export async function login(body: LoginRequest): Promise<LoginResponse> {
  const res = await fetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  return parse<LoginResponse>(res);
}

export async function register(
  body: RegisterRequest
): Promise<RegisterResponse> {
  const res = await fetch(`${API_BASE_URL}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  return parse<RegisterResponse>(res);
}

// ---- Documents (protected — require the JWT) ----

/** Step 1: reserve a document + get a presigned S3 PUT URL. */
export async function createUpload(
  body: CreateUploadRequest
): Promise<CreateUploadResponse> {
  const res = await fetch(`${API_BASE_URL}/documents`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify(body),
  });
  return parse<CreateUploadResponse>(res);
}

/** Step 3: mark the upload complete (flips status PENDING -> UPLOADED). */
export async function confirmUpload(
  documentId: number
): Promise<ConfirmUploadResponse> {
  const res = await fetch(`${API_BASE_URL}/documents/${documentId}/confirm`, {
    method: "POST",
    headers: { ...authHeaders() },
  });
  return parse<ConfirmUploadResponse>(res);
}

/**
 * Step 2: PUT the raw file bytes directly to S3 via the presigned URL — the
 * backend never sees the bytes, and NO auth header goes here (it's S3, not our
 * API; the presigned URL is the credential). Uses XHR for upload progress.
 *
 * Content-Type MUST match the one used when presigning or S3 rejects the PUT.
 */
export function uploadToS3(
  uploadUrl: string,
  file: File,
  contentType: string,
  onProgress: (percent: number) => void
): Promise<void> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    xhr.open("PUT", uploadUrl);
    xhr.setRequestHeader("Content-Type", contentType);
    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable) onProgress(Math.round((e.loaded / e.total) * 100));
    };
    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) resolve();
      else reject(new Error(`S3 upload failed: ${xhr.status} ${xhr.responseText}`));
    };
    xhr.onerror = () =>
      reject(new Error("S3 upload failed — network or CORS error"));
    xhr.send(file);
  });
}
