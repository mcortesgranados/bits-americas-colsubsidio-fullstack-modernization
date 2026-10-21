import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Impide entrar a una ruta protegida sin sesión.
 *
 * El guard es una comodidad de navegación, no un control de seguridad: la autorización real la hace el
 * backend con el token. Aquí solo se evita mostrar una pantalla vacía y se conserva la ruta pedida para
 * volver a ella tras el login.
 */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
