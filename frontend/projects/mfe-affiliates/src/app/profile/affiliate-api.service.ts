import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AffiliateProfile, Beneficiary, PageResponse, SubsidyRequestSummary } from 'shared-core';

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

  /**
   * Beneficiarios del titular (UC-02), ordenados por nombre por la API.
   *
   * La ruta es la del portal (`/me`), no la del back-office: el identificador del afiliado se resuelve
   * desde el token y el frontend no puede pedir los beneficiarios de otra persona ni por error.
   */
  getBeneficiaries(): Observable<Beneficiary[]> {
    return this.http.get<Beneficiary[]>('/api/v1/affiliates/me/beneficiaries');
  }

  /** Últimas solicitudes del titular, de la más reciente a la más antigua. */
  getRecentRequests(size = 5): Observable<PageResponse<SubsidyRequestSummary>> {
    return this.http.get<PageResponse<SubsidyRequestSummary>>('/api/v1/affiliates/me/subsidy-requests', {
      params: { page: 0, size, sort: 'submittedAt,desc' },
    });
  }
}
