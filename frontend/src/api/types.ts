// Mirrors the backend DTOs (com.ali.docqa.dto).

export type DocumentStatus =
  | "PENDING"
  | "UPLOADED"
  | "PROCESSING"
  | "INDEXED"
  | "FAILED";

export interface CreateUploadRequest {
  filename: string;
  contentType: string;
}

export interface CreateUploadResponse {
  documentId: number;
  uploadUrl: string;
}

export interface ConfirmUploadResponse {
  documentId: number;
  status: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface RegisterResponse {
  id: number;
  username: string;
}
