import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AffiliateProfile, PageResponse, SubsidyRequestSummary } from 'shared-core';

/**
 * Acceso a los endpoints del afiliado autenticado.
 *
 * Las rutas son relativas a propósito: el interceptor del shell antepone la base configurada en
 * `assets/config.json`. El remoto no sabe en qué host vive la API, y por eso puede publicarse en un
 * dominio distinto sin cambios.
 */
@Injectable({ providedIn: 'root' })
export class AffiliateApiService {
  private readonly http = inject(HttpClient);

  /** Perfil del titular resuelto desde el token, nunca desde un identificador de la URL. */
  getProfile(): Observable<AffiliateProfile> {
    return this.http.get<AffiliateProfile>('/api/v1/affiliates/me');
  }

  /** Últimas solicitudes del titular, de la más reciente a la más antigua. */
  getRecentRequests(size = 5): Observable<PageResponse<SubsidyRequestSummary>> {
    return this.http.get<PageResponse<SubsidyRequestSummary>>('/api/v1/affiliates/me/subsidy-requests', {
      params: { page: 0, size, sort: 'submittedAt,desc' },
    });
  }
}
