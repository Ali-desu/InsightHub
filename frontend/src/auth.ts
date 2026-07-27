// Tiny client-side auth state: the JWT lives in localStorage, and we read the
// username straight out of the token's payload (the "sub" claim) for display.

const TOKEN_KEY = "insighthub.token";

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

/** Decode the JWT payload (base64url) and return its "sub" (the username). */
export function usernameFromToken(token: string): string | null {
  try {
    const payload = token.split(".")[1];
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    const claims = JSON.parse(json) as { sub?: string; exp?: number };
    // Treat an expired token as logged out.
    if (claims.exp && claims.exp * 1000 < Date.now()) return null;
    return claims.sub ?? null;
  } catch {
    return null;
  }
}
