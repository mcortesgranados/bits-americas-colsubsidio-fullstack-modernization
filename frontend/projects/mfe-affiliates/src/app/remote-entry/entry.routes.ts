import { Routes } from '@angular/router';
import { AffiliateShellComponent } from '../affiliate-shell/affiliate-shell.component';

/**
 * Rutas que el shell monta bajo `/afiliados`.
 *
 * El contrato entre contenedor y remoto es esta lista: el shell no conoce componentes del remoto y el
 * remoto no conoce el layout del shell. Añadir una pantalla nueva es añadir una ruta hija aquí; el
 * manifiesto y el shell no cambian. La ruta padre aporta la navegación entre secciones del portal.
 */
export const routes: Routes = [
  {
    path: '',
    component: AffiliateShellComponent,
    children: [
      {
        path: '',
        title: 'Mi perfil · Portal de afiliados',
        loadComponent: () => import('../profile/profile-page.component').then((m) => m.ProfilePageComponent),
      },
      {
        path: 'beneficiarios',
        title: 'Mis beneficiarios · Portal de afiliados',
        loadComponent: () =>
          import('../beneficiaries/beneficiaries-page.component').then((m) => m.BeneficiariesPageComponent),
      },
      {
        path: 'solicitudes',
        children: [
          {
            path: 'nueva',
            title: 'Nueva solicitud · Portal de afiliados',
            loadComponent: () =>
              import('../requests/subsidy-request-page.component').then((m) => m.SubsidyRequestPageComponent),
          },
          {
            path: ':id',
            title: 'Detalle de la solicitud · Portal de afiliados',
            loadComponent: () =>
              import('../requests/request-detail.component').then((m) => m.RequestDetailComponent),
          },
          {
            path: ':id/documentos',
            title: 'Documentos de la solicitud · Portal de afiliados',
            loadComponent: () =>
              import('../requests/documents-page.component').then((m) => m.DocumentsPageComponent),
          },
        ],
      },
    ],
  },
];
