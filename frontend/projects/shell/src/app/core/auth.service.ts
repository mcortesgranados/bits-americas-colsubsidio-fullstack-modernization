import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { DEMO_TOKEN_STORAGE_KEY, DemoTokenRequest, DemoTokenResponse } from 'shared-core';
import { Observable, tap } from 'rxjs';

const STORAGE_KEY = DEMO_TOKEN_STORAGE_KEY;

/**
 * Estado de sesión del portal.
 *
 * El token vive en memoria y se refleja en `sessionStorage` para sobrevivir a una recarga de la pestaña.
 * No se usa cookie: el portal y la API pueden estar en dominios distintos, y una cookie cruzada exigiría
 * `SameSite=None`, permiso de terceros y protección CSRF adicional. Con la cabecera `Authorization` el
 * problema desaparece y el token se descarta al cerrar la pestaña.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenState = signal<string | null>(sessionStorage.getItem(STORAGE_KEY));

  /** Token vigente, o `null` si no hay sesión. */
  readonly token = this.tokenState.asReadonly();

  /** Indica si hay un token en memoria; el servidor sigue siendo quien valida su firma y vigencia. */
  readonly isAuthenticated = computed(() => this.tokenState() !== null);

  /**
   * Canjea credenciales por un token y guarda el resultado.
   *
   * @param credentials usuario y contraseña del formulario
   * @returns el observable de la respuesta, ya con el token almacenado al completarse
   */
  login(credentials: DemoTokenRequest): Observable<DemoTokenResponse> {
    return this.http.post<DemoTokenResponse>('/api/v1/auth/demo-token', credentials).pipe(
      tap((response) => {
        this.tokenState.set(response.accessToken);
        sessionStorage.setItem(STORAGE_KEY, response.accessToken);
      }),
    );
  }

  /** Descarta el token en memoria y en el almacenamiento de la pestaña. */
  logout(): void {
    this.tokenState.set(null);
    sessionStorage.removeItem(STORAGE_KEY);
  }
}
