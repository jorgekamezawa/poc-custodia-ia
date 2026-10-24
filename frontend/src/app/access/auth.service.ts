import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export interface CurrentUser {
  username: string;
  displayName: string;
  roles: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  readonly user = signal<CurrentUser | null>(null);

  /** Asks the backend who is logged in; the answer also brings the CSRF cookie the login needs. */
  async loadCurrentUser(): Promise<CurrentUser | null> {
    try {
      this.user.set(await firstValueFrom(this.http.get<CurrentUser>('/api/auth/me')));
    } catch {
      this.user.set(null);
    }
    return this.user();
  }

  async login(username: string, password: string): Promise<CurrentUser> {
    const body = new HttpParams().set('username', username).set('password', password);
    const user = await firstValueFrom(this.http.post<CurrentUser>('/api/auth/login', body));
    this.user.set(user);
    return user;
  }

  async logout(): Promise<void> {
    try {
      await firstValueFrom(this.http.post<void>('/api/auth/logout', null));
    } finally {
      this.clear();
    }
  }

  clear(): void {
    this.user.set(null);
  }
}
