import { APP_INITIALIZER, ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
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
 */
export const appConfig: ApplicationConfig = {
  providers: [
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
