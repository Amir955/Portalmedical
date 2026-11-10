import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';
import { LoaderService } from '../services/loader.service';
import { ToastrService } from 'ngx-toastr';
import { TokenService } from './token.service';

@Injectable({
  providedIn: 'root'
})
export class JwtInterceptorService implements HttpInterceptor {
  constructor(
    private token: TokenService,
    private load: LoaderService,
    private toastr: ToastrService
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.token.getToken();

    // Public endpoints work without a session; a valid token is still sent so the
    // server can tailor the answer (e.g. full doctor records for staff).
    if (this.isPublic(request.url)) {
      const publicRequest = token && !this.token.isTokenExpired(token)
        ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
        : request;
      this.load.isloading.next(true);
      return next.handle(publicRequest).pipe(finalize(() => {
        this.load.isloading.next(false);
      }));
    }

    if (this.token.isTokenExpired(token)) {
      this.load.isloading.next(false);
      this.token.handleExpiredSession('Votre session a expiré. Reconnectez-vous pour continuer.');
      return throwError(() => new Error('JWT expired before request'));
    }

    const authenticatedRequest = token
      ? request.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
          }
        })
      : request;

    this.load.isloading.next(true);
    return next.handle(authenticatedRequest).pipe(
      catchError((error: HttpErrorResponse) => {
        if (this.isExpiredTokenError(error)) {
          this.token.handleExpiredSession('Votre session a expiré. Reconnectez-vous pour continuer.');
        } else if (error && error.status === 403) {
          // Signed in, but not allowed for this account (e.g. medical records are reserved
          // to doctors): say so instead of logging the user out.
          this.toastr.warning('Cette action est réservée au médecin.', 'Accès réservé');
        }

        return throwError(() => error);
      }),
      finalize(() => {
        this.load.isloading.next(false);
      })
    );
  }

  /** Endpoints the backend serves without a session (see Security.java). */
  private isPublic(url: string): boolean {
    return [
      '/users/login',
      '/user/add',
      '/user/register',
      '/user/sendEmail',
      '/user/resetPassword',
      '/user/getbyRole/Medecin',
      '/rendezvous/demandeRDV',
      '/rendezvous/creneauxPris/',
      '/creneaux',
      '/calendrier',
      '/rendezvous/annulation/'
    ].some((path) => url.indexOf(path) !== -1);
  }

  private isExpiredTokenError(error: HttpErrorResponse): boolean {
    if (!error) {
      return false;
    }

    // 401 = no valid session. 403 = valid session without the right role, unless the
    // server says the token itself is the problem (checked below).
    if (error.status === 401) {
      return true;
    }

    const message = this.extractErrorMessage(error).toLowerCase();
    return message.indexOf('expiredjwtexception') !== -1
      || message.indexOf('jwt expired') !== -1
      || message.indexOf('token expired') !== -1
      || message.indexOf('signature exception') !== -1
      || message.indexOf('malformedjwt') !== -1;
  }

  private extractErrorMessage(error: HttpErrorResponse): string {
    if (!error.error) {
      return error.message || '';
    }

    if (typeof error.error === 'string') {
      return error.error;
    }

    if (typeof error.error.message === 'string') {
      return error.error.message;
    }

    return error.message || '';
  }
}
