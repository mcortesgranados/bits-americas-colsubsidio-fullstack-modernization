/**
 * Acceso al token de la sesión de demostración.
 *
 * El token vive en `sessionStorage` para sobrevivir a una recarga de la pestaña. El shell lo escribe al
 * iniciar sesión y los remotos lo leen cuando necesitan una conexión que no pasa por `HttpClient`, como el
 * stream SSE: `EventSource` no permite cabeceras y `fetch` sí, pero ninguno de los dos usa el interceptor.
 *
 * La llave se declara una sola vez aquí para que el shell y los remotos no puedan desincronizarse al
 * renombrarla. No es un mecanismo de seguridad: la firma la valida el backend en cada petición.
 */
import { InjectionToken } from '@angular/core';

export const DEMO_TOKEN_STORAGE_KEY = 'cs.demo.token';

/**
 * URL base de la API, resuelta en runtime desde ssets/config.json por el shell.
 *
 * Los remotos la usan cuando abren una conexión que no pasa por HttpClient (el stream SSE), porque el
 * interceptor que prefija /api solo actúa sobre las peticiones de Angular. El valor por defecto es vacío,
 * que deja las URLs relativas para el modo aislado.
 */
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => '',
});

/**
 * Devuelve el token vigente de la pestaña.
 *
 * @returns el token, o `null` cuando no hay sesión
 */
export function readDemoToken(): string | null {
  return sessionStorage.getItem(DEMO_TOKEN_STORAGE_KEY);
}
