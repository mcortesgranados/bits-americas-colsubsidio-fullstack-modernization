import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Adjunta el token a cada llamada y reacciona a una sesión caducada.
 *
 * El token no se añade a la petición de login, que es la única que se hace sin sesión. Cuando el servidor
 * responde `401` se limpia el estado local y se redirige al acceso conservando la ruta pedida, para que el
 * usuario vuelva donde estaba después de autenticarse.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.token();
  const isLogin = request.url.endsWith('/auth/demo-token');

  const authorized =
    token && !isLogin ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;

  return next(authorized).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && !isLogin) {
        auth.logout();
        void router.navigate(['/login'], { queryParams: { returnUrl: router.url } });
      }
      return throwError(() => error);
    }),
  );
};
