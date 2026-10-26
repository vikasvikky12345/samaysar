import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, finalize, shareReplay, tap } from 'rxjs';

export type Role = 'OWNER' | 'DOCTOR' | 'RECEPTION' | 'ADMIN';

export interface Me {
  id: string;
  name: string;
  mobile: string;
  role: Role;
  clinic: { id: string; name: string; slug: string; planStatus: string; trialEndsAt: string | null };
}

export interface Session {
  accessToken: string;
  expiresInSeconds: number;
  user: Me;
}

export type VerifyResult =
  | ({ status: 'LOGGED_IN' } & Session)
  | { status: 'SIGNUP_REQUIRED'; signupToken: string };

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private refreshing: Observable<Session> | null = null;

  readonly user = signal<Me | null>(null);
  /** Kept in memory only; the refresh token lives in an HttpOnly cookie we never see. */
  readonly accessToken = signal<string | null>(null);

  sendOtp(mobile: string) {
    return this.http.post<{ expiresInSeconds: number }>('/api/auth/otp/send', { mobile });
  }

  verifyOtp(mobile: string, code: string) {
    return this.http
      .post<VerifyResult>('/api/auth/otp/verify', { mobile, code })
      .pipe(tap((r) => r.status === 'LOGGED_IN' && this.setSession(r)));
  }

  signup(body: { signupToken: string; ownerName: string; clinicName: string; city: string }) {
    return this.http.post<Session>('/api/auth/signup', body).pipe(tap((s) => this.setSession(s)));
  }

  /** Concurrent callers share one in-flight request, so the rotated cookie is only used once. */
  refresh(): Observable<Session> {
    this.refreshing ??= this.http.post<Session>('/api/auth/refresh', {}).pipe(
      tap((s) => this.setSession(s)),
      finalize(() => (this.refreshing = null)),
      shareReplay(1),
    );
    return this.refreshing;
  }

  logout() {
    this.clear();
    return this.http.post<void>('/api/auth/logout', {});
  }

  loadMe() {
    return this.http.get<Me>('/api/me').pipe(tap((u) => this.user.set(u)));
  }

  clear() {
    this.accessToken.set(null);
    this.user.set(null);
  }

  private setSession(s: Session) {
    this.accessToken.set(s.accessToken);
    this.user.set(s.user);
  }
}
