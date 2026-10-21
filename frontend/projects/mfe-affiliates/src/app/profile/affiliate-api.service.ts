import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AffiliateProfile,
  Beneficiary,
  PageResponse,
  SubsidyDocument,
  SubsidyDocumentUploaded,
  SubsidyRequestDetail,
  SubsidyRequestCreate,
  SubsidyRequestCreated,
  SubsidyRequestSummary,
} from 'shared-core';

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

  /**
   * Registra una solicitud de subsidio (UC-03).
   *
   * El cuerpo solo lleva tipo, monto y motivo: el afiliado y el radicado los resuelve el backend. Un 201
   * devuelve el radicado y el catálogo de documentos que el asistente muestra como siguiente paso.
   */
  createSubsidyRequest(payload: SubsidyRequestCreate): Observable<SubsidyRequestCreated> {
    return this.http.post<SubsidyRequestCreated>('/api/v1/subsidy-requests', payload);
  }

  /** Detalle de la solicitud con documentos e historial (UC-05). */
  getRequestDetail(requestId: number): Observable<SubsidyRequestDetail> {
    return this.http.get<SubsidyRequestDetail>(`/api/v1/subsidy-requests/${requestId}`);
  }

  /** Documentos exigidos por una solicitud, con su estado actual (UC-04). */
  getDocuments(requestId: number): Observable<SubsidyDocument[]> {
    return this.http.get<SubsidyDocument[]>(`/api/v1/subsidy-requests/${requestId}/documents`);
  }

  /**
   * Carga o reemplaza un documento (UC-04).
   *
   * El `Content-Type` no se fija a mano: el navegador debe añadir el `boundary` del multipart, y forzarlo
   * rompería el parseo en el servidor. El nombre del campo (`file`) y el tipo van como campos del formulario.
   */
  uploadDocument(requestId: number, documentType: string, file: File): Observable<SubsidyDocumentUploaded> {
    const body = new FormData();
    body.append('documentType', documentType);
    body.append('file', file);
    return this.http.post<SubsidyDocumentUploaded>(`/api/v1/subsidy-requests/${requestId}/documents`, body);
  }

  /** Descarga el archivo como blob; el enlace directo no enviaría el token de sesión. */
  downloadDocument(requestId: number, documentType: string): Observable<Blob> {
    return this.http.get(
      `/api/v1/subsidy-requests/${requestId}/documents/${documentType}/content`,
      { responseType: 'blob' },
    );
  }

  /** Últimas solicitudes del titular, de la más reciente a la más antigua. */
  getRecentRequests(size = 5): Observable<PageResponse<SubsidyRequestSummary>> {
    return this.http.get<PageResponse<SubsidyRequestSummary>>('/api/v1/affiliates/me/subsidy-requests', {
      params: { page: 0, size, sort: 'submittedAt,desc' },
    });
  }
}
