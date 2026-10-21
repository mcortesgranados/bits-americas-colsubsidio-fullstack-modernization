import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

interface AppConfig {
  apiBaseUrl: string;
}

/**
 * Configuración de la aplicación leída en tiempo de ejecución.
 *
 * La URL de la API no se compila dentro del bundle: se sirve junto al shell en `assets/config.json`. Así,
 * mover el backend entre local, EC2 o Azure es cambiar un JSON y recargar, sin recompilar los tres
 * proyectos. Si el archivo no está o llega corrupto, la aplicación arranca con URLs relativas y deja el
 * error para el primer request, en lugar de quedarse en blanco.
 */
@Injectable({ providedIn: 'root' })
export class AppConfigService {
  private readonly http = inject(HttpClient);
  private config: AppConfig = { apiBaseUrl: '' };

  /** Carga la configuración; la llama el inicializador de la aplicación antes del primer render. */
  async load(): Promise<void> {
    try {
      this.config = await firstValueFrom(this.http.get<AppConfig>('assets/config.json'));
    } catch {
      console.warn('assets/config.json no disponible: se usan URLs relativas');
      this.config = { apiBaseUrl: '' };
    }
  }

  /** Base de la API sin barra final, para concatenar rutas que empiezan por `/api`. */
  get apiBaseUrl(): string {
    return this.config.apiBaseUrl.replace(/\/+$/, '');
  }
}

/** Fábrica que usa Angular para esperar a la configuración durante el arranque. */
export function loadAppConfig(config: AppConfigService): () => Promise<void> {
  return () => config.load();
}
