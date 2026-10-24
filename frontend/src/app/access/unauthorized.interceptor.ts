import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

// These two answer 401 as part of their normal flow; the caller decides what to do.
const AUTH_ENDPOINTS = ['/api/auth/me', '/api/auth/login'];

export const unauthorizedInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return next(request).pipe(
    catchError((error: unknown) => {
      if (
        error instanceof HttpErrorResponse &&
        error.status === 401 &&
        !AUTH_ENDPOINTS.includes(request.url)
      ) {
        auth.clear();
        void router.navigate(['/entrar']);
      }
      return throwError(() => error);
    }),
  );
};
