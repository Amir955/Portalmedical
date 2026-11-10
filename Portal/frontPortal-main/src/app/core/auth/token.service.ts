import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';

@Injectable({
  providedIn: 'root'
})
export class TokenService {
  private sessionExpiredHandled = false;

  constructor(
    private router: Router,
    private toastr: ToastrService
  ) {}

  set(data: any) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('id', data.id);
    this.sessionExpiredHandled = false;
  }

  handle(data: any) {
    this.set(data);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getId(): string | null {
    return localStorage.getItem('id');
  }

  remove() {
    localStorage.removeItem('token');
    localStorage.removeItem('id');
    this.sessionExpiredHandled = false;
  }

  decode(payload: string): any | null {
    try {
      return JSON.parse(atob(payload));
    } catch (error) {
      return null;
    }
  }

  payload(token: string | null): any | null {
    if (!token) {
      return null;
    }

    const parts = token.split('.');
    if (parts.length < 2) {
      return null;
    }

    return this.decode(parts[1]);
  }

  getUserName(): string | null {
    const payload = this.payload(this.getToken());
    return payload ? payload.name : null;
  }

  getExprirationDate(token: string | null): Date | null {
    const payload = this.payload(token);

    if (!payload || !payload.exp) {
      return null;
    }

    const date = new Date(0);
    date.setUTCSeconds(payload.exp);
    return date;
  }

  getUserRole(): string | null {
    const payload = this.payload(this.getToken());
    return payload ? payload.role : null;
  }

  isTokenExpired(token: string | null = this.getToken()): boolean {
    const expirationDate = this.getExprirationDate(token);

    if (!token || !expirationDate) {
      return true;
    }

    return expirationDate.getTime() <= new Date().getTime();
  }

  isValid(): boolean {
    const token = this.getToken();
    const id = this.getId();
    const payload = this.payload(token);

    if (!token || !id || !payload || this.isTokenExpired(token)) {
      return false;
    }

    if (payload.id === undefined || payload.id === null) {
      return true;
    }

    return String(id) === String(payload.id);
  }

  getInfos() {
    const token = this.getToken();
    const payload = this.payload(token);
    return payload ? payload : null;
  }

  loggedIn(): boolean {
    return this.isValid();
  }

  handleExpiredSession(message?: string) {
    if (this.sessionExpiredHandled) {
      return;
    }

    this.sessionExpiredHandled = true;
    this.remove();

    if (this.isPublicRoute(this.router.url)) {
      return;
    }

    if (message) {
      this.toastr.warning(message, 'Session expirée', {
        timeOut: 4500
      });
    }

    this.router.navigateByUrl('/connexion');
  }

  private isPublicRoute(fullUrl: string): boolean {
    const url = fullUrl.split('?')[0];
    return url === '/'
      || url === '/connexion'
      || url === '/inscription'
      || url === '/prendre-rendez-vous'
      || url.startsWith('/reinitialiser-mot-de-passe/')
      || url.startsWith('/annuler/');
  }
}
