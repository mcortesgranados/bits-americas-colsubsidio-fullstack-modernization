import { Routes } from '@angular/router';

/**
 * Rutas que el shell monta bajo `/afiliados`.
 *
 * El contrato entre contenedor y remoto es esta lista: el shell no conoce componentes del remoto y el
 * remoto no conoce el layout del shell. Añadir una pantalla nueva es añadir una ruta aquí; el manifiesto
 * y el shell no cambian.
 */
export const routes: Routes = [
  {
    path: '',
    title: 'Mi perfil · Portal de afiliados',
    loadComponent: () => import('../profile/profile-page.component').then((m) => m.ProfilePageComponent),
  },
];
