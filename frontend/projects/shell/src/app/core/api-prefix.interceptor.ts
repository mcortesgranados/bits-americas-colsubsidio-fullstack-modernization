import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AppConfigService } from './app-config.service';

/**
 * Resuelve la base de la API en tiempo de ejecución.
 *
 * Los servicios piden rutas relativas (`/api/v1/...`) y este interceptor les antepone la URL configurada.
 * Es lo que permite apuntar el mismo bundle a un backend local, a una instancia EC2 o a Azure sin tocar
 * el código: solo cambia `assets/config.json`.
 */
export const apiPrefixInterceptor: HttpInterceptorFn = (request, next) => {
  const config = inject(AppConfigService);

  if (!config.apiBaseUrl || !request.url.startsWith('/api')) {
    return next(request);
  }

  return next(request.clone({ url: `${config.apiBaseUrl}${request.url}` }));
};
