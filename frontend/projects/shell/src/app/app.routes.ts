import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { loadRemoteRoutes } from './core/remote-loader';

/**
 * Rutas del contenedor.
 *
 * El portal de afiliados no se importa: se resuelve con `loadRemoteRoutes`, que lee el manifiesto en
 * runtime y monta las rutas del remoto bajo `/afiliados`. El resto son rutas propias del shell.
 */
export const routes: Routes = [
  {
    path: 'login',
    title: 'Acceso · Portal Colsubsidio (demo)',
    loadComponent: () => import('./pages/login/login-page.component').then((m) => m.LoginPageComponent),
  },
  {
    path: 'afiliados',
    canActivate: [authGuard],
    loadChildren: () => loadRemoteRoutes('mfeAffiliates'),
  },
  { path: '', pathMatch: 'full', redirectTo: 'afiliados' },
  {
    path: '**',
    title: 'Página no encontrada',
    loadComponent: () => import('./pages/not-found/not-found.component').then((m) => m.NotFoundComponent),
  },
];
