/**
 * Contratos compartidos entre el shell y los remotos.
 *
 * El archivo es la única fuente de tipos del frontend: si el backend cambia un campo, el error de
 * compilación aparece en los tres proyectos y no en uno solo. Las interfaces son solo de tipos, así que
 * no añaden código al bundle; la versión de contrato sí es un valor y ambos lados la comparan antes de
 * montar una ruta.
 */

/** Versión del contrato que hablan el shell y los remotos. Se incrementa en MAYOR si hay ruptura. */
export const CONTRACT_VERSION = '1.0.0';

/** Respuesta de `POST /api/v1/auth/demo-token`. */
export interface DemoTokenResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  role: string;
  affiliateId: number | null;
  companyId: number | null;
}

/** Credenciales que envía el formulario de acceso. */
export interface DemoTokenRequest {
  username: string;
  password: string;
}

/** Perfil que devuelve `GET /api/v1/affiliates/me`. */
export interface AffiliateProfile {
  id: number;
  fullName: string;
  documentType: string;
  documentNumber: string;
  email: string;
  phone: string | null;
  category: string | null;
  status: string;
  companyId: number | null;
  companyName: string | null;
  createdAt: string;
  updatedAt: string;
}

/**
 * Beneficiario registrado por el afiliado (`GET /api/v1/affiliates/me/beneficiaries`).
 *
 * `documentNumber` llega enmascarado desde la API: el portal nunca recibe el número completo, así que
 * no hay ningún punto del frontend donde pueda filtrarse.
 */
export interface Beneficiary {
  id: number;
  fullName: string;
  relationship: string;
  documentNumber: string | null;
  birthDate: string | null;
  status: string;
}

/** Resumen de una solicitud dentro del listado del portal. */
export interface SubsidyRequestSummary {
  id: number;
  requestNumber: string;
  subsidyType: string;
  status: string;
  requestedAmount: number;
  currency: string;
  submittedAt: string | null;
  decidedAt: string | null;
}

/** Cuerpo de `POST /api/v1/subsidy-requests`: solo lo que el afiliado elige. */
export interface SubsidyRequestCreate {
  subsidyType: string;
  requestedAmount: number;
  purpose: string | null;
}

/** Respuesta 201 al registrar una solicitud: radicado, estado y documentos exigidos. */
export interface SubsidyRequestCreated {
  id: number;
  requestNumber: string;
  status: string;
  subsidyType: string;
  requestedAmount: number;
  currency: string;
  submittedAt: string;
  requiredDocuments: string[];
}

/** Documento requerido de una solicitud (`GET /subsidy-requests/{id}/documents`). */
export interface SubsidyDocument {
  id: number;
  documentType: string;
  status: string;
  fileName: string | null;
  contentType: string | null;
  sizeBytes: number | null;
  checksum: string | null;
  uploadedAt: string | null;
}

/** Resultado de cargar o reemplazar un documento. */
export interface SubsidyDocumentUploaded extends SubsidyDocument {
  replaced: boolean;
}

/** Transición de estado de una solicitud (UC-05). */
export interface StatusHistoryEntry {
  id: number;
  fromStatus: string | null;
  toStatus: string;
  reason: string | null;
  actor: string;
  occurredAt: string;
  correlationId: string | null;
}
/** Detalle completo de una solicitud con documentos e historial. */
export interface SubsidyRequestDetail {
  id: number;
  requestNumber: string;
  subsidyType: string;
  status: string;
  requestedAmount: number;
  currency: string;
  purpose: string | null;
  submittedAt: string | null;
  decidedAt: string | null;
  decisionReason: string | null;
  pendingDocuments: number;
  rejectedDocuments: number;
  documents: SubsidyDocument[];
  history: StatusHistoryEntry[];
}
/** Evento del stream SSE de estados. */
export interface StatusChangedEvent {
  requestId: number;
  fromStatus: string | null;
  status: string;
  occurredAt: string;
}
/** Envoltorio de paginación que usa la API en lugar de exponer la página de Spring. */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Entrada del manifiesto de microfrontends, leída en runtime por el shell. */
export interface RemoteDefinition {
  remoteEntry: string;
  exposedModule: string;
  contractVersion: string;
}

/** Manifiesto completo, servido como `assets/mf.manifest.json`. */
export type MfManifest = Record<string, RemoteDefinition>;
