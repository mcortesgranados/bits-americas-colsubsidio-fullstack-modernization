import { registerLocaleData } from '@angular/common';
import localeEsCo from '@angular/common/locales/es-CO';
import { APP_INITIALIZER, ApplicationConfig, LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { API_BASE_URL } from 'shared-core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { routes } from './app.routes';
import { AppConfigService, loadAppConfig } from './core/app-config.service';
import { apiPrefixInterceptor } from './core/api-prefix.interceptor';
import { authInterceptor } from './core/auth.interceptor';

/**
 * Configuración del shell.
 *
 * El orden de los interceptores importa: primero se resuelve la URL de la API y después se añade el
 * token, porque el segundo necesita la ruta ya absoluta para decidir si es la llamada de login.
 *
 * La locale se fija en `es-CO` para que los montos y las fechas se muestren como en Colombia
 * (`4.500.000` y `21/10/2026`) y no con el formato en-US que Angular usa por defecto. La configuración
 * vive en el shell y los remotos la heredan, porque comparten el inyector.
 */
registerLocaleData(localeEsCo, 'es-CO');

export const appConfig: ApplicationConfig = {
  providers: [
    { provide: LOCALE_ID, useValue: 'es-CO' },
    { provide: API_BASE_URL, useFactory: (config: AppConfigService) => config.apiBaseUrl, deps: [AppConfigService] },
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([apiPrefixInterceptor, authInterceptor])),
    {
      provide: APP_INITIALIZER,
      useFactory: loadAppConfig,
      deps: [AppConfigService],
      multi: true,
    },
  ],
};
